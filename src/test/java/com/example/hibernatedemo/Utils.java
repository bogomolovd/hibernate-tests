package com.example.hibernatedemo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.example.hibernatedemo.model.Account;
import com.example.hibernatedemo.model.Address;
import com.example.hibernatedemo.model.Apartment;
import com.example.hibernatedemo.model.CreditAccount;
import com.example.hibernatedemo.model.DebitAccount;
import com.example.hibernatedemo.model.Person;
import com.example.hibernatedemo.model.Phone;
import com.example.hibernatedemo.model.PhoneDetails;

public class Utils {
    public static Person createPerson(int num) {
        Person person = new Person();
        person.setName("Name" + num);
        person.setSurname("Surname" + num);
        List<String> nicknames = new ArrayList<>();
        nicknames.add("best" + num);
        nicknames.add("gamer" + num);
        person.setNicknames(nicknames);
        List<String> lastVisitedCities = new ArrayList<>();
        lastVisitedCities.add("Voronezh");
        lastVisitedCities.add("Tbilisi");
        lastVisitedCities.add("Yerevan");
        person.setLastVisitedCities(lastVisitedCities);
        person.setSsn("123-456-789-" + num);
        return person;
    }

    public static Apartment createApartment(int num) {
        Apartment apartment = new Apartment();
        apartment.setAddress(createAddress(num));
        return apartment;
    }

    public static Address createAddress(int num) {
        Address address = new Address();
        address.setCity("City" + num);
        address.setStreet("Street" + num);
        address.setBuilding(num);
        address.setApartment(num);
        return address;
    }

    public static Phone createPhone(int num) {
        return createPhone(num, null);
    }

    public static Phone createPhone(int num, Person person) {
        Phone phone = new Phone();
        phone.setNumber(String.valueOf(num));
        if (person != null) {
            phone.setPerson(person);
        }
        phone.setPhoneDetails(createPhoneDetails(num, phone));
        return phone;
    }

    public static PhoneDetails createPhoneDetails(int num, Phone phone) {
        PhoneDetails phoneDetails = new PhoneDetails();
        phoneDetails.setOperator("operator" + num);
        phoneDetails.setPhone(phone);
        return phoneDetails;
    }

    public static <T extends EntityManager> void doInTransaction(T entityManager, Consumer<T> consumer) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        consumer.accept(entityManager);
        transaction.commit();
    }

    public static <T> T doInTransactionAndReturn(EntityManager entityManager, Function<EntityManager, T> function) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T result = function.apply(entityManager);
        transaction.commit();
        return result;
    }

    public static void clearDB(EntityManager entityManager) {
        Utils.doInTransaction(entityManager, mgr -> {
            mgr.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
			mgr.createNativeQuery("truncate table PHONE").executeUpdate();
			mgr.createNativeQuery("truncate table PERSON").executeUpdate();
            mgr.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
		});
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Account createAccount(int num) {
        Account account = new Account();
        fillAccount(account, num);
        return account;
    }
    
    public static DebitAccount createDebitAccount(int num) {
        DebitAccount account = new DebitAccount();
        fillAccount(account, num);
        account.setOverdraftFee(new BigDecimal(num));
        return account;
    }

    private static Account fillAccount(Account account, int num) {
        account.setBalance(new BigDecimal(num));
        account.setInterestRate(new BigDecimal(num));
        account.setOwner("owner" + num);
        account.setActive(true);
        return account;
    }

    public static CreditAccount createCreditAccount(int num) {
        CreditAccount account = new CreditAccount();
        fillAccount(account, num);
        account.setCreditLimit(new BigDecimal(num));
        return account;
    }
}
