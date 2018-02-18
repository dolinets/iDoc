package org.igov.service.exception;

import org.igov.service.business.util.CommonUtils;
import org.springframework.http.HttpStatus;

/**
 * Created by diver on 4/6/15.
 */
public class CommonServiceException extends Exception {

    private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    private String errorCode;

    public CommonServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CommonServiceException(String message) {
        super(message);
    }

    public CommonServiceException(String errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

    public CommonServiceException(String errorCode, String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = errorCode;
    }

    public CommonServiceException(String errorCode, String message, HttpStatus httpStatus) {
        this(errorCode, message);
        setHttpStatus(httpStatus);
    }

    public CommonServiceException(String errorCode, String message, Throwable throwable, HttpStatus httpStatus) {
        this(errorCode, message, throwable);
        setHttpStatus(httpStatus);
    }

    public CommonServiceException(int errorCode, String message) {
        super(message);
        this.errorCode = String.valueOf(errorCode);
    }

    public CommonServiceException(int errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = String.valueOf(errorCode);
    }

    public CommonServiceException(int errorCode, String message, Throwable throwable) {
        super(message, throwable);
        this.errorCode = String.valueOf(errorCode);
    }

    public CommonServiceException(int errorCode, String message, HttpStatus httpStatus) {
        this(errorCode, message);
        setHttpStatus(httpStatus);
    }

    public CommonServiceException(int errorCode, String message, Throwable throwable, HttpStatus httpStatus) {
        this(errorCode, message, throwable);
        setHttpStatus(httpStatus);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
    
     /**
     * @param oException
     * @return текст ошибки.
     */
    public static String sException(Exception oException) {
        if (CommonUtils.bIs(oException.getMessage())) {
            return oException.getMessage();
        } else if (oException.getCause() != null && oException.getCause().getMessage() != null) {
            return oException.getCause().toString();
        } else if (oException.getCause() != null && oException.getCause().toString() != null) {
            return oException.getCause().toString();
        } else {
            return oException.toString();
        }
    }
}
