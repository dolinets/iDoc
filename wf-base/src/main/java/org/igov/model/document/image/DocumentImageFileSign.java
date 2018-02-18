package org.igov.model.document.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.*;

/**
 *
 * @author Kovylin
 */
@Entity
public class DocumentImageFileSign extends AbstractEntity{
    
    /*@JsonProperty(value = "nID_DocumentImageFile")
    @Column
    private Long nID_DocumentImageFile;*/
    
    @JsonIgnore
    @ManyToOne(targetEntity = DocumentImageFile.class)
    @JoinColumn(name = "nID_DocumentImageFile")
    private DocumentImageFile oDocumentImageFile;
    
    @JsonProperty(value = "sSign")
    @Column
    private String sSign;
    
    /*@JsonProperty(value = "nID_SignType")
    @Column
    private Long nID_SignType;*/

    @JsonIgnore
    @ManyToOne(targetEntity = SignType.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "nID_SignType")
    private SignType oSignType;
    
    @JsonProperty(value = "sSignData_JSON")
    @Column
    private String sSignData_JSON;

    /*public Long getnID_DocumentImageFile() {
        return nID_DocumentImageFile;
    }*/

    public String getsSign() {
        return sSign;
    }

    /*public Long getnID_SignType() {
        return nID_SignType;
    }*/

    public String getsSignData_JSON() {
        return sSignData_JSON;
    }

    public DocumentImageFile getoDocumentImageFile() {
        return oDocumentImageFile;
    }

    public SignType getoSignType() {
        return oSignType;
    }
    
    /*public void setnID_DocumentImageFile(Long nID_DocumentImageFile) {
        this.nID_DocumentImageFile = nID_DocumentImageFile;
    }*/

    public void setsSign(String sSign) {
        this.sSign = sSign;
    }

    /*public void setnID_SignType(Long nID_SignType) {
        this.nID_SignType = nID_SignType;
    }*/

    public void setsSignData_JSON(String sSignData_JSON) {
        this.sSignData_JSON = sSignData_JSON;
    }

    public void setoDocumentImageFile(DocumentImageFile oDocumentImageFile) {
        this.oDocumentImageFile = oDocumentImageFile;
    }

    public void setoSignType(SignType oSignType) {
        this.oSignType = oSignType;
    }
    
    
    
}
