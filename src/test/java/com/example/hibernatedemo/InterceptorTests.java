package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.DebitAccount;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class InterceptorTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenEntityIsFlushedThenItIsLoggedByInterceptor() {
        SessionFactoryImpl factory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = factory.getServiceRegistry()
                .getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PERSIST)
                .prependListener(new MyDefaultPersistEventListener());
        Session session = factory.withOptions()
                // registering session-scoped interceptor
                .interceptor(new PreInsertInterceptor())
                .openSession();
        DebitAccount account = Utils.createDebitAccount(1);
        Utils.doInTransaction(session, em -> {
            em.persist(account);
        });
        // look at the logs. There are additional logs
        // from the interceptor and listener
        assertEquals(1L, account.getId());
    }
}
