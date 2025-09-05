package kr.hwan.monami.exceptions;

import org.springframework.http.HttpStatus;

/**
 * 서비스에서 조회, 입력, 수정, 삭제에 대한 결과가 없는 경우에 대한 예외 처리 클래스.
 * 예를 들어, 회원가입 후 저장된 데이터가 없거나 회원 정보 수정 등에서 수정할 대상이 없는 경우 발생한다.
 * 이 예외는 클라이언트에게 잘못된 요청임을 알리는 데 사용된다.
 */
public class ServiceNoResultException extends MyException {
    public ServiceNoResultException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}