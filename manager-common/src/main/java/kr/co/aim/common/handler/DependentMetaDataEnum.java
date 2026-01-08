package kr.co.aim.common.handler;

public interface DependentMetaDataEnum extends MetaDataEnum {

    /**
     * 자신(자식)의 부모 Enum을 반환합니다.
     * @return MetaDataEnum (부모 Enum)
     */
    MetaDataEnum getParent();
}