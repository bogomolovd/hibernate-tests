package com.example.hibernatedemo;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.hibernatedemo.model.Apartment;
import com.example.hibernatedemo.model.Person;
import com.example.hibernatedemo.model.Phone;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
// general best practice is to make all associations lazy and init them only when needed
// such initialization can be done dynamically case-by-case using hql queries and entity graphs
@NamedEntityGraph(
    name = "person.phones", 
    attributeNodes = {
        @NamedAttributeNode("phones")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "phone.phoneDetails",
            attributeNodes = {
                @NamedAttributeNode("phoneDetails")
            }
        ),
    }
)
public class FetchingTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void whenEagerAssociationIsFetchedViaFindThenNoAdditionalQueryIsExecuted() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Phone fetchedPhone = entityManager2.find(Phone.class, 1L);
        // look at the logs. There are only one query with join operators to fetch all info
        // Many to one association is eager by default
        assertTrue(Hibernate.isInitialized(fetchedPhone.getPerson()));
    }

    @Test
    void whenEagerAssociationIsFetchedViaQueryThenAdditionalQueryIsExecuted() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Phone fetchedPhone = entityManager2.createQuery("select p from Phone p where p.id = 1",
            Phone.class).getSingleResult();
        // look at the logs. There are three queries to fetch all data (person, phone and details)
        // that's because hibernate needs to fetch eager association even if it's not used in query
        // use joins in query to avoid N + 1 issue
        assertTrue(Hibernate.isInitialized(fetchedPhone.getPerson()));
    }

    @Test
    void whenEntityIsFetchedThenLazyAssociationIsNotInitialized() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Person fetchedPerson = entityManager2.find(Person.class, 1L);
        // look at the logs. There is addtional query for phone as one to many is lazy by default
        assertFalse(Hibernate.isInitialized(fetchedPerson.getPhones()));
    }

    @Test
    void whenEntityIsFetchedUsingJoinQueryThenLazyAssociationIsInitialized() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Person fetchedPerson = entityManager2
            .createQuery("select p from Person p left join fetch p.phones ph where p.id = 1", Person.class)
            .getSingleResult();
        // look at the logs. There only one query for the entire structure (person+phone+details)
        // because of the one to one association
        assertTrue(Hibernate.isInitialized(fetchedPerson.getPhones()));
    }

    @Test
    void whenEntityIsFetchedUsingFetchGraphThenLazyAssociationIsInitialized() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        EntityGraph<Person> personEntityGraph = entityManager2.createEntityGraph(Person.class);
        personEntityGraph.addAttributeNodes("phones");
        personEntityGraph.addSubgraph("phones").addAttributeNodes("phoneDetails");;
        Person fetchedPerson = entityManager2.find(Person.class, 1L,
            singletonMap("jakarta.persistence.fetchgraph", personEntityGraph));
        // look at the logs. There only one query for the entire structure (person+phone+details)
        assertTrue(Hibernate.isInitialized(fetchedPerson.getPhones()));
        assertTrue(Hibernate.isInitialized(fetchedPerson.getPhones().get(0).getPhoneDetails()));
    }

    @Test
    void whenEagerAssociationIsNotPresentInFetchGraphThenItIsInitialized() {
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        EntityGraph<Person> personEntityGraph = entityManager2.createEntityGraph(Person.class);
        personEntityGraph.addAttributeNodes("phones");
        Person fetchedPerson = entityManager2.find(Person.class, 1L,
            singletonMap("jakarta.persistence.fetchgraph", personEntityGraph));
        assertTrue(Hibernate.isInitialized(fetchedPerson.getPhones()));
        // phone details is absent in entity graph
        // but it is still initialized because it is many to one and eager by default
        // look at the logs. There is an additional query for details because it is absent in entity graph
        assertTrue(Hibernate.isInitialized(fetchedPerson.getPhones().get(0).getPhoneDetails()));
    }

    @Test
    void whenFetchModeIsSubSelectThenSubQueryIsUsedToFetchAssociations() {
        Person person = Utils.createPerson(1);
        Person person2 = Utils.createPerson(2);
        Apartment apartment = Utils.createApartment(1);
        Apartment apartment2 = Utils.createApartment(2);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
            em.persist(person2);
            em.persist(apartment);
            em.persist(apartment2);
            person.addApartment(apartment);
            person2.addApartment(apartment2);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        List<Person> fetchedPeople = entityManager2.createQuery("select p from Person p", Person.class)
            .getResultList();
        // look at the logs. As subselect mode was chosen for person_apartments association
        // they are fetched using subselect instead of joining tables
        assertFalse(fetchedPeople.get(0).getPersonApartments().isEmpty());
        assertFalse(fetchedPeople.get(1).getPersonApartments().isEmpty());
    }
}
