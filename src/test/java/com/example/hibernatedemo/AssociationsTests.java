package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import com.example.hibernatedemo.model.Apartment;
import com.example.hibernatedemo.model.Person;
import com.example.hibernatedemo.model.Phone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class AssociationsTests {
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	// @AfterEach
	// public void clearDB() {
	// 	EntityManager entityManager = entityManagerFactory.createEntityManager();
	// 	Utils.clearDB(entityManager);
	// }

	@Test
	void whenPersonIsSavedThenItCanBeFetched() {
		Person expectedPerson = Utils.createPerson(1);
		expectedPerson.setTransientField("someTransientValue");
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		entityManager.merge(expectedPerson);
		transaction.commit();
		Person person = entityManager.find(Person.class, 1L);
		expectedPerson.setId(1L);
		assertEquals(expectedPerson, person);
	}

	@Test
	void whenPhoneSavedWithExistingPersonThenPersonCanBeFetchedByRef() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Utils.doInTransaction(entityManager, mgr -> mgr.merge(Utils.createPerson(1)));
		Person person = entityManager.find(Person.class, 1L);
		Phone expectedPhone = Utils.createPhone(1, person);
		Utils.doInTransaction(entityManager, mgr -> mgr.merge(expectedPhone));
		Phone phone = entityManager.find(Phone.class, 1L);
		expectedPhone.setId(1L);
		expectedPhone.getPhoneDetails().setId(1L);
		assertEquals(expectedPhone, phone);
	}

	@Test
	void whenPhoneSavedWithNotExistingPersonThenPersonCanBeFetchedByRef() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Person expectedPerson = Utils.createPerson(1);
		Phone expectedPhone = Utils.createPhone(1, expectedPerson);
		assertThrows(Exception.class,
			() -> Utils.doInTransaction(entityManager, mgr -> mgr.merge(expectedPhone)));
	}

	@Test
	void whenPersonIsSavedWithPhonesTheyCanBeFetchedByRef() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Person expectedPerson = Utils.createPerson(1);
		Phone expectedPhone1 = Utils.createPhone(1, expectedPerson);
		Phone expectedPhone2 = Utils.createPhone(2, expectedPerson);
		expectedPerson.getPhones().add(expectedPhone1);
		expectedPerson.getPhones().add(expectedPhone2);
		Utils.doInTransaction(entityManager, mgr -> mgr.merge(expectedPerson));
		Person person = entityManager.find(Person.class, 1L);
		expectedPerson.setId(1L);
		expectedPhone1.setId(1L);
		expectedPhone1.getPhoneDetails().setId(1L);
		expectedPhone2.setId(2L);
		expectedPhone2.getPhoneDetails().setId(2L);
		assertEquals(expectedPerson, person);
	}

	@Test
	void whenPersonIsSavedWithApartmentsTheyCanBeFetchedByRef() {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		Person expectedPerson1 = Utils.createPerson(1);
		Person expectedPerson2 = Utils.createPerson(2);
		Apartment apartment1 = Utils.createApartment(1);
		Apartment apartment2 = Utils.createApartment(2);
		Utils.doInTransaction(entityManager, mgr -> {
			mgr.persist(expectedPerson1);
			mgr.persist(expectedPerson2);
			mgr.persist(apartment1);
			mgr.persist(apartment2);
			expectedPerson1.addApartment(apartment1);
			expectedPerson1.addApartment(apartment2);
			expectedPerson2.addApartment(apartment1);
		});
		Person person1 = entityManager.find(Person.class, 1L);
		Person person2 = entityManager.find(Person.class, 2L);
		assertEquals(expectedPerson1, person1);
		assertEquals(expectedPerson2, person2);
		assertEquals(apartment1, person1.getPersonApartments().get(0).getApartment());
		assertEquals(apartment2, person1.getPersonApartments().get(1).getApartment());
		assertEquals(apartment1, person2.getPersonApartments().get(0).getApartment());
	}
}
