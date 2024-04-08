package com.ink.rpc.serializer;

import java.io.IOException;

public interface Serializer {

    /**
     * 序列化
     * @param object 需要序列化的对象
     * @return 序列化的字节数组
     * @param <T> 泛型方法，被序列化的对象类型需指定
     * @throws IOException 抛出 IO 异常
     */
    <T> byte[] serializer(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param type 反序列化的对象类型
     * @return 指定类型的对象
     * @param <T> 泛型方法，反序列化出的对象类型需指定
     * @throws IOException 抛出 IO 异常
     */
    <T> T deSerialize(byte[] bytes, Class<T> type) throws IOException;

}
