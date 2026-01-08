package kr.co.aim.api.service;

import kr.co.aim.common.handler.NotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service("MAIL")
@RequiredArgsConstructor
@Slf4j
public class MailSevice implements NotificationHandler {
    private final JavaMailSender mailSender;
    @Override
    public void send(String to, String from, String subject, String content) {
        // 메일 발송 로직
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(from);
        message.setSubject(subject);
        message.setText(content);
        //mailSender.send(message);
        log.info("mail Send Completed");
    }
}
