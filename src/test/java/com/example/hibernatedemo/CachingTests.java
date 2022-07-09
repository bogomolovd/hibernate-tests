package com.example.hibernatedemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.CacheMode;
import org.hibernate.Session;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;

import com.example.hibernatedemo.model.Account;
import com.example.hibernatedemo.model.Person;
import com.example.hibernatedemo.model.Phone;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class CachingTests {
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    public void whenEntityExistsIn2ndLevelCacheThenItIsReadFromThere() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Account account = Utils.createAccount(1);
        // entity is put to 2nd level cache
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        // entity is loaded from 2nd level cache
        Account fetchedAccount = entityManager2.find(Account.class, 1L);
        // entity is removed from persistence context to simulate cache miss
        entityManager2.detach(fetchedAccount);
        // entity is loaded from db because of the cache miss
        entityManager2.find(Account.class, 1L);
        Session session = entityManager2.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();

        assertEquals(1, statistics.getSecondLevelCacheMissCount());
        assertEquals(1, statistics.getSecondLevelCachePutCount());
        assertEquals(1, statistics.getSecondLevelCacheHitCount());
    }

    @Test
    public void whenEntityUpdatedThenItIsIn2ndLevelCacheAlso() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        Account account = Utils.createAccount(1);
        // entity is put to 2nd level cache
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        // entity is loaded from 2nd level cache
        Account fetchedAccount = entityManager2.find(Account.class, 1L);
        // entity is put to 2nd level cache for the second time
        Utils.doInTransaction(entityManager2, em -> {
            fetchedAccount.setActive(false);
        });
        EntityManager entityManager3 = entityManagerFactory.createEntityManager();
        // entity is loaded from 2nd level cache for the second time
        Account fetchedAccount3 = entityManager3.find(Account.class, 1L);

        assertFalse(fetchedAccount3.isActive());
        assertEquals(2, statistics.getSecondLevelCachePutCount());
        assertEquals(2, statistics.getSecondLevelCacheHitCount());
    }

    @Test
    public void whenRelationshipIsCachedThenItsIdsAreReadFrom2LvlCache() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Person person = Utils.createPerson(1);
        Phone phone = Utils.createPhone(1, person);
        person.getPhones().add(phone);
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        // put person to 2nd level cache
        Person fetchedPerson = entityManager2.find(Person.class, 1L);
        // put phones to 2nd level cache
        fetchedPerson.getPhones().get(0);
        EntityManager entityManager3 = entityManagerFactory.createEntityManager();
        // take person from 2nd level cache as there is 1st level cache for new entity
        // mgr
        fetchedPerson = entityManager3.find(Person.class, 1L);
        // take phones from 2nd level cache as there is 1st level cache for new entity
        // mgr
        fetchedPerson.getPhones().get(0);
        Session session = entityManager3.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        CollectionStatistics collectionStatistics = statistics
                .getCollectionStatistics("com.example.hibernatedemo.model.Person.phones");

        // statistics is counted for the whole session factory, so the are non-zero
        // miss and put counters
        assertEquals(1, collectionStatistics.getCacheMissCount());
        assertEquals(1, collectionStatistics.getCachePutCount());
        assertEquals(1, collectionStatistics.getCacheHitCount());
    }

    // caching for queries should be turned on explicitly as it affects performance
    @Test
    public void whenQueryExecutedThenItStoredTheResultsIn2ndLvlCache() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        Person person = Utils.createPerson(1);
        Person person2 = Utils.createPerson(2);
        Utils.doInTransaction(entityManager, em -> {
            em.persist(person);
            em.persist(person2);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        // cache miss and cache put for the query in the 2nd lvl cache
        entityManager2.unwrap(Session.class)
                .createQuery("select p from Person p where id = :id")
                .setParameter("id", 1L)
                // caching should be turned on explicitly
                .setCacheable(true)
                .getResultList();

        EntityManager entityManager3 = entityManagerFactory.createEntityManager();
        // cache hit for the query in the 2nd lvl cache
        entityManager3.unwrap(Session.class)
                .createQuery("select p from Person p where id = :id")
                .setParameter("id", 1L)
                .setCacheable(true)
                .getResultList();

        assertEquals(1L, statistics.getQueryCacheHitCount());
        assertEquals(1L, statistics.getQueryCacheMissCount());
        assertEquals(1L, statistics.getQueryCachePutCount());
    }

    @Test
    public void whenCacheModeIsGetThenEntityIsNotRefreshedIn2ndLvlCache() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        Account account = Utils.createAccount(1);
        // entity is put to 2nd level cache
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
        });
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        Session session2 = entityManager2.unwrap(Session.class);
        session2.setCacheMode(CacheMode.GET);
        // entity is loaded from 2nd level cache
        Account fetchedAccount = entityManager2.find(Account.class, 1L);
        // entity is not put to 2nd level cache because of GET cache mode
        Utils.doInTransaction(entityManager2, em -> {
            fetchedAccount.setActive(false);
        });
        assertEquals(1, statistics.getSecondLevelCacheHitCount());
        assertEquals(1, statistics.getSecondLevelCachePutCount());
    }

    @Test
    public void whenEntityIsEvictedFrom2ndLvlCacheThenItCantBeFoundThere() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Session session = entityManager.unwrap(Session.class);
        Statistics statistics = session.getSessionFactory().getStatistics();
        Account account = Utils.createAccount(1);
        // entity is put to 2nd level cache
        Utils.doInTransaction(entityManager, em -> {
            em.persist(account);
        });
        // entity is evicted from 2nd level cache
        entityManagerFactory.getCache().evict(Account.class, 1L);
        EntityManager entityManager2 = entityManagerFactory.createEntityManager();
        // entity is not loaded from 2nd level cache as it was evicted
        // cache miss is registered and entity is put to 2nd level cache again
        entityManager2.find(Account.class, 1L);

        assertEquals(0, statistics.getSecondLevelCacheHitCount());
        assertEquals(1, statistics.getSecondLevelCacheMissCount());
        assertEquals(2, statistics.getSecondLevelCachePutCount()); 
    }

    @TestConfiguration
    @PropertySource("classpath:application-caching.properties")
    public static class CachingTestsConfiguration {
    }
}
