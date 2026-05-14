package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SystemName implements MetaDataEnum {
    MNG("MNG"),
    WCS("WCS"),
    MANTI("MANTI"),
    EAS("EAS");
    private final String value;
}
