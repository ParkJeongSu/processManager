package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurgeLogStatus implements MetaDataEnum {
    SUCCESS("SUCCESS"),
    FAIL("FAIL"),
    TIMEOUT("TIMEOUT");
    private final String value;
}
