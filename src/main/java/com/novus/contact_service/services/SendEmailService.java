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
            content = buildSupportConfirmationEmail(subject, content);
            subject = "Thanks for Contacting SupMap Support";

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

    public String buildSupportConfirmationEmail(String subject, String messageContent) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Support Request Confirmation</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">\n" +
                "    <div style=\"background-color: #f9f9f9; padding: 20px; border-radius: 5px; border-left: 4px solid #4285f4;\">\n" +
                "        <h2 style=\"color: #4285f4; margin-top: 0;\">Your Support Request Has Been Received</h2>\n" +
                "        <p>Thank you for contacting SupMap Support. We have received your message and will get back to you as soon as possible.</p>\n" +
                "        <p>Here's a summary of your request:</p>\n" +
                "        <div style=\"background-color: #fff; padding: 15px; border-radius: 5px; margin: 15px 0; border: 1px solid #ddd;\">\n" +
                "            <p><strong>Subject:</strong> " + subject + "</p>\n" +
                "            <p><strong>Your message:</strong></p>\n" +
                "            <p style=\"background-color: #f0f0f0; padding: 10px; border-radius: 5px;\">" + messageContent + "</p>\n" +
                "        </div>\n" +
                "        <p>Our team will review your request and respond as quickly as possible. Most inquiries are answered within 24 hours during business days.</p>\n" +
                "        <p>If you have any additional information to add to your request, please reply to this email.</p>\n" +
                "    </div>\n" +
                "    <div style=\"margin-top: 30px; font-size: 14px; color: #666; border-top: 1px solid #ddd; padding-top: 20px;\">\n" +
                "        <p>Best regards,<br>\n" +
                "        The SupMap Team</p>\n" +
                "        <div style=\"margin-top: 15px;\">\n" +
                "            <p>SupMap - Simplify your routes and projects.</p>\n" +
                "            <p>📞 Support: <a href=\"tel:+33614129625\" style=\"color: #4285f4; text-decoration: none;\">+33 6 14 12 96 25</a><br>\n" +
                "            📩 Email: <a href=\"mailto:supmap.application@gmail.com\" style=\"color: #4285f4; text-decoration: none;\">supmap.application@gmail.com</a><br>\n" +
                "            🌐 Website: <a href=\"https://supmap-application.com\" style=\"color: #4285f4; text-decoration: none;\">https://supmap-application.com</a><br>\n" +
                "            📱 Available on iOS and Android!</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}