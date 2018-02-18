package org.igov.model.document.image;

import io.swagger.annotations.ApiModelProperty;

/**
 * command object
 */
public class DocumentImageFileSignVO {
    
    @ApiModelProperty("Строка с подписью")
    private String sSign;
    @ApiModelProperty("Строковый ID типа подписи")
    private String sID_SignType;
    @ApiModelProperty("Строка-JSON-обьект с данными ЭЦП субьекта")
    private String sSignData_JSON;
    
    public DocumentImageFileSignVO() {
        
    }
    
    public String getsSign() {
        return sSign;
    }
    
    public void setsSign(String sSign) {
        this.sSign = sSign;
    }
    
    public String getsID_SignType() {
        return sID_SignType;
    }
    
    public void setsID_SignType(String sID_SignType) {
        this.sID_SignType = sID_SignType;
    }
    
    public String getsSignData_JSON() {
        return sSignData_JSON;
    }
    
    public void setsSignData_JSON(String sSignData_JSON) {
        this.sSignData_JSON = sSignData_JSON;
    }
    
}
