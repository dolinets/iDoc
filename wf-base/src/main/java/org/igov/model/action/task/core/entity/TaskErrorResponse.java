package org.igov.model.action.task.core.entity;

import java.util.Objects;

/**
 *
 * @author idenysenko
 */
public class TaskErrorResponse implements TaskErrorResponseI {

    private final String errorCode;
    private final String message;
    private final String sID_Order;
    private final String sDocumentStatus;

    public TaskErrorResponse(String errorCode, String message, String sID_Order, String sDocumentStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.sID_Order = sID_Order;
        this.sDocumentStatus = sDocumentStatus;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getsID_Order() {
        return sID_Order;
    }

    @Override
    public String getsDocumentStatus() {
        return sDocumentStatus;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.errorCode);
        hash = 97 * hash + Objects.hashCode(this.message);
        hash = 97 * hash + Objects.hashCode(this.sID_Order);
        hash = 97 * hash + Objects.hashCode(this.sDocumentStatus);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskErrorResponse other = (TaskErrorResponse) obj;
        if (!Objects.equals(this.errorCode, other.errorCode)) {
            return false;
        }
        if (!Objects.equals(this.message, other.message)) {
            return false;
        }
        if (!Objects.equals(this.sID_Order, other.sID_Order)) {
            return false;
        }
        return Objects.equals(this.sDocumentStatus, other.sDocumentStatus);
    }

    @Override
    public String toString() {
        return "TaskErrorResponse{" + "errorCode=" + errorCode
                + ", message=" + message
                + ", sID_Order=" + sID_Order
                + ", sDocumentStatus=" + sDocumentStatus + '}';
    }
}
