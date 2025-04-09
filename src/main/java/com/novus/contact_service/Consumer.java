package com.novus.contact_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novus.contact_service.services.SendEmailService;
import com.novus.contact_service.services.NewsletterService;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class Consumer {

    private final SendEmailService sendEmailService;
    private final NewsletterService newsletterService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "contact-service", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuthenticationEvents(
            @Payload String messageJson,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("JSON message received from contact-service topic [key: {}, partition: {}, offset: {}]", key, partition, offset);

            KafkaMessage kafkaMessage = objectMapper.readValue(messageJson, KafkaMessage.class);

            handleOperation(key, kafkaMessage);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
            acknowledgment.acknowledge();
        }
    }

    private void handleOperation(String operationKey, KafkaMessage kafkaMessage) {
        log.info("Processing operation: {}", operationKey);

        switch (operationKey) {
            case "sendSupportEmail":
                sendEmailService.processSupportEmail(kafkaMessage);
                break;
            case "subscribeToNewsletter":
                newsletterService.processSubscription(kafkaMessage);
                break;
            case "unsubscribeFromNewsletter":
                newsletterService.processUnsubscription(kafkaMessage);
                break;
            case "sendNewsletter":
                newsletterService.processSendNewsletter(kafkaMessage);
                break;
            default:
                log.warn("Unknown operation: {}", operationKey);
        }
    }
}