package kr.co.aim.common.enums;

import kr.co.aim.common.handler.MetaDataEnum;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProcessState implements MetaDataEnum {
    DOWN("Down"),
    STARTING("Starting"),
    RUNNING("Running"),
    STOPPING("Stopping");
    private final String value;
}
