package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.Account;
import com.example.hibernatedemo.model.CreditAccount;
import com.example.hibernatedemo.model.DebitAccount;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class InheritanceTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenAccountsAreSavedTheyCanBeFetchedViaPolymorphicQuery() {
        Account account = Utils.createAccount(1);
        DebitAccount debitAccount = Utils.createDebitAccount(2);
        CreditAccount creditAccount = Utils.createCreditAccount(3);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.merge(account);
            em.merge(debitAccount);
            em.merge(creditAccount);
        });
        account.setId(1L);
        debitAccount.setId(2L);
        creditAccount.setId(3L);
        List<Account> accounts = entityManager.createQuery("select a from Account a order by a.id", Account.class)
                .getResultList();
        assertEquals(account, accounts.get(0));
        assertEquals(debitAccount, accounts.get(1));
        assertEquals(creditAccount, accounts.get(2));
    }
}
