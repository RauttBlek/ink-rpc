package com.ink.rpc.server.tcp;

import com.ink.rpc.server.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

import java.nio.charset.StandardCharsets;

public class VertxTcpServer implements HttpServer {

    private byte[] handleRequest(byte[] requestData) {
        return "处理后的字节数组：".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doStart(int port) {
        //创建 Vertx 实例
        Vertx vertx = Vertx.vertx();
        //创建一个 tcp 服务器
        NetServer netServer = vertx.createNetServer();

        //请求处理
        netServer.connectHandler(new VertxTcpServerHandler());
//        netServer.connectHandler(netSocket -> {
//            //连接，传输数据及处理
//            netSocket.handler(buffer -> {
//                //接收字节数组
//                byte[] bytes = buffer.getBytes();
//                //进行字节数组的处理（解析请求，调用服务，构造响应）
//                byte[] responseData = handleRequest(bytes);
//                //向客户端发送响应数据，格式为 Buffer
//                netSocket.write(Buffer.buffer(responseData));
//            });
//        });

        //启动服务器，监听端口
        netServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP 服务器已启动于：" + port);
            } else {
                System.err.println("TCP 服务启动失败：" + result.cause());
            }
        });

    }
}
