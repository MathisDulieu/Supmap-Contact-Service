package com.novus.contact_service.services;

import com.novus.contact_service.dao.NewsletterDaoUtils;
import com.novus.contact_service.dao.NewsletterSubscriptionDaoUtils;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterDaoUtils newsletterDaoUtils;
    private final NewsletterSubscriptionDaoUtils newsletterSubscriptionDaoUtils;

    public void processSubscription(KafkaMessage kafkaMessage) {
        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String email = request.get("email");

            log.info("Processing newsletter subscription request for: {}", email);

            log.info("Successfully subscribed to newsletter: {}", email);
        } catch (Exception e) {
            log.error("Error processing newsletter subscription: {}", e.getMessage(), e);
        }
    }

    public void processUnsubscription(KafkaMessage kafkaMessage) {
        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String email = request.get("email");

            log.info("Processing newsletter unsubscription request for: {}", email);

            log.info("Successfully unsubscribed from newsletter: {}", email);
        } catch (Exception e) {
            log.error("Error processing newsletter unsubscription: {}", e.getMessage(), e);
        }
    }

    public void processSendNewsletter(KafkaMessage kafkaMessage) {
        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String subject = request.get("subject");
            String content = request.get("content");


        } catch (Exception e) {
            log.error("Error processing send newsletter: {}", e.getMessage(), e);
        }
    }

    private void sendEmailToSubscriber(String email, String subject, String content) {

    }

}
