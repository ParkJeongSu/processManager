package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SqlOperator implements MetaDataEnum {
    LT("<"),
    LE("<="),
    GT(">"),
    GE(">="),
    EQ("=");
    private final String value;
}
