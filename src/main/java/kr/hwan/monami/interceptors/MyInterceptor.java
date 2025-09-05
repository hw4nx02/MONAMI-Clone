package kr.hwan.monami.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.hwan.monami.helpers.SessionCheckHelper;
import kr.hwan.monami.helpers.WebHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import ua_parser.Client;
import ua_parser.Parser;

import java.nio.file.AccessDeniedException;
import java.util.Map;

@Slf4j // 로그
@Component // @Controller, @Service 상위의 클래스. SpringBoot가 요구하는 부속품임을 나타내는 어노테이션
@RequiredArgsConstructor
public class MyInterceptor implements HandlerInterceptor {
    /** 페이지 실행 시간 관련 변수 */
    long startTime = 0; // 페이지의 실행 시작 시각을 저장할 변수
    long endTime = 0; // 페이지의 실행 완료 시각을 저장할 변수

    /** WebHelper 객체 */
    private final WebHelper webHelper;

    /**
     * Controller 실행 전에 수행되는 메서드
     * 클라이언트(웹 브라우저)의 요청을 Controller에 전달하기 전에 호출
     * 보통 이곳에서 각종 체크 작업과 로그를 기록하는 작업을 진행
     * @param request 요청 객체
     * @param response 응답 객체
     * @param handler
     * @return boolean 값을 전달하는데, false의 경우, controller를 실행시키지 않고 요청을 종료한다.
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        log.info("preHandle가 실행됨");
        // 객체 주입 받으므로써 사용할 필요가 없어진 부분
//        webHelper = new WebHelper(request); // WebHelper는 request 객체가 필요하기 때문에, Interceptor 내부에 할당함.

        log.info("----------- new Client connect ---------");

        /** 1) 페이지의 실행 시작 시간 */
        startTime = System.currentTimeMillis();

        /** 2) 접속한 클라이언트 정보 확인하기 */
        String ua = request.getHeader("user-agent"); // request에서 헤더 값 중 user-agent 값 가져오기.
        Parser uaParser = new Parser(); // Parser 객체 할당.
        Client c = uaParser.parse(ua); //

        String fmt = "[Client] %s, %s, %s %s, %s %s";

        String ipAddr = webHelper.getClientIp();
        String osVersion = c.os.major + (c.os.major != null ? "." + c.os.major : "");
        String uaVersion = c.userAgent.major + (c.userAgent.minor != null ? "." + c.userAgent.minor : "");
        String clientInfo = String.format(fmt, ipAddr, c.device.family, c.os.family, osVersion, c.userAgent.family, uaVersion);

        log.info(clientInfo);

        /** 3) 클라이언트의 요청 정보(URL) 확인하기 */
        // 현재 URL 획득
        String url = request.getRequestURL().toString();

        // GET 방식인지, POST 방식인지 조회
        String methodName = request.getMethod();

        // URL에서 "?"이후에 전달되는 GET 파라미터 문자열을 모두 가져온다.
        String queryString = request.getQueryString();

        // 가져온 값이 있다면 URL과 결합하여 완전한 URL을 구성한다.
        if (queryString != null) {
            url = url + "?" + queryString;
        }

        // 획득한 정보를 로그로 표시
        log.info(String.format("[%s] %s", methodName, url));

        /** 4) 클라이언트가 전달한 모든 파라미터 확인하기 */
        Map<String, String[]> params = request.getParameterMap();

        for (String key : params.keySet()) {
            String[] value = params.get(key);
            log.info(String.format("(param) <-- %s = %s", key, String.join(",", value)));
        }

        /** 5) 클라이언트가 머물렀던 이전 페이지 확인 */
        String refer = request.getHeader("refer");

        // 이전에 머물렀던 페이지가 존재한다면?
        // --> 직전 종료시간과 이번 접속의 시작시간과의 차이는 이전 페이지에 머문 시간을 의미한다.
        if (refer != null && endTime > 0) {
            log.info(String.format("- REFER : time=%d, url=%s", startTime - endTime, refer));
        }

        /** 6) 로그인 여부에 따른 페이지 접근 제어 */
        // @SessionCheckHelper 어노테이션이 붙은 메서드에 대해서만 세션 체크를 수행한다.
        if (handler instanceof HandlerMethod handlerMethod) {
            // 세션 검사용 어노테이션을 가져온다.
            SessionCheckHelper annotation = handlerMethod.getMethodAnnotation(SessionCheckHelper.class);

            // 어노테이션이 존재한다면 세션 체크를 수행한다.
            if (annotation != null) {
                // 컨트롤러 유형을 가져온다.
                Class<?> beanType = handlerMethod.getBeanType();
                // Restful 방식의 컨트롤러인지 검사
                boolean isRestful = beanType.isAnnotationPresent(RestController.class);

                // 세션 검사 여부를 결정하는 enable 속성을 가져온다.
                // enable이 true인 경우 세션이 있어야 접근 가능하고,
                // false인 경우 세션이 없어야 접근 가능하다.
                boolean enable = annotation.enable();

                // 로그인 여부를 체크한다.
                HttpSession session = request.getSession();
                boolean isLoggedIn = session != null && session.getAttribute("memberInfo") != null;

                if (enable) {           // 로그인 중에만 접근 가능한 페이지 검사
                    if (!isLoggedIn) {  // 로그인을 하지 않은 상태라면? -> 로그인 상태라면 굳이 인터셉트해서 메서드 수행을 막을 필요가 없음
                        if (isRestful) { // RestController라면 -> 메서드 수행하지 않음
                            throw new AccessDeniedException("로그인이 필요합니다.");
                        } else { // RestController가 아닌 일반 Controller라면, 페이지를 리디렉션
                            response.setStatus(403);
                            response.sendRedirect(request.getContextPath() + "/account/login");
                        }

                        return false;
                    }
                } else {                // 로그인하지 않은 상태에서만 접근 가능한 페이지 검사
                    if (isLoggedIn) {   // 로그인을 한 상태라면?
                        if (isRestful) {
                            throw new AccessDeniedException("로그인 중에는 접근할 수 없습니다.");
                        } else {
                            response.setStatus(403);
                            response.sendRedirect(request.getContextPath() + "/account/login");
                        }
                        return false;
                    }
                }
            }
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    /**
     * view 단으로 forward 되기 전에 수행.
     * 컨트롤러 로직이 실행된 이후 호출된다.
     * 컨트롤러 단에서 에러 발생 시 해당 메서드는 수행되지 않는다.
     * request로 넘어온 데이터 가공 시 많이 사용된다.
     */
    // controller 실행에서 에러가 발생하면 해당 메서드 실행 X. 보통 request 객체를 받아서 사용.
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //log.debug("MyInterceptor.postHandle 실행됨");

        /** 1) 컨트롤러의 실행 종료 시각을 가져온다. */
        endTime = System.currentTimeMillis();

        /** 2) 컨트롤러가 실행하는데 걸린 시각을 구한다. */
        log.info(String.format("running time: %d(ms)", endTime-startTime));

        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 컨트롤러 종료 후 view가 정상적으로 랜더링 된 후 제일 마지막에 실행이 되는 메서드.
     * (잘 사용 안함)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion가 실행됨");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
