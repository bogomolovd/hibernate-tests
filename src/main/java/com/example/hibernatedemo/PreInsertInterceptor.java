package com.example.hibernatedemo;

import java.io.Serializable;
import java.util.Arrays;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.hibernatedemo.model.Account;

public class PreInsertInterceptor extends EmptyInterceptor {
    private static Logger LOGGER = LoggerFactory.getLogger(PreInsertInterceptor.class);

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof Account) {
            Account account = (Account) entity;
            account.setOwner("customOwner");
            LOGGER.info("Set custom owner to account {}#{}",
                entity.getClass().getSimpleName(),
                id);
        }
        LOGGER.info("Saving entity {}#{} with state {}",
                entity.getClass().getSimpleName(),
                id,
                Arrays.toString(state));
        return super.onSave(entity, id, state, propertyNames, types);
    }

}
