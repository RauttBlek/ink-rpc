package com.ink.rpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;

public class VertxTcpClient {

    public void start() {
        //创建 Vertx 实例
        Vertx vertx = Vertx.vertx();

        //连接服务
        vertx.createNetClient().connect(8080, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("已连接到 TCP 服务");
                NetSocket socket = result.result();
                //发送数据
                socket.write("Hello server!");
                //接收响应
                socket.handler(buffer -> {
                    System.out.println("收到响应：" + buffer.toString());
                });
            } else {
                System.err.println("连接 TCP 服务失败");
            }
        });
    }


}
