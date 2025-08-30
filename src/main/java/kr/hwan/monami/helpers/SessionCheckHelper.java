package kr.hwan.monami.helpers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 로그인 체크를 수행해야 하는 페이지인지 여부를 설정하는 어노테이션 -> 어노테이션을 직접 생성
 * -> 이 어노테이션이 적용된 메서드는 세션 체크를 수행함
 *
 * enable = true --> 로그인을 해야만 접근 가능한 페이지
 * enable = false --> 로그인을 하지 않아야만 접근 가능한 페이지
 * 로그인에 관계없이 수행되는 메서드라면 해당 어노테이션 적용하지 않음.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionCheckHelper {
    boolean enable() default true;
}
