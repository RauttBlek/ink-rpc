package com.ink.rpc.server.tcp;

import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.protocol.ProtocolMessage;
import com.ink.rpc.protocol.ProtocolMessageDecoder;
import com.ink.rpc.protocol.ProtocolMessageEncoder;
import com.ink.rpc.protocol.ProtocolMessageTypeEnum;
import com.ink.rpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 服务端协议消息处理器
 */
public class VertxTcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket netSocket) {

        BufferHandlerWrapper bufferHandlerWrapper = new BufferHandlerWrapper(buffer -> {
            //接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            RpcResponse rpcResponse = new RpcResponse();
            try {
                //获取要调用的实现类，并通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParaType());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), rpcRequest.getParaArray());
                //封装响应
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (Exception e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });

        netSocket.handler(bufferHandlerWrapper);
    }
}
