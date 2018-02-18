package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectOperatorBank extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubject")
    @ManyToOne(targetEntity = Subject.class)
    @JoinColumn(name = "nID_Subject")
    private Subject oSubject;

    @JsonProperty(value = "sName")
    @Column(name = "sName")
    private String sName;
    
    @JsonProperty(value = "nMFO")
    @Column(name = "nMFO")
    private Long nMFO;
    
    @JsonProperty(value = "sOKPO")
    @Column(name = "sOKPO")
    private String sOKPO;
    
    @JsonProperty(value = "sLocation")
    @Column(name = "sLocation")
    private String sLocation;
    
    @JsonProperty(value = "aSubjectBankAccount")
    @OneToMany(cascade = CascadeType.ALL)
    private List<SubjectBankAccount> aSubjectBankAccount;

    public Subject getoSubject() {
        return oSubject;
    }

    public void setoSubject(Subject oSubject) {
        this.oSubject = oSubject;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public Long getnMFO() {
        return nMFO;
    }

    public void setnMFO(Long nMFO) {
        this.nMFO = nMFO;
    }

    public String getsOKPO() {
        return sOKPO;
    }

    public void setsOKPO(String sOKPO) {
        this.sOKPO = sOKPO;
    }

    public String getsLocation() {
        return sLocation;
    }

    public void setsLocation(String sLocation) {
        this.sLocation = sLocation;
    }

    public List<SubjectBankAccount> getaSubjectBankAccount() {
        return aSubjectBankAccount;
    }

    public void setaSubjectBankAccount(List<SubjectBankAccount> aSubjectBankAccount) {
        this.aSubjectBankAccount = aSubjectBankAccount;
    }
 
    
}
