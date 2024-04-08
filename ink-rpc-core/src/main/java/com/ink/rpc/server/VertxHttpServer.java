package com.ink.rpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        //获取 vertx 实例并创建 HTTP 服务器
        Vertx vertx = Vertx.vertx();
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //处理请求
        server.requestHandler(new HttpServerHandler());
//        server.requestHandler(request -> {
//            //处理 HTTP 请求，此处只做输出
//            System.out.println("Received request:" + request.method() + " " + request.uri());
//
//            //处理后发送 HTTP 响应
//            request.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Morning from Vert.x HTTP server!");
//        });

        //启动 Vertx HTTP 服务器，指定监听的端口
        server.listen(port, result -> {
           if(result.succeeded()){
               System.out.println("启动成功，正在监听端口：" + port);
           }else{
               System.err.println("启动失败：" + result.cause());
           }
        });
    }
}
