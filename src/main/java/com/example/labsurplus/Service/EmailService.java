package com.example.labsurplus.Service;

import com.example.labsurplus.Model.Lab;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    // [تعديل] لو فشل الإرسال (سيرفر الإيميل طايح، إيميل غلط...) ينسجل في اللوق بس،
    // وما يرمي exception يخرّب العملية الأساسية (موافقة، استلام...)
    public void sendEmail(String to, String subject, String text) {
        if (to == null || to.isBlank())
            return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Email to {} failed: {}", to, e.getMessage());
        }
    }

    // [جديد] يرسل لإيميل المختبر لو المختبر موجود وعنده إيميل
    public void notifyLab(Lab lab, String subject, String text) {
        if (lab != null)
            sendEmail(lab.getEmail(), subject, text);
    }
}
