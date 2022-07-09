package com.example.hibernatedemo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.hibernatedemo.model.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {
}
