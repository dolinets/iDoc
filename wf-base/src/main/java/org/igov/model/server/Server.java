package org.igov.model.server;

import org.igov.model.core.AbstractEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

/**
 * User: goodg_000
 * Date: 29.10.2015
 * Time: 21:26
 */
@javax.persistence.Entity
@ApiModel(description="Сервер")
public class Server extends AbstractEntity {

    @Column(unique = true, nullable = false)
    @ApiModelProperty(value = "Строка-ИД сервера, уникальный", required = true)
    private String sID;

    @Column
    @ApiModelProperty(value = "Тип сервера", required = true)
    private String sType;

    @Column
    @ApiModelProperty(value = "URL хоста-Alpha (не актуально)", required = false)
    private String sURL_Alpha;

    @Column
    @ApiModelProperty(value = "URL хоста-Beta (не актуально)", required = false)
    private String sURL_Beta;

    @Column
    @ApiModelProperty(value = "URL хоста-Omega (не актуально)", required = false)
    private String sURL_Omega;

    @Column
    @ApiModelProperty(value = "URL хоста (основного)", required = true)
    private String sURL;

    public String getsID() {
        return sID;
    }
    public void setsID(String sID) {
        this.sID = sID;
    }

    public String getsType() {
        return sType;
    }
    public void setsType(String sType) {
        this.sType = sType;
    }

    public String getsURL_Alpha() {
        return sURL_Alpha;
    }
    public void setsURL_Alpha(String sURL_Alpha) {
        this.sURL_Alpha = sURL_Alpha;
    }

    public String getsURL_Beta() {
        return sURL_Beta;
    }
    public void setsURL_Beta(String sURL_Beta) {
        this.sURL_Beta = sURL_Beta;
    }

    public String getsURL_Omega() {
        return sURL_Omega;
    }
    public void setsURL_Omega(String sURL_Omega) {
        this.sURL_Omega = sURL_Omega;
    }

    public String getsURL() {
        return sURL;
    }
    public void setsURL(String sURL) {
        this.sURL = sURL;
    }

    @Override
    public String toString() {
        return "Server{" + "id=" + getId()
                + "sID=" + sID
                + ", sType=" + sType
                + ", sURL_Alpha=" + sURL_Alpha
                + ", sURL_Beta=" + sURL_Beta
                + ", sURL_Omega=" + sURL_Omega
                + ", sURL=" + sURL
                + '}';
    }
    
}

