package com.ink.rpc.proxy;

import com.ink.rpc.config.RpcConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * MOCK 服务代理
 * 构建虚拟响应值并返回
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        log.info("mock invoke{}", method.getName());
        return getDefaultReturn(returnType);
    }

    /**
     * 获取指定类型的默认值
     * @param type 要求的返回值类型
     * @return 预设的默认值
     */
    private Object getDefaultReturn(Class<?> type){
        //基本类型判断
        if(type.isPrimitive()){
            if(type == int.class){
                return 0;
            } else if (type == short.class) {
                return (short)0;
            }else if(type == long.class){
                return 0L;
            }else if(type == boolean.class){
                return false;
            }
        }
        //对象类型
        return null;
    }
}
