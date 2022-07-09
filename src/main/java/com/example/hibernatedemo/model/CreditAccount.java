package com.example.hibernatedemo.model;

import java.math.BigDecimal;

import javax.persistence.Entity;

@Entity
public class CreditAccount extends Account {
    private BigDecimal creditLimit;

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((creditLimit == null) ? 0 : creditLimit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CreditAccount other = (CreditAccount) obj;
        if (creditLimit == null) {
            if (other.creditLimit != null)
                return false;
        } else if (!creditLimit.equals(other.creditLimit))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CreditAccount [creditLimit=" + creditLimit + "]";
    }
}
