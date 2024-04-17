package com.ink.rpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import com.ink.rpc.serializer.SerializerKeys;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 服务代理类（基于 JDK 动态代理）
 */
public class ServiceProxy implements InvocationHandler {
    /**
     *调用代理
     * @return rpcResponse
     * @throws Throwable 可抛出的受检异常
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Serializer serializer = SerializerFactory.getSerializerInstance(SerializerKeys.JDK);
        //构造请求对象
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paraType(method.getParameterTypes())
                .paraArray(args)
                .build();
        try{
            //序列化
            byte[] serializedObject = serializer.serializer(rpcRequest);
            //发送请求，此处地址硬编码，临时使用
            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
                        .body(serializedObject)
                        .execute()){
                byte[] bodyBytes = httpResponse.bodyBytes();
                //将响应中字节数组反序列化
                RpcResponse rpcResponse = serializer.deSerialize(bodyBytes, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
