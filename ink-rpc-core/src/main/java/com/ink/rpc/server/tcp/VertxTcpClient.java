package com.ink.rpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.ink.rpc.RpcApplication;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.model.ServiceMetaInfo;
import com.ink.rpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    /**
     * 发送 TCP 请求
     *
     * @param rpcRequest      要发送的请求
     * @param serviceMetaInfo 请求服务的信息
     * @return 响应对象
     * @throws ExecutionException   抛出执行异常
     * @throws InterruptedException 抛出中断异常
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo) throws ExecutionException, InterruptedException {
        //获取 Vertx 对象
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
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
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
        return rpcResponse;
    }


}
