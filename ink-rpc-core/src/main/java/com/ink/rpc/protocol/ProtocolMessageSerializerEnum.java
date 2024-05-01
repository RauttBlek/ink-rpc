package com.ink.rpc.protocol;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ProtocolMessageSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private final int key;

    private final String value;

    /**
     * 获取值列表
     *
     * @return 所有枚举的值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(e -> e.value).collect(Collectors.toList());
    }

    public static ProtocolMessageSerializerEnum getEnumByKey(int key) {
        for (ProtocolMessageSerializerEnum anEnum : values()) {
            if (anEnum.key == key) {
                return anEnum;
            }
        }
        return null;
    }

    public static ProtocolMessageSerializerEnum getEnumByValue(String value) {
        if (StrUtil.isEmpty(value)) {
            return null;
        }
        for (ProtocolMessageSerializerEnum anEnum : values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

}
