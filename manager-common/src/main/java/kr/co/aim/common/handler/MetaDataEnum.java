package kr.co.aim.common.handler;

public interface MetaDataEnum {
    String getValue(); // 프론트엔드에 'Label'로 표시될 값 (예: "Set")
    String name();     // 프론트엔드에 'Code' 또는 'Key'로 사용될 값 (예: "SET")
}
