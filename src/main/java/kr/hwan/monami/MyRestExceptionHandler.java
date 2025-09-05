package kr.hwan.monami;

import kr.hwan.monami.exceptions.MyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 에러 발생 시 에러 내용을 JSON으로 처리하는 역할
 */
@Slf4j
@RestControllerAdvice
public class MyRestExceptionHandler {

    private static final Map<String, HttpStatus> EXCEPTION_STATUS_MAP = new HashMap<>();

    static {
        // 잘못된 요청에 대한 예외 처리 -> 예: 잘못된 파라미터 값, 유효하지 않은 값 전달
        EXCEPTION_STATUS_MAP.put("IllegalArgumentException", HttpStatus.BAD_REQUEST);

        // 요청 파라미터 누락 -> 예: 필수 쿼리 파라미터 또는 폼 파라미터가 없을 때
        EXCEPTION_STATUS_MAP.put("MissingServletRequestParameterException", HttpStatus.BAD_REQUEST);

        // 메서드 파라미터 타입 불일치 -> 예: int 타입에 문자 전달, enum 타입에 잘못된 값 전달
        EXCEPTION_STATUS_MAP.put("MethodArgumentTypeMismatchException", HttpStatus.BAD_REQUEST);

        // JSON 파싱 실패 -> 예: 잘못된 JSON 포맷, 타입 불일치, 바인딩 오류
        EXCEPTION_STATUS_MAP.put("HttpMessageNotReadableException", HttpStatus.BAD_REQUEST);

        // 지원하지 않는 HTTP 메서드 사용 -> 예: GET만 지원하는 엔드포인트에 POST 요청
        EXCEPTION_STATUS_MAP.put("HttpRequestMethodNotSupportedException", HttpStatus.METHOD_NOT_ALLOWED);

        // 지원하지 않는 Content-Type -> 예: application/json만 받는데 application/xml로 요청
        EXCEPTION_STATUS_MAP.put("HttpMediaTypeNotSupportedException", HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        // Accept 헤더로 지원하지 않는 미디어 타입 요청 -> 예: 서버가 text/plain만 제공하는데 application/json 요청
        EXCEPTION_STATUS_MAP.put("HttpMediaTypeNotAcceptableException", HttpStatus.NOT_ACCEPTABLE);

        // 인증은 되었으나 접근 권한이 없을 때 -> 예: ROLE_USER가 접근 불가능한 관리자 페이지 요청
        EXCEPTION_STATUS_MAP.put("AccessDeniedException", HttpStatus.FORBIDDEN);

        // 인증이 되지 않았을 때 -> 예: JWT 토큰 없거나 만료되어 요청 시
        EXCEPTION_STATUS_MAP.put("AuthenticationException", HttpStatus.UNAUTHORIZED);

        // 매핑되는 핸들러(컨트롤러)가 없을 때 -> 예: 잘못된 URL로 접근
        EXCEPTION_STATUS_MAP.put("NoHandlerFoundException", HttpStatus.NOT_FOUND);

        // 이미 존재하는 엔티티 생성 시 충돌 -> 예: 이미 존재하는 이메일로 회원가입 시도
        EXCEPTION_STATUS_MAP.put("EntityExistsException", HttpStatus.CONFLICT);

        // 데이터 무결성 제약 조건 위반 -> 예: UNIQUE, FOREIGN KEY 위반
        EXCEPTION_STATUS_MAP.put("DataIntegrityViolationException", HttpStatus.CONFLICT);

        // Bean Validation 유효성 검사 실패 -> 예: @NotNull, @Size 검사 실패
        EXCEPTION_STATUS_MAP.put("ConstraintViolationException", HttpStatus.UNPROCESSABLE_ENTITY);

        // NullPointerException 발생 시 -> 예: 의도하지 않게 null 접근
        EXCEPTION_STATUS_MAP.put("NullPointerException", HttpStatus.INTERNAL_SERVER_ERROR);

        // 서비스 사용 불가 -> 예: 유지보수 중, 서버 과부하
        EXCEPTION_STATUS_MAP.put("ServiceUnavailableException", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * 직접 만든 Exception들은 프로그램적으로는 에러가 아니지만, 논리적인 흐름으로 봤을 때 진행을 시키면 안되는 경우임.
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> myExceptionHandler(
            Exception e,
            WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;

        /** 기본 상태 코드는 INTERNAL_SERVER_ERROR로 설정 */
        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

        if (e instanceof MyException myException) { // 이 클래스에 전달된 에러 객체가 MyException 객체의 유형이라면,
            // MyException을 상속받은 예외는 클래스에 정의되어 있는 값으로 상태 코드를 설정
            status = myException.getStatus().value(); // 에러 클래스에 정의된 값 뽑아서 사용.
        } else {
            // 그 외의 예외는 EXCEPTION_STATUS_MAP에서 매핑된 상태 코드로 설정
            // 매핑되지 않은 예외는 INTERNAL_SERVER_ERROR로 처리
            status = EXCEPTION_STATUS_MAP.getOrDefault(
                    e.getClass().getSimpleName(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).value();
        }

        log.error("========== Http {} Error =========", status);
        log.error(e.getMessage(), e);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("error", e.getClass().getSimpleName());
        result.put("message", e.getMessage() != null ? e.getMessage() : HttpStatus.INTERNAL_SERVER_ERROR);
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("path", servletWebRequest.getRequest().getRequestURI());

        return ResponseEntity.status(status).body(result);
    }
}