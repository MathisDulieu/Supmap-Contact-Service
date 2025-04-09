package com.novus.contact_service.services;

import com.novus.contact_service.utils.LogUtils;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static com.novus.contact_service.services.EmailService.getEmailSignature;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailService {

    private final EmailService emailService;
    private final LogUtils logUtils;

    public void processSupportEmail(KafkaMessage kafkaMessage) {
        Map<String, String> request = kafkaMessage.getRequest();
        String email = request.get("email");
        String subject = request.get("subject");
        String content = request.get("content");
        String userId = request.get("userId");

        try {
            content = content + getEmailSignature();

            emailService.sendEmail(email, subject, content);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "SUPPORT_EMAIL_SENT",
                    kafkaMessage.getIpAddress(),
                    "Support email successfully sent to team from: " + email,
                    HttpMethod.POST,
                    "/contact/support",
                    "contact-service",
                    null,
                    userId
            );
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            logUtils.buildAndSaveLog(
                    LogLevel.ERROR,
                    "SUPPORT_EMAIL_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing support email from: " + email + ", error: " + e.getMessage(),
                    HttpMethod.POST,
                    "/contact/support",
                    "contact-service",
                    stackTrace,
                    userId
            );
            throw new RuntimeException("Failed to process support email: " + e.getMessage(), e);
        }
    }
}