package com.ink.rpc.protocol;

import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

public class ProtocolMessageDecoder {
    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        byte magic = buffer.getByte(0);
        if(magic != ProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException("消息 magic 非法");
        }
        //向 header 中填充信息
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getByte(5));
        header.setBodyLength(13);//请求 ID 为 long 类型，占 8 字节，因此读到第 13 字节
        //为解决粘包问题，读取指定长度字节
        byte[] bodyBytes = buffer.getBytes(17, 17 + header.getBodyLength());
        //获取序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new RuntimeException("未查询到或未指定序列化协议");
        }
        Serializer serializerInstance = SerializerFactory.getSerializerInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum typeEnum = ProtocolMessageTypeEnum.getEnumByKey(header.getType());
        if (typeEnum == null) {
            throw new RuntimeException("未查询到或未指定消息类型");
        }
        switch (typeEnum){
            case REQUEST:
                RpcRequest request = serializerInstance.deSerialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, request);
            case RESPONSE:
                RpcResponse response = serializerInstance.deSerialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, response);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("不支持的消息类型");
        }
    }
}
