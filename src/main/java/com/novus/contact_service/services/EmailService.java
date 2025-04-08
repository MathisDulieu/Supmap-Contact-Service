package com.novus.contact_service.services;

import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    public void processSupportEmail(KafkaMessage kafkaMessage) {
        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String email = request.get("email");
            String subject = request.get("subject");
            String content = request.get("content");

            log.info("Processing support email request from: {}", email);

            log.info("Support email successfully sent to team from: {}", email);
        } catch (Exception e) {
            log.error("Error processing support email: {}", e.getMessage(), e);
        }
    }
}