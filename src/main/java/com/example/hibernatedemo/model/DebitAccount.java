package com.example.hibernatedemo.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.PostPersist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class DebitAccount extends Account {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebitAccount.class);

    private BigDecimal overdraftFee;

    public BigDecimal getOverdraftFee() {
        return overdraftFee;
    }

    public void setOverdraftFee(BigDecimal overdraftFee) {
        this.overdraftFee = overdraftFee;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((overdraftFee == null) ? 0 : overdraftFee.hashCode());
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
        DebitAccount other = (DebitAccount) obj;
        if (overdraftFee == null) {
            if (other.overdraftFee != null)
                return false;
        } else if (!overdraftFee.equals(other.overdraftFee))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DebitAccount [overdraftFee=" + overdraftFee + "]";
    }

    @PostPersist
    public void postPersist() {
        LOGGER.info("persisted entity {}", this);
    }
}
