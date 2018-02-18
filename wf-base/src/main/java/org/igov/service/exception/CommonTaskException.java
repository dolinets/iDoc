package org.igov.service.exception;

import org.springframework.http.HttpStatus;

/**
 * @author idenysenko on 23/08/17
 */
public class CommonTaskException extends Exception implements ExceptionMessage {
    
    private HttpStatus httpStatus = HttpStatus.METHOD_NOT_ALLOWED;
    private String errorCode;
    private String message;
    private String sID_Order;
    private String sDocumentStatus;
    
    public CommonTaskException(String errorCode, String message, String sID_Order, String sFilterStatus) {
        super();
        this.errorCode = errorCode;
        this.message = message;
        this.sID_Order = sID_Order;
        this.sDocumentStatus = sFilterStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getsID_Order() {
        return sID_Order;
    }

    public String getsDocumentStatus() {
        return sDocumentStatus;
    }

}
