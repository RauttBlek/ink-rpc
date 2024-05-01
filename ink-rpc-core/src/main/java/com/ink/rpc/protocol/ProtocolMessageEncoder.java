package com.ink.rpc.protocol;

import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 协议消息编码器
 */
public class ProtocolMessageEncoder {

    /**
     * 编码
     * @param protocolMessage 协议消息
     * @return 编码成的 Buffer 对象
     * @throws IOException IO异常
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        Buffer buffer = Buffer.buffer();
        //向缓冲区写入字节
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());

        //获取序列化器名称
        ProtocolMessageSerializerEnum serializer = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if(serializer == null){
            throw new RuntimeException("序列化协议不存在");
        }
        //获取序列化器实例
        Serializer serializerInstance = SerializerFactory.getSerializerInstance(serializer.getValue());
        //序列化消息体
        byte[] bodyBytes = serializerInstance.serializer(protocolMessage.getBody());
        //写入消息体长度及消息体
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}
