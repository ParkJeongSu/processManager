package kr.co.aim.common.handler;

import java.util.List;

public interface ParentMetaDataEnum extends MetaDataEnum {

    /**
     * 자신(자식)의 부모 Enum을 반환합니다.
     * @return MetaDataEnum (부모 Enum)
     */
    List<MetaDataEnum> getChildList();
}