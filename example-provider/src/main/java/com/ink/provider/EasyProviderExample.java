package com.ink.provider;

import com.ink.common.service.UserService;
import com.ink.rpc.registry.LocalRegistry;
import com.ink.rpc.server.VertxHttpServer;

public class EasyProviderExample {

    public static void main(String[] args) {

        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);
        VertxHttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
