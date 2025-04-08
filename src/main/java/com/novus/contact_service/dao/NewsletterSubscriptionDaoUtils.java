package com.novus.contact_service.dao;

import com.novus.database_utils.NewsletterSubscription.NewsletterSubscriptionDao;
import com.novus.shared_models.common.NewsletterSubscription.NewsletterSubscription;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewsletterSubscriptionDaoUtils {

    private final NewsletterSubscriptionDao<NewsletterSubscription> newsletterSubscriptionDao;

    public NewsletterSubscriptionDaoUtils(MongoTemplate mongoTemplate) {
        this.newsletterSubscriptionDao = new NewsletterSubscriptionDao<>(mongoTemplate);
    }

    public void save(NewsletterSubscription newsletterSubscription) {
        newsletterSubscriptionDao.save(newsletterSubscription);
    }

}
