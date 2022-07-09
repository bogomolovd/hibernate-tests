package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.Account;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class LockingTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenEntityIsSavedThenItsVersionIsIncreased() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
        });
        assertEquals(0L, account.getVersion());
        Utils.doInTransaction(entityManager, em -> {
            account.setBalance(BigDecimal.valueOf(1234L));
        });
        assertEquals(1L, account.getVersion());
    }

    @Test
    public void whenEntityWasModifiedByAnotherTransactionThenLockExceptionIsThrown() {
        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        EntityTransaction transaction1 = em1.getTransaction();
        transaction1.begin();
        em1.persist(account);
        transaction1.commit();
        Account account2 = em2.find(Account.class, 1L);
        EntityTransaction transaction2 = em1.getTransaction();
        transaction2.begin();
        account.setBalance(BigDecimal.valueOf(1234L));
        transaction2.commit();
        EntityTransaction transaction3 = em2.getTransaction();
        transaction3.begin();
        account2.setBalance(BigDecimal.valueOf(5000L));
        // OptimisticLockException is the cause
        assertThrows(RollbackException.class, () -> transaction3.commit());
    }

    @Test
    public void whenEntityLockedPessimisticalyThenItCantBeChangedInAnotherTransaction() {
        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        EntityTransaction transaction1 = em1.getTransaction();
        transaction1.begin();
        em1.persist(account);
        transaction1.commit();
        Account account2 = em2.find(Account.class, 1L);
        EntityTransaction transaction2 = em2.getTransaction();
        transaction2.begin();
        em2.lock(account2, LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        EntityTransaction transaction3 = em1.getTransaction();
        transaction3.begin();
        account.setBalance(BigDecimal.valueOf(1234L));
        // timeout exception on waiting lock
        assertThrows(RollbackException.class, () -> transaction3.commit());
    }
}
