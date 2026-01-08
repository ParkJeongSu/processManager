package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventName implements MetaDataEnum {
    CREATED("Created"),
    REMOVED("Removed"),
    UPDATED("Updated");

    private final String value;
}
