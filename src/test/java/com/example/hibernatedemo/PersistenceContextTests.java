package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.Account;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class PersistenceContextTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private AccountDAO accountDAO;

    @Test
    public void whenEntityPersistedIdIsGenerated() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        Utils.doInTransaction(entityManager, em -> em.persist(account));
        assertEquals(1L, account.getId());
    }

    @Test
    public void whenEntityPersistedWithTransactionalIdIsGenerated() {
        Account account = Utils.createAccount(1);
        accountDAO.saveAccount(account);
        assertEquals(1L, account.getId());
    }

    @Test
    public void whenEntityUpdatedWithTransactionalSaveIsNotNeccessary() {
        Account account = Utils.createAccount(1);
        accountDAO.saveAccount(account);
        BigDecimal newBalance = BigDecimal.valueOf(5000);
        accountDAO.updateAccountById(1L, newBalance);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account updatedAccount = entityManager.find(Account.class, 1L);
        assertEquals(BigDecimal.valueOf(5000L), updatedAccount.getBalance());
    }

    @Test
    public void whenEntityRemovedWithTransactionalItIsGone() {
        Account account = Utils.createAccount(1);
        accountDAO.saveAccount(account);
        assertEquals(1L, account.getId());
        accountDAO.removeAccount(account);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account removedAccount = entityManager.find(Account.class, 1L);
        assertNull(removedAccount);
    }

    @Test
    public void whenFilterIsAppliedThenFilteredEntitiesNotReturned() {
        Account account = Utils.createAccount(1);
        Account account2 = Utils.createAccount(2);
        account2.setActive(false);
        accountDAO.saveAccount(account);
        accountDAO.saveAccount(account2);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Account> allAccounts = entityManager.unwrap(Session.class)
                .byMultipleIds(Account.class)
                .multiLoad(1L, 2L);
        assertEquals(2L, allAccounts.size());
        entityManager.unwrap(Session.class)
                .enableFilter("activeAccount")
                .setParameter("active", true);
        List<Account> filteredAccounts = entityManager.createQuery("select a from Account a", Account.class)
                .getResultList();
        assertEquals(1L, filteredAccounts.size());
    }

    @Test
    public void whenEntityUpdatedWithDynamicUpdateThenOnlyChangedColumnsArePresentInSql() {
        Account account = Utils.createAccount(1);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
            account.setBalance(BigDecimal.valueOf(1000L));
        });
        Account updatedAccount = entityManager.find(Account.class, 1L);
        assertEquals(BigDecimal.valueOf(1000L), updatedAccount.getBalance());
    }

    @Test
    public void whenEntityRefreshedThenItsValuesUpdated() {
        Account account = Utils.createAccount(1);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
            em.createNativeQuery("update account set balance = 1000 where id = 1")
                    .executeUpdate();
        });
        assertEquals(BigDecimal.valueOf(1L), account.getBalance());
        entityManager.refresh(account);
        assertEquals(BigDecimal.valueOf(1000L), account.getBalance());
    }

    @Test
    public void whenEntityMergedItIsInPersistenceContextAgain() {
        Account account = Utils.createAccount(1);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            entityManager.persist(account);
        });
        assertTrue(entityManager.contains(account));
        // the same effect for .clear() and .close()
        entityManager.detach(account);
        assertFalse(entityManager.contains(account));
        Account merged = Utils.doInTransactionAndReturn(entityManager, em -> {
            Account mergedAcc = entityManager.merge(account);
            mergedAcc.setBalance(BigDecimal.valueOf(1000L));
            return mergedAcc;
        });
        assertTrue(entityManager.contains(merged));
    }

    @Test
    public void whenEntityIsFoundByRepositoryItDoesntExistInPersistenceContext() {
        Account account = Utils.createAccount(1);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            entityManager.persist(account);
        });
        entityManager.clear();
        Utils.doInTransaction(entityManager, em -> {
            Account foundAccount = accountDAO.findAccount(1L);
            // foundAccount = em.merge(foundAccount); - not in persistence context until merge
            foundAccount.setBalance(BigDecimal.valueOf(1234L));
        });
        entityManager.clear();
        Account updatedAccount = entityManager.find(Account.class, 1L);
        // the value is old as the account wasn't merged before setting new balance
        assertEquals(BigDecimal.valueOf(1), updatedAccount.getBalance());
    }
}
