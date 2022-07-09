package com.example.hibernatedemo.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;

import com.example.hibernatedemo.NicknamesTypeDescriptor;

@Entity
public class Person implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Access(AccessType.PROPERTY)
    private String name;
    private String surname;

    @Convert(converter = NicknamesTypeDescriptor.class)
    private List<String> nicknames;

    @ElementCollection
    private List<String> lastVisitedCities;

    @NaturalId
    private String ssn;

    @Transient
    private String transientField;
    
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<Phone> phones = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<PersonApartment> personApartments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        System.out.println("name accessed");
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        System.out.println("surname accessed");
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTransientField() {
        return transientField;
    }

    public void setTransientField(String transientField) {
        this.transientField = transientField;
    }


    public List<String> getNicknames() {
        return nicknames;
    }

    public void setNicknames(List<String> nicknames) {
        this.nicknames = nicknames;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public List<String> getLastVisitedCities() {
        return lastVisitedCities;
    }

    public void setLastVisitedCities(List<String> lastVisitedCities) {
        this.lastVisitedCities = lastVisitedCities;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    public List<PersonApartment> getPersonApartments() {
        return personApartments;
    }

    public void setPersonApartments(List<PersonApartment> personApartments) {
        this.personApartments = personApartments;
    }

    public void addApartment(Apartment apartment) {
        PersonApartment personApartment = new PersonApartment(this, apartment);
        personApartments.add(personApartment);
        apartment.getPersonApartments().add(personApartment);
    }

    public void removeApartment(Apartment apartment) {
        PersonApartment personApartment = new PersonApartment(this, apartment);
        personApartments.remove(personApartment);
        apartment.getPersonApartments().remove(personApartment);
        personApartment.setApartment(null);
        personApartment.setOwner(null);
    }


    @Override
    public String toString() {
        return "Person [id=" + id + ", lastVisitedCities=" + lastVisitedCities + ", name=" + name + ", nicknames="
                + nicknames + ", personApartments=" + personApartments + ", phones=" + phones + ", ssn=" + ssn
                + ", surname=" + surname + ", transientField=" + transientField + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lastVisitedCities == null) ? 0 : lastVisitedCities.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((nicknames == null) ? 0 : nicknames.hashCode());
        result = prime * result + ((phones == null) ? 0 : phones.hashCode());
        result = prime * result + ((ssn == null) ? 0 : ssn.hashCode());
        result = prime * result + ((surname == null) ? 0 : surname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastVisitedCities == null) {
            if (other.lastVisitedCities != null)
                return false;
        } else if (!lastVisitedCities.equals(other.lastVisitedCities))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (nicknames == null) {
            if (other.nicknames != null)
                return false;
        } else if (!nicknames.equals(other.nicknames))
            return false;
        if (phones == null) {
            if (other.phones != null)
                return false;
        } else if (!phones.equals(other.phones))
            return false;
        if (ssn == null) {
            if (other.ssn != null)
                return false;
        } else if (!ssn.equals(other.ssn))
            return false;
        if (surname == null) {
            if (other.surname != null)
                return false;
        } else if (!surname.equals(other.surname))
            return false;
        return true;
    }
}
