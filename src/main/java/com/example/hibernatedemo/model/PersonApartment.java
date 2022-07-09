package com.example.hibernatedemo.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class PersonApartment implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Person owner;

    @Id
    @ManyToOne
    private Apartment apartment;

    public PersonApartment() {
    }

    public PersonApartment(Person owner, Apartment apartment) {
        this.owner = owner;
        this.apartment = apartment;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apartment == null) ? 0 : apartment.hashCode());
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
        PersonApartment other = (PersonApartment) obj;
        if (apartment == null) {
            if (other.apartment != null)
                return false;
        } else if (!apartment.equals(other.apartment))
            return false;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        return true;
    }
}
