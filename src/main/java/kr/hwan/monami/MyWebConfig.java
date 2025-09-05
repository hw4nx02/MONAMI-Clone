package kr.hwan.monami;

import kr.hwan.monami.interceptors.MyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 해당 클래스가 환경설정임을 알림.
@RequiredArgsConstructor
public class MyWebConfig implements WebMvcConfigurer {
    private final MyInterceptor myInterceptor; // MyInterceptor 객체 선언

    /** 업로드 된 파일이 저장될 경로 (application.properties로부터 읽어옴) */
    // --> import org.springframework.beans.factory.annotation.Value;
    @Value("${upload.dir}")
    private String uploadDir;

    /** 업로드 된 파일이 노출될 URL 경로 (application.properties로부터 읽어옴) */
    @Value("${upload.url}")
    private String uploadUrl;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration ir = registry.addInterceptor(myInterceptor);
        // 해당 경로는 인터셉터가 가로채지 않음.
        // 텍스트 파일이나 아이콘은 가로챌 필요 없으니까
        ir.excludePathPatterns("/error", "robots.txt", "/favicon.ico", "/assets/**");
    }

    /** 설정파일에 명시된 업로드 저장 경로와 URL상의 경로를 맵핑 시킴 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(String.format("%s/**", uploadUrl))
                .addResourceLocations(String.format("file://%s/", uploadDir));
    }
}
