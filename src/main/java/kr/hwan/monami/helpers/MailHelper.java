package kr.hwan.monami.helpers;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class MailHelper {
    /**
     * 메일 발송을 위한 JavaMailSender 객체
     * --> import org.springframework.mail.javamail.JavaMailSender;
     */
    private JavaMailSender javaMailSender = null;

    /**
     * 보내는 사람의 이름과 이메일 주소 (환경설정 파일에서 읽어옴)
     */
    private String senderName = null;
    private String senderEmail = null;

    /**
     * 생성자
     *
     * `@Value` 어노테이션을 사용하여 환경설정 파일에서 보내는 사람의 이름과 이메일 주소를 읽어온다.
     * JavaMailSender 객체는 Spring의 의존성 주입을 통해 자동으로 주입된다.
     * 즉, 선언만 해놓으면 객체는 자동으로 Spring이 할당해주는 것.
     * --> import org.springframework.beans.factory.annotation.Value;
     * @param javaMailSender JavaMailSender 객체
     * @param senderName 보내는 사람 이름
     * @param senderEmail 보내는 사람 이메일 주소
     */
    @Autowired
    public MailHelper(JavaMailSender javaMailSender,
                      @Value("${mailhelper.sender.name}") String senderName,
                      @Value("${mailhelper.sender.email}") String senderEmail) {
        this.javaMailSender = javaMailSender;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
    }

    /**
     * 메일을 발송한다.
     *
     * @param receiverName 수신자 이름
     * @param receiverEmail 수신자 이메일 주소
     * @param subject 제목
     * @param content 내용
     *
     * @throws Exception
     */
    public void sendMail(String receiverName,
                         String receiverEmail,
                         String subject,
                         String content) throws Exception {
        /** 1) 메일 발송 정보 로그 확인 */
        log.debug("---------------------------------");
        log.debug(String.format("ReceiverName: %s", receiverName));
        log.debug(String.format("ReceiverEmail: %s", receiverEmail));
        log.debug(String.format("Subject: %s", subject));
        // 본문의 경우 출력량이 많아서 로그 띄우지 않았음.

        /** 2) Java Mail 라이브러리를 활용한 메일 발송 */
        // --> import jakarta.mail.internet.MimeMessage;
        MimeMessage message = javaMailSender.createMimeMessage();
        // --> import org.springframework.mail.javamail.MimeMessageHelper;
        MimeMessageHelper helper = new MimeMessageHelper(message);

        /** 2-1) 제목, 내용, 수신자 설정 */
        try {
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.setTo(new InternetAddress(receiverEmail, receiverName, StandardCharsets.UTF_8.name()));
            helper.setFrom(new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name()));

            /** 2-2) 메일 보내기 */
            javaMailSender.send(message);
        } catch (MessagingException e) {
            // 에러가 발생했음을 사용자에게 alert로 알리고 전 페이지로 이동하는 처리가 필요
            log.error("메일 발송 정보 설정 실패");
            throw e;
        } catch (UnsupportedEncodingException e) {
            log.error("지원하지 않는 인코딩");
            throw e;
        } catch (Exception e) {
            // 알 수 없는 오류가 발생했음을 사용자에게 alert로 알리고 전 페이지로 이동하는 처리가 필요
            log.error("알 수 없는 오류", e);
            throw e;
        }
    }

    /**
     * 메일을 발송한다.
     *
     * @param receiverEmail 수신자 이메일 주소
     * @param subject 제목
     * @param content 내용
     * @throws Exception
     */
    public void sendMail(String receiverEmail, String subject, String content) throws Exception{
        this.sendMail(null, receiverEmail, subject, content);
}


}
