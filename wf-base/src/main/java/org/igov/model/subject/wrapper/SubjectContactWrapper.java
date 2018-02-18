package org.igov.model.subject.wrapper;

public class SubjectContactWrapper {

    private String sType;
    private String sValue;

    public SubjectContactWrapper() {

    }

    public String getsType() {
        return sType;
    }

    public void setsType(String sType) {
        this.sType = sType;
    }

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SubjectContactWrapper)) {
            return false;
        }
        final SubjectContactWrapper that = (SubjectContactWrapper) obj;
        return getsType().equals(that.getsType()) && getsValue().equals(that.getsValue());
    }

    @Override
    public int hashCode() {
        return 17 * getsType().hashCode() * getsValue().hashCode() - 11;
    }
}
