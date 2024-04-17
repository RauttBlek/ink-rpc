package com.ink.rpc.serializer;

import com.ink.rpc.spi.SpiLoader;

public class SerializerFactory {

    static {
        //加载序列化器类
        SpiLoader.load(Serializer.class);
    }

//    /**
//     * 字符串 => 序列化器 映射，用于实现单例
//     */
//    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>(){
//        {
//            put(SerializerKeys.JDK, new JdkSerializer());
//            put(SerializerKeys.JSON, new JsonSerializer());
//            put(SerializerKeys.HESSIAN, new HessianSerializer());
//            put(SerializerKeys.KRYO, new KryoSerializer());
//        }
//    };
    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取一个具体的序列化器实例
     * @param key 序列化器名称
     * @return 序列化器实例
     */
    public static Serializer getSerializerInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

}
