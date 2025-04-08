package com.novus.contact_service.dao;

import com.novus.database_utils.Newsletter.NewsletterDao;
import com.novus.shared_models.common.Newsletter.Newsletter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewsletterDaoUtils {

    private final NewsletterDao<Newsletter> newsletterDao;

    public NewsletterDaoUtils(MongoTemplate mongoTemplate) {
        this.newsletterDao = new NewsletterDao<>(mongoTemplate);
    }

    public void save(Newsletter newsletter) {
        newsletterDao.save(newsletter);
    }

}
