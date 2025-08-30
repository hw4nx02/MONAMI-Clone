package kr.hwan.monami.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 문자열 형식이 잘못된 경우에 대한 예외 처리 클래스.
 * 예를 들어, 이메일 형식이 잘못되었거나 날짜 형식이 올바르지 않은 경우 발생한다.
 * 이 예외는 클라이언트의 입력값이 올바르지 않기 때문에 잘못된 요청임을 알리는 데 사용된다.
 */
public class StringFormatException extends MyException {
    public StringFormatException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}