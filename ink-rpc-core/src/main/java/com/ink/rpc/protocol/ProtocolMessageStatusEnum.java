package com.ink.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
@AllArgsConstructor
public enum ProtocolMessageStatusEnum {

    OK("success", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;

    private final int value;

    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum protocolMessageStatusEnum : ProtocolMessageStatusEnum.values()) {
            if (protocolMessageStatusEnum.value == value) {
                return protocolMessageStatusEnum;
            }
        }
        return null;
    }
}
