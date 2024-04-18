package com.ink.rpc.model;

import com.ink.rpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RPC 请求对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {

    /**
     * 请求的服务名称
     */
    private String serviceName;

    /**
     * 请求的服务版本
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

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
