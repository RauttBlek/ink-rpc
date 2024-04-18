package com.ink.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.ink.rpc.RpcApplication;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.config.RpcConfig;
import com.ink.rpc.constant.RpcConstant;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.model.ServiceMetaInfo;
import com.ink.rpc.registry.Registry;
import com.ink.rpc.registry.RegistryFactory;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import com.ink.rpc.serializer.SerializerKeys;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务代理类（基于 JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     *调用代理
     * @return rpcResponse
     * @throws Throwable 可抛出的受检异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Serializer serializer = SerializerFactory.getSerializerInstance(SerializerKeys.JDK);
        //构造请求对象
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paraType(method.getParameterTypes())
                .paraArray(args)
                .build();
        try{
            //序列化
            byte[] serializedObject = serializer.serializer(rpcRequest);
            //从注册中心取得服务名称与地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            //封装查询用服务元信息
            ServiceMetaInfo searchServiceInfo = new ServiceMetaInfo();
            searchServiceInfo.setServiceName(serviceName);
            searchServiceInfo.setVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            //查询服务信息
            List<ServiceMetaInfo> serviceMetaInfos = registry.serviceDiscovery(searchServiceInfo.getServiceKey());
            //查询结果判空
            if(CollUtil.isEmpty(serviceMetaInfos)){
                throw new RuntimeException("查询不到服务");
            }
            //目前只有一种服务，先取第一个
            ServiceMetaInfo serviceMetaInfo = serviceMetaInfos.get(0);

            //发送请求
            try(HttpResponse httpResponse = HttpRequest.post(serviceMetaInfo.getServiceAddress())
                        .body(serializedObject)
                        .execute()){
                byte[] bodyBytes = httpResponse.bodyBytes();
                //将响应中字节数组反序列化
                RpcResponse rpcResponse = serializer.deSerialize(bodyBytes, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
