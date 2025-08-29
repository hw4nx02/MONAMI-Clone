package kr.hwan.monami;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice()
public class MyApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 RestController의 응답을 처리하도록 true 반환
        return true;
    }

    /**
     * 응답 본문을 가공하는 메소드
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // JSON 응답인지 확인
        boolean isJsonResponse = selectedContentType != null && selectedContentType.includes(MediaType.APPLICATION_JSON);

        // HTTP 응답 상태 코드 확인
        int statusCode = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            statusCode = servletResponse.getServletResponse().getStatus();
        }

        // JSON 응답이 아니거나 상태 코드가 OK가 아닌 경우 원본 응답 반환
        if (!isJsonResponse || statusCode != HttpStatus.OK.value()) {
            return body;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "OK");

        // 컨트롤러의 응답 본문이 있을 경우 결과에 덧붙임
        if (body != null && body instanceof Map) {
            result.putAll((Map<String, Object>) body);
        }

        result.put("timestamp", LocalDateTime.now().toString());
        result.put("path", request.getURI().getPath());

        return result;
    }
}