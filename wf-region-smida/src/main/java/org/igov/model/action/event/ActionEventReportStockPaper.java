package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventReportStockPaper extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;

    @JsonProperty(value = "nSizeCharterCapitalIPO")
    @Column
    private Long nSizeCharterCapitalIPO;
    
    @JsonProperty(value = "nCommonDenominationIPO")
    @Column
    private Long nCommonDenominationIPO;
    
    @JsonProperty(value = "nQuantityIPO")
    @Column
    private Long nQuantityIPO;

    @JsonProperty(value = "sDateReportSubmissionIPO")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateReportSubmissionIPO;

    public ActionEvent getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEvent oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public Long getnSizeCharterCapitalIPO() {
        return nSizeCharterCapitalIPO;
    }

    public void setnSizeCharterCapitalIPO(Long nSizeCharterCapitalIPO) {
        this.nSizeCharterCapitalIPO = nSizeCharterCapitalIPO;
    }

    public Long getnCommonDenominationIPO() {
        return nCommonDenominationIPO;
    }

    public void setnCommonDenominationIPO(Long nCommonDenominationIPO) {
        this.nCommonDenominationIPO = nCommonDenominationIPO;
    }

    public Long getnQuantityIPO() {
        return nQuantityIPO;
    }

    public void setnQuantityIPO(Long nQuantityIPO) {
        this.nQuantityIPO = nQuantityIPO;
    }

    public DateTime getsDateReportSubmissionIPO() {
        return sDateReportSubmissionIPO;
    }

    public void setsDateReportSubmissionIPO(DateTime sDateReportSubmissionIPO) {
        this.sDateReportSubmissionIPO = sDateReportSubmissionIPO;
    }
    
    

    
}
