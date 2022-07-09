package com.example.hibernatedemo;

import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.spi.PersistEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDefaultPersistEventListener extends DefaultPersistEventListener {
    private static Logger LOGGER = LoggerFactory.getLogger(PreInsertInterceptor.class);

    @Override
    public void onPersist(PersistEvent event) throws HibernateException {
        LOGGER.info("Saving entity {} with state {}",
                event.getEntityName(),
                event.getObject());
        super.onPersist(event);
    }
}
