package com.ink.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProtocolMessage<T> {
    /**
     * 消息头
     */
    private Header header;
    /**
     * 消息体
     */
    private T body;

    /**
     * 消息头内部类
     */
    @Data
    public static class Header {
        /**
         * 魔数，确保安全性
         */
        private byte magic;
        /**
         * 版本号
         */
        private byte version;
        /**
         * 序列化器
         */
        private byte serializer;
        /**
         * 消息类型（请求/响应）
         */
        private byte type;
        /**
         * 消息状态
         */
        private byte status;
        /**
         * 请求 ID
         */
        private long requestId;
        /**
         * 消息体长度
         */
        private int bodyLength;
    }
}
