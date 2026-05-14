package kr.co.aim.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageList {
    CONNECTION_CHECK("ConnectionCheck"),
    CONNECTION("Connection");


    private final String messageName;
}