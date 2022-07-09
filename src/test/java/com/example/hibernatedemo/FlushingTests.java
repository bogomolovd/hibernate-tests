package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.Account;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class FlushingTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenChangesAreOverlappingThenTheyAreAvailableInDbWithoutFlush() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
            account.setBalance(BigDecimal.valueOf(1234L));
            // no flush needed, changes already propagated to db before selecting the same
            // entity
            // the same for querying overlapping data via hql and sql
            Account accountFromDB = em.find(Account.class, 1L);
            assertEquals(BigDecimal.valueOf(1234L), accountFromDB.getBalance());
        });
    }

    @Test
    public void whenChangesAreOverlappingAndFlushModeIsCommitThenTheyAreNotAvailableInDbWithoutFlush() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.setFlushMode(FlushModeType.COMMIT);
        Account account = Utils.createAccount(1);
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
            account.setBalance(BigDecimal.valueOf(1234L));
            List<Account> accountsFromDB = em
                .createQuery("select a from Account a where a.balance = 1234", Account.class)
                .getResultList();
            assertEquals(0, accountsFromDB.size());
            em.flush();
            accountsFromDB = em
                .createQuery("select a from Account a where a.balance = 1234", Account.class)
                .getResultList();
            assertEquals(1, accountsFromDB.size());
        });
    }
}
