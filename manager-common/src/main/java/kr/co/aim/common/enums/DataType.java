package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataType implements MetaDataEnum {
    DATE("DATE"),
    NUMBER("NUMBER"),
    STRING("STRING");
    private final String value;
}
