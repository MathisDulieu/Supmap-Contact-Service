package com.novus.contact_service.dao;

import com.novus.database_utils.NewsletterSubscription.NewsletterSubscriptionDao;
import com.novus.shared_models.common.NewsletterSubscription.NewsletterSubscription;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class NewsletterSubscriptionDaoUtils {

    private final NewsletterSubscriptionDao<NewsletterSubscription> newsletterSubscriptionDao;

    public NewsletterSubscriptionDaoUtils(MongoTemplate mongoTemplate) {
        this.newsletterSubscriptionDao = new NewsletterSubscriptionDao<>(mongoTemplate);
    }

    public void save(NewsletterSubscription newsletterSubscription) {
        newsletterSubscriptionDao.save(newsletterSubscription);
    }

    public Optional<NewsletterSubscription> findSubscriptionByEmail(String email) {
        return newsletterSubscriptionDao.findByEmail(email, NewsletterSubscription.class);
    }

    public List<NewsletterSubscription> findAllActiveSubscriptions() {
        return newsletterSubscriptionDao.findAllActiveSubscriptions(NewsletterSubscription.class);
    }
}
