package com.novus.contact_service.services;

import com.novus.contact_service.UuidProvider;
import com.novus.contact_service.configuration.DateConfiguration;
import com.novus.contact_service.dao.NewsletterDaoUtils;
import com.novus.contact_service.dao.NewsletterSubscriptionDaoUtils;
import com.novus.contact_service.dao.UserDaoUtils;
import com.novus.contact_service.utils.LogUtils;
import com.novus.shared_models.common.Kafka.KafkaMessage;
import com.novus.shared_models.common.Log.HttpMethod;
import com.novus.shared_models.common.Log.LogLevel;
import com.novus.shared_models.common.Newsletter.Newsletter;
import com.novus.shared_models.common.Newsletter.NewsletterType;
import com.novus.shared_models.common.NewsletterSubscription.NewsletterSubscription;
import com.novus.shared_models.common.User.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.novus.contact_service.services.EmailService.getEmailSignature;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {

    private final NewsletterDaoUtils newsletterDaoUtils;
    private final NewsletterSubscriptionDaoUtils newsletterSubscriptionDaoUtils;
    private final EmailService emailService;
    private final LogUtils logUtils;
    private final UuidProvider uuidProvider;
    private final UserDaoUtils userDaoUtils;
    private final DateConfiguration dateConfiguration;

    public void processSubscription(KafkaMessage kafkaMessage) {
        Map<String, String> request = kafkaMessage.getRequest();
        String email = request.get("email");
        String userId = request.get("userId");

        try {
            NewsletterSubscription subscription = NewsletterSubscription.builder()
                    .id(uuidProvider.generateUuid())
                    .email(email)
                    .userId(userId)
                    .isActive(true)
                    .subscribedAt(dateConfiguration.newDate())
                    .build();

            newsletterSubscriptionDaoUtils.save(subscription);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "NEWSLETTER_SUBSCRIPTION_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    "User successfully subscribed to newsletter: " + email,
                    HttpMethod.POST,
                    "/contact/subscribe-newsletter",
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
                    "NEWSLETTER_SUBSCRIPTION_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing newsletter subscription for: " + email + ", error: " + e.getMessage(),
                    HttpMethod.POST,
                    "/contact/subscribe-newsletter",
                    "contact-service",
                    stackTrace,
                    userId
            );
            throw new RuntimeException("Failed to process newsletter subscription: " + e.getMessage(), e);
        }
    }

    public void processUnsubscription(KafkaMessage kafkaMessage) {
        Map<String, String> request = kafkaMessage.getRequest();
        String email = request.get("email");
        String userId = request.get("userId");
        String reason = request.get("reason");

        try {
            Optional<NewsletterSubscription> optionalNewsletterSubscription = newsletterSubscriptionDaoUtils.findSubscriptionByEmail(email);

            if (optionalNewsletterSubscription.isEmpty()) {
                logUtils.buildAndSaveLog(
                        LogLevel.WARN,
                        "NEWSLETTER_UNSUBSCRIPTION_NOT_FOUND",
                        kafkaMessage.getIpAddress(),
                        "No active subscription found for email: " + email,
                        HttpMethod.POST,
                        "/contact/unsubscribe-newsletter",
                        "contact-service",
                        null,
                        userId
                );
                throw new RuntimeException("No active subscription found for email: " + email);
            }

            NewsletterSubscription newsletterSubscription = optionalNewsletterSubscription.get();

            newsletterSubscription.setActive(false);
            newsletterSubscription.setUnsubscribedAt(dateConfiguration.newDate());
            newsletterSubscription.setUnsubscribeReason(reason);

            newsletterSubscriptionDaoUtils.save(newsletterSubscription);

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "NEWSLETTER_UNSUBSCRIPTION_SUCCESS",
                    kafkaMessage.getIpAddress(),
                    "User successfully unsubscribed from newsletter: " + email,
                    HttpMethod.POST,
                    "/contact/unsubscribe-newsletter",
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
                    "NEWSLETTER_UNSUBSCRIPTION_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing newsletter unsubscription for: " + email + ", error: " + e.getMessage(),
                    HttpMethod.POST,
                    "/contact/unsubscribe-newsletter",
                    "contact-service",
                    stackTrace,
                    userId
            );
            throw new RuntimeException("Failed to process newsletter unsubscription: " + e.getMessage(), e);
        }
    }

    public void processSendNewsletter(KafkaMessage kafkaMessage) {
        String userId = kafkaMessage.getAuthenticatedUser().getId();
        try {
            Map<String, String> request = kafkaMessage.getRequest();
            String subject = request.get("subject");
            String content = request.get("content");

            Newsletter newsletter = Newsletter.builder()
                    .id(uuidProvider.generateUuid())
                    .content(content)
                    .htmlContent(content)
                    .subject(subject)
                    .createdByUserId(userId)
                    .sentDate(dateConfiguration.newDate())
                    .type(NewsletterType.GENERAL)
                    .build();

            newsletterDaoUtils.save(newsletter);

            User user = kafkaMessage.getAuthenticatedUser();
            user.setLastActivityDate(dateConfiguration.newDate());

            userDaoUtils.save(user);

            List<NewsletterSubscription> activeSubscriptions = newsletterSubscriptionDaoUtils.findAllActiveSubscriptions();

            if (activeSubscriptions.isEmpty()) {
                logUtils.buildAndSaveLog(
                        LogLevel.WARN,
                        "NEWSLETTER_SEND_NO_SUBSCRIBERS",
                        kafkaMessage.getIpAddress(),
                        "No active subscribers found to send newsletter",
                        HttpMethod.POST,
                        "/private/admin/contact/send-newsletter",
                        "contact-service",
                        null,
                        userId
                );
                return;
            }

            int successCount = 0;
            int errorCount = 0;

            for (NewsletterSubscription subscription : activeSubscriptions) {
                try {
                    sendEmailToSubscriber(subscription.getEmail(), subject, content);

                    subscription.setLastNewsletterSentId(newsletter.getId());
                    subscription.setLastNewsletterSentDate(dateConfiguration.newDate());
                    newsletterSubscriptionDaoUtils.save(subscription);

                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    log.error("Failed to send newsletter to: {}, error: {}", subscription.getEmail(), e.getMessage());
                }
            }

            logUtils.buildAndSaveLog(
                    LogLevel.INFO,
                    "NEWSLETTER_SEND_COMPLETED",
                    kafkaMessage.getIpAddress(),
                    String.format("Newsletter sent to %d subscribers (%d failed)", successCount, errorCount),
                    HttpMethod.POST,
                    "/private/admin/contact/send-newsletter",
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
                    "NEWSLETTER_SEND_ERROR",
                    kafkaMessage.getIpAddress(),
                    "Error processing send newsletter: " + e.getMessage(),
                    HttpMethod.POST,
                    "/private/admin/contact/send-newsletter",
                    "contact-service",
                    stackTrace,
                    userId
            );
            throw new RuntimeException("Failed to process send newsletter: " + e.getMessage(), e);
        }
    }

    private void sendEmailToSubscriber(String email, String subject, String content) {
        try {
            content = content + getEmailSignature();
            emailService.sendEmail(email, subject, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send newsletter to " + email, e);
        }
    }

}
