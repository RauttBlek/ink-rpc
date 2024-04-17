package com.ink.rpc.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;

import java.io.IOException;

public class JsonSerializer implements Serializer{
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serializer(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deSerialize(byte[] bytes, Class<T> type) throws IOException {
        T obj = OBJECT_MAPPER.readValue(bytes, type);
        if(obj instanceof RpcRequest){
            return handleRequest((RpcRequest) obj, type);
        }
        if(obj instanceof RpcResponse){
            return handleResponse((RpcResponse) obj, type);
        }
        return obj;
    }

    /**
     * 用于防止 Object 反序列化时被作为 LinkedHashMap 无法转换为原始对象，对请求体做特殊处理
     * @param rpcRequest 请求对象
     * @param type 反序列化要转换的类型
     * @return 处理后的请求对象
     * @param <T> 类型参数
     * @throws IOException IO 异常
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] paraTypes = rpcRequest.getParaType();
        Object[] args = rpcRequest.getParaArray();
        for(int i = 0;i < paraTypes.length;i++){
            Class<?> paraType = paraTypes[i];
            if(!paraType.isAssignableFrom(args[i].getClass())){
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(bytes, paraType);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     * 用于防止 Object 反序列化时被作为 LinkedHashMap 无法转换为原始对象，对响应体做特殊处理
     * @param rpcResponse 响应对象
     * @param type 反序列化要转换的类型
     * @return 处理后的响应对象
     * @param <T> 类型参数
     * @throws IOException IO 异常
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse);
        rpcResponse.setData(OBJECT_MAPPER.readValue(bytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }

}
