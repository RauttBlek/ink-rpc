package com.ink.rpc.server;

import com.ink.rpc.model.RpcRequest;
import com.ink.rpc.model.RpcResponse;
import com.ink.rpc.registry.LocalRegistry;
import com.ink.rpc.serializer.Serializer;
import com.ink.rpc.serializer.SerializerFactory;
import com.ink.rpc.serializer.SerializerKeys;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

public class HttpServerHandler implements Handler<HttpServerRequest> {
    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        //指定一个序列化器
        final Serializer serializer = SerializerFactory.getSerializerInstance(SerializerKeys.JDK);

        //打印日志
        System.out.println("Received Request:" + httpServerRequest.method() + " " + httpServerRequest.uri());

        //异步处理 HTTP 请求
        httpServerRequest.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest request = null;
            try{
                //反序列化
                request = serializer.deSerialize(bytes, RpcRequest.class);
            }catch (IOException e){
                e.printStackTrace();
            }
            //构造响应对象
            RpcResponse rpcResponse = new RpcResponse();
            //当请求为空时直接返回
            if(request == null){
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(httpServerRequest, rpcResponse, serializer);
                return;
            }
            try{
                //获取要调用的实现类，并通过反射调用
                Class<?> implClass = LocalRegistry.get(request.getServiceName());
                //Class<?> implClass = Class.forName(request.getServiceName());
                Method method = implClass.getMethod(request.getMethodName(), request.getParaType());
                Object result = method.invoke(implClass.getDeclaredConstructor().newInstance(), request.getParaArray());
                //封装响应
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            }catch (Exception e){
                e.printStackTrace();
            }
            //响应
            doResponse(httpServerRequest, rpcResponse, serializer);
        });
    }

    /**
     * 响应方法
     * @param request HTTP 请求对象
     * @param response rpc 响应对象
     * @param serializer 序列化器
     */
    void doResponse(HttpServerRequest request, RpcResponse response, Serializer serializer){
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");

        try{
            //序列化
            byte[] serialized = serializer.serializer(response);
            httpServerResponse.end(Buffer.buffer(serialized));
        }catch (IOException e){
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
