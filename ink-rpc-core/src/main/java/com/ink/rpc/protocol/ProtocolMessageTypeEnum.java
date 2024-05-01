package com.ink.rpc.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
@AllArgsConstructor
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    public static ProtocolMessageTypeEnum getEnumByKey(int key){
        for(ProtocolMessageTypeEnum anEnum: values()){
            if(anEnum.key == key){
                return anEnum;
            }
        }
        return null;
    }

}
