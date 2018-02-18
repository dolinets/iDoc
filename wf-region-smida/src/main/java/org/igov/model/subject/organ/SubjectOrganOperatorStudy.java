    package org.igov.model.subject.organ;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.Dictionary;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectOrganOperatorStudy extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sName")
    @Column
    private String sName;

    @JsonProperty(value = "sNumber")
    @Column
    private String sNumber;

    @JsonProperty(value = "oSubjectOrgan")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectOrgan")
    private SubjectOrgan oSubjectOrgan;

    @JsonProperty(value = "sNumberContract")
    @Column
    private String sNumberContract;

    @JsonProperty(value = "sDateFrom")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateFrom;
    
    @JsonProperty(value = "sDateTill")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateTill;

    @JsonProperty(value = "sTypeActivities")
    @Column
    private String sTypeActivities;

    @JsonProperty(value = "sTypeContract")
    @Column
    private String sTypeContract;

    @JsonProperty(value = "oDictionary_Specialization")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Dictionary_Specialization")
    private Dictionary oDictionary_Specialization;

    @JsonProperty("sSpecialization")
    @Column
    private String sSpecialization;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsNumber() {
        return sNumber;
    }

    public void setsNumber(String sNumber) {
        this.sNumber = sNumber;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }

    public String getsNumberContract() {
        return sNumberContract;
    }

    public void setsNumberContract(String sNumberContract) {
        this.sNumberContract = sNumberContract;
    }

    public DateTime getsDateFrom() {
        return sDateFrom;
    }

    public void setsDateFrom(DateTime sDateFrom) {
        this.sDateFrom = sDateFrom;
    }

    public DateTime getsDateTill() {
        return sDateTill;
    }

    public void setsDateTill(DateTime sDateTill) {
        this.sDateTill = sDateTill;
    }

    public String getsTypeActivities() {
        return sTypeActivities;
    }

    public void setsTypeActivities(String sTypeActivities) {
        this.sTypeActivities = sTypeActivities;
    }

    public String getsTypeContract() {
        return sTypeContract;
    }

    public void setsTypeContract(String sTypeContract) {
        this.sTypeContract = sTypeContract;
    }

    public Dictionary getoDictionary_Specialization() {
        return oDictionary_Specialization;
    }

    public void setoDictionary_Specialization(Dictionary oDictionary_Specialization) {
        this.oDictionary_Specialization = oDictionary_Specialization;
    }

    public String getsSpecialization() {
        return sSpecialization;
    }

    public void setsSpecialization(String sSpecialization) {
        this.sSpecialization = sSpecialization;
    }
}
