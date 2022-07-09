package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.hibernatedemo.model.Account;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class BatchingTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenBatchingIsEnabledThenInsertCommandsAreSentToDBInBatches() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.unwrap(Session.class)
                .setJdbcBatchSize(3);
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            accounts.add(Utils.createAccount(i));
        }
        int batchSize = 3;
        Utils.doInTransaction(entityManager, em -> {
            for (int i = 0; i < accounts.size(); i++) {
                if (i > 0 && i % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
                em.persist(accounts.get(i));
            }
        });
        // look at the logs. Insert commands are batched into batches size of 3
        assertEquals(15L, accounts.get(14).getId());
    }

    @Test
    public void whenScrollIsUsedThenUpdateCommandsAreSentToDBInBatches() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            for (int i = 0; i < 10; i++) {
                em.persist(Utils.createAccount(i));
            }
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager2, em -> {
            int batchSize = 3;
            Session session = em.unwrap(Session.class);
            session.setJdbcBatchSize(batchSize);
            ScrollableResults scroll = session.createQuery("select a from Account a")
                    .setCacheMode(CacheMode.IGNORE)
                    .scroll(ScrollMode.FORWARD_ONLY);
            int count = 0;
            while (scroll.next()) {
                Account account = (Account) scroll.get(0);
                String newOwner = "NEW" + account.getOwner();
                account.setOwner(newOwner);
                if (++count % batchSize == 0) {
                    em.flush();
                    em.clear();
                }
            }
        });
        EntityManager entityManager3 = entityManagerFactory.createEntityManager();
        List<Account> accounts = entityManager3
                .createQuery("select a from Account a where a.owner like 'NEW%'", Account.class)
                .getResultList();
        // look at the logs. Updates are performed in batches of size 3
        assertEquals(10, accounts.size());
    }

    @TestConfiguration
    public static class BatchingTestsConfiguration {
        @Bean
        public static BeanPostProcessor datasourceProxyBeanPostProcessor() {
            return new DatasourceProxyBeanPostProcessor();
        }
    }
}
