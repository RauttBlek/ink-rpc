package com.ink.provider;

import com.ink.common.service.UserService;
import com.ink.rpc.RpcApplication;
import com.ink.rpc.config.RegistryConfig;
import com.ink.rpc.config.RpcConfig;
import com.ink.rpc.model.ServiceMetaInfo;
import com.ink.rpc.registry.LocalRegistry;
import com.ink.rpc.registry.Registry;
import com.ink.rpc.registry.RegistryFactory;
import com.ink.rpc.server.tcp.VertxTcpServer;

public class EasyProviderExample {

    public static void main(String[] args) {
        String serviceName = UserService.class.getName();
        //获取注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
//        serviceMetaInfo.setServiceAddress(rpcConfig.getServerHost() + ":" + rpcConfig.getServerPort());

        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //在本地保存一个注册信息
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        //VertxHttpServer httpServer = new VertxHttpServer();
        //启动 TCP 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(8080);
    }
}
