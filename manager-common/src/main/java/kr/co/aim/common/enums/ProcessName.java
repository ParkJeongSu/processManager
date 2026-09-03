package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcessName implements MetaDataEnum {
    IO_CON("IOCon"),
    WCS("WCS");
    private final String value;
}
