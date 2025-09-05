package kr.hwan.monami.helpers;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class UtilHelper {
    public String getRandomString(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("길이는 1자리 이상이어야 합니다.");
        }

        // 랜덤 문자열을 생성하기 위한 문자 집합
        // --> a~z, A~Z, 0~9의 모든 문자
        final String DATA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        // SecureRandom은 보안적으로 안전한 난수 생성기
        // 해당 클래스는 암호학적으로 안전한 난수를 생성하는 데 사용된다.
        // SecureRandom은 Java에서 제공하는 기본 난수 생성기보다 더 강력한 난수 생성 알고리즘을 사용한다.
        // 이로 인해 예측 불가능한 난수를 생성하기에 보안이 중요한 경우 SecureRandom을 사용하는 것이 좋다.
        // --> import java.security.SecureRandom;
        SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // 0부터 DATA.length() - 1까지의 랜덤한 정수를 생성한다.
            int rnd = random.nextInt(DATA.length());

            // 생성된 랜덤한 정수를 인덱스로 사용하여 DATA에서 문자를 가져온다.
            // String.charAT() 메서드는 해당 인덱스의 문자를 반환한다.
            // 따라서, sb에 랜덤하게 선택된 문자를 추가한다.
            sb.append(DATA.charAt(rnd));
        }

        // 최종적으로 생선된 문자열을 반환
        // StringBuilder는 문자열을 효율적으로 조작할 수 있는 클래스
        // toString() 메서드를 호출하여 StringBuilder 객체를 String으로 변환한다.
        // 이 메서드는 StringBuilder에 저장된 모든 문자를 하나의 문자열로 결합한다.
        return sb.toString();
    }
}
