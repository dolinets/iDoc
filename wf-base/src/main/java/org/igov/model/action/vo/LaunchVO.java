package org.igov.model.action.vo;

import java.io.Serializable;
import java.util.Arrays;

public class LaunchVO implements Serializable {

    private Class<?> oClass;
    private Class[] aClass;
    private Object[] aObject;

    public LaunchVO() {
    }

    public Class<?> getoClass() {
        return oClass;
    }

    public void setoClass(Class<?> oClass) {
        this.oClass = oClass;
    }

    public Class[] getaClass() {
        return aClass;
    }

    public void setaClass(Class[] aClass) {
        this.aClass = aClass;
    }

    public Object[] getaObject() {
        return aObject;
    }

    public void setaObject(Object[] aObject) {
        this.aObject = aObject;
    }

    @Override
    public String toString() {
        return "LaunchVO{" +
                "oClass=" + oClass +
                ", aClass=" + Arrays.toString(aClass) +
                ", aObject=" + Arrays.toString(aObject) +
                '}';
    }
}
