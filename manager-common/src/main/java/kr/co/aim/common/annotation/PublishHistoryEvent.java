package kr.co.aim.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메소드에 붙일 것
@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
public @interface PublishHistoryEvent {
}