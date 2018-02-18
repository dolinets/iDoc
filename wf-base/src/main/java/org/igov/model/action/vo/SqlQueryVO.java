package org.igov.model.action.vo;

public class SqlQueryVO {

    String sFrom;
    String sWhere;

    public String getsFrom() {
        return sFrom;
    }

    public void setsFrom(String sFrom) {
        this.sFrom = sFrom;
    }

    public String getsWhere() {
        return sWhere;
    }

    public void setsWhere(String sWhere) {
        this.sWhere = sWhere;
    }

    @Override
    public String toString() {
        return "SqlQueryVO{" +
                "sFrom='" + sFrom + '\'' +
                ", sWhere='" + sWhere + '\'' +
                '}';
    }
}
