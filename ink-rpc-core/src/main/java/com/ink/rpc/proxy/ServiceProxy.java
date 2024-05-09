package com.ink.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.ink.rpc.RpcApplication;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.config.RpcConfig;
import com.ink.rpc.constant.RpcConstant;
import com.ink.rpc.loadbalancer.LoadBalancer;
import com.ink.rpc.loadbalancer.LoadBalancerFactory;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.model.ServiceMetaInfo;
import com.ink.rpc.protocol.*;
import com.ink.rpc.registry.Registry;
import com.ink.rpc.registry.RegistryFactory;
import com.ink.rpc.retry.RetryStrategy;
import com.ink.rpc.retry.RetryStrategyFactory;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import com.ink.rpc.serializer.SerializerKeys;
import com.ink.rpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理类（基于 JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     * 调用代理
     *
     * @return rpcResponse
     * @throws Throwable 可抛出的受检异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //获取序列化器
        //final Serializer serializer = SerializerFactory.getSerializerInstance(SerializerKeys.JDK);
        //构造请求对象
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .paraType(method.getParameterTypes())
                .paraArray(args)
                .build();

        //序列化
        //byte[] serializedObject = serializer.serializer(rpcRequest);
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
        if (CollUtil.isEmpty(serviceMetaInfos)) {
            throw new RuntimeException("查询不到服务");
        }

        //使用负载均衡器选择服务节点
        Map<String, Object> paramMap = new HashMap<>();
        //将请求方法名作为负载均衡参数
        paramMap.put("methodName", method.getName());
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
        ServiceMetaInfo serviceMetaInfo = loadBalancer.select(paramMap, serviceMetaInfos);

        RetryStrategy retryStrategy = RetryStrategyFactory.getRetryStrategyInstance(rpcConfig.getRetryStrategy());
        RpcResponse rpcResponse = retryStrategy.doRetry(
                () -> VertxTcpClient.doRequest(rpcRequest, serviceMetaInfo)
        );
        return rpcResponse.getData();

    }
}
