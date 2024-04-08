package com.ink.rpc.server;

public interface HttpServer {
    /**
     * 用于启动服务器
     * @param port 端口号
     */
    void doStart(int port);
}
