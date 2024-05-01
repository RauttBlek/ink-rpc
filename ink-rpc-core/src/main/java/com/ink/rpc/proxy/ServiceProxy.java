package com.ink.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.ink.rpc.RpcApplication;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.config.RpcConfig;
import com.ink.rpc.constant.RpcConstant;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.model.ServiceMetaInfo;
import com.ink.rpc.protocol.*;
import com.ink.rpc.registry.Registry;
import com.ink.rpc.registry.RegistryFactory;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import com.ink.rpc.serializer.SerializerKeys;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
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
        final Serializer serializer = SerializerFactory.getSerializerInstance(SerializerKeys.JDK);
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
        //目前只有一种服务，先取第一个
        ServiceMetaInfo serviceMetaInfo = serviceMetaInfos.get(0);

        //发送 TCP 请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> completableFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceAddress(),
                result -> {
                    if (result.succeeded()) {
                        System.out.println("已连接到 TCP 服务");
                        NetSocket socket = result.result();
                        //连接成功后，构造消息对象
                        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(rpcConfig.getSerializer()).getKey());
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        protocolMessage.setHeader(header);
                        protocolMessage.setBody(rpcRequest);
                        //编码并发送消息对象
                        try {
                            Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(encodeBuffer);
                        } catch (IOException e) {
                            throw new RuntimeException("协议消息编码错误");
                        }
                        //接收响应
                        socket.handler(buffer -> {
                            try {
                                ProtocolMessage<RpcResponse> decodeMessage = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);
                                completableFuture.complete(decodeMessage.getBody());
                            } catch (IOException e) {
                                throw new RuntimeException("协议消息解码错误");
                            }
                        });
                    } else {
                        System.err.println("连接 TCP 服务失败");
                    }
                });
        RpcResponse rpcResponse = completableFuture.get();
        //关闭 TCP 连接
        netClient.close();
        return rpcResponse.getData();

    }
}
