package org.igov.model.document.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.igov.model.core.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kovylin
 */

@javax.persistence.Entity
public class DocumentImageFile extends AbstractEntity {
    
    @JsonProperty(value = "sHash")
    @Column
    private String sHash;
    
    @JsonProperty(value = "sSecret")
    @Column
    private String sSecret;
    
    @JsonProperty(value = "sKey_File")
    @Column
    private String sKey_File;
    
    @JsonProperty(value = "sID_FileStorage")
    @Column
    private String sID_FileStorage;
    
    @JsonProperty(value = "sFileType")
    @Column
    private String sFileType;
    
    @JsonProperty(value = "sFileExtension")
    @Column
    private String sFileExtension;
    
    @JsonProperty(value = "nBytes")
    @Column
    private Long nBytes;
    
    @JsonProperty(value = "sDateSave")
    @Column
    private String sDateSave;
    
    @OneToMany(mappedBy = "oDocumentImageFile", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<DocumentImageFileSign> aDocumentImageFileSign = new ArrayList<>();

    public List<DocumentImageFileSign> getaDocumentImageFileSign() {
        return aDocumentImageFileSign;
    }

    public void setaDocumentImageFileSign(List<DocumentImageFileSign> aDocumentImageFileSign) {
        this.aDocumentImageFileSign = aDocumentImageFileSign;
    }

    public void addoDocumentImageFileSign(DocumentImageFileSign oDocumentImageFileSign) {
        aDocumentImageFileSign.add(oDocumentImageFileSign);
        oDocumentImageFileSign.setoDocumentImageFile(this);
    }
    
    public String getsHash() {
        return sHash;
    }

    public String getsSecret() {
        return sSecret;
    }

    public String getsKey_File() {
        return sKey_File;
    }

    public String getsID_FileStorage() {
        return sID_FileStorage;
    }

    public String getsFileType() {
        return sFileType;
    }

    public String getsFileExtension() {
        return sFileExtension;
    }

    public Long getnBytes() {
        return nBytes;
    }

    public String getsDateSave() {
        return sDateSave;
    }

    public void setsHash(String sHash) {
        this.sHash = sHash;
    }

    public void setsSecret(String sSecret) {
        this.sSecret = sSecret;
    }

    public void setsKey_File(String sKey_File) {
        this.sKey_File = sKey_File;
    }

    public void setsID_FileStorage(String sID_FileStorage) {
        this.sID_FileStorage = sID_FileStorage;
    }

    public void setsFileType(String sFileType) {
        this.sFileType = sFileType;
    }

    public void setsFileExtension(String sFileExtension) {
        this.sFileExtension = sFileExtension;
    }

    public void setnBytes(Long nBytes) {
        this.nBytes = nBytes;
    }

    public void setsDateSave(String sDateSave) {
        this.sDateSave = sDateSave;
    }
    
}
