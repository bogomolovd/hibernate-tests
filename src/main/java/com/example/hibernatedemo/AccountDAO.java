package com.example.hibernatedemo;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.hibernatedemo.model.Account;

@Service
public class AccountDAO {
    private final EntityManager entityManager;
    private final AccountRepository accountRepository;

    public AccountDAO(EntityManager entityManager, AccountRepository accountRepository) {
        this.entityManager = entityManager;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account findAccount(long id) {
        return accountRepository.findById(id).get();
    }

    @Transactional
    public void saveAccount(Account account) {
        entityManager.persist(account);
    }

    @Transactional
    public void updateAccountById(long id, BigDecimal newBalance) {
        Account account = accountRepository.findById(id).get();
        account.setBalance(newBalance);
    }

    @Transactional
    public void removeAccount(Account account) {
        // merge account to add it to current persistence context
        Account merged = entityManager.merge(account);
        entityManager.remove(merged);
    }
}
