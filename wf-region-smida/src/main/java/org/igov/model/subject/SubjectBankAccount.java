package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectBankAccount extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectOperatorBank")
    @ManyToOne(targetEntity = SubjectOperatorBank.class)
    @JoinColumn(name = "nID_SubjectOperatorBank")
    private SubjectOperatorBank oSubjectOperatorBank;
    
    @JsonProperty(value = "nAccount")
    @Column(name = "nAccount")
    private Long nAccount;
    
    @JsonProperty(value = "nAccountFON")
    @Column(name = "nAccountFON")
    private Long nAccountFON;

    public SubjectOperatorBank getoSubjectOperatorBank() {
        return oSubjectOperatorBank;
    }

    public void setoSubjectOperatorBank(SubjectOperatorBank oSubjectOperatorBank) {
        this.oSubjectOperatorBank = oSubjectOperatorBank;
    }

    public Long getnAccount() {
        return nAccount;
    }

    public void setnAccount(Long nAccount) {
        this.nAccount = nAccount;
    }

    public Long getnAccountFON() {
        return nAccountFON;
    }

    public void setnAccountFON(Long nAccountFON) {
        this.nAccountFON = nAccountFON;
    }

    @Override
    public String toString() {
        return "SubjectBankAccount{" + "oSubjectOperatorBank=" + oSubjectOperatorBank + ", nAccount=" + nAccount + ", nAccountFON=" + nAccountFON + '}';
    }
    
}
