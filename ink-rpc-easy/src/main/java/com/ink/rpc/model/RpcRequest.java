package com.ink.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 请求方法类型
      */
    private String methodName;

    /**
     * 参数类型列表
     */
    private Class<?>[] paraType;

    /**
     * 参数列表
     */
    private Object[] paraArray;
}
