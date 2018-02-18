package org.igov.model.action.vo;

public class FilterFieldVO {

    private String sID_Field;
    private String sCondition;
    private String sValue;

    public FilterFieldVO() {
    }

    public String getsID_Field() {
        return sID_Field;
    }

    public void setsID_Field(String sID_Field) {
        this.sID_Field = sID_Field;
    }

    public String getsCondition() {
        return sCondition;
    }

    public void setsCondition(String sCondition) {
        this.sCondition = sCondition;
    }

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }

    public boolean validate() {
        boolean bResult = false;
        if (this.sID_Field != null && this.sCondition != null && this.sValue != null) {
            bResult = true;
        }
        return bResult;
    }

    public String parseOperation() {
        String sExpression = "";
        if (this.sCondition.equals("equals")) {
            sExpression = " = '" + this.sValue + "'";
        }
        if (this.sCondition.equals("contains")) {
            sExpression = " LIKE '%" + this.sValue +"%'";
        }
        if (this.sCondition.equals("startWith")) {
            sExpression = " LIKE '" + this.sValue +"%'";
        }
        if (this.sCondition.equals("endWith")) {
            sExpression = " LIKE '%" + this.sValue +"'";
        }
        if (this.sCondition.equals("moreThan")) {
            sExpression = " > '%" + this.sValue +"%'";
        }
        if (this.sCondition.equals("lessThan")) {
            sExpression = " < '%" + this.sValue +"%'";
        }
        return  sExpression;
    }

    @Override
    public String toString() {
        return "FilterFieldVO{" +
                "sID_Field='" + sID_Field + '\'' +
                ", sCondition='" + sCondition + '\'' +
                ", sValue='" + sValue + '\'' +
                '}';
    }
}
