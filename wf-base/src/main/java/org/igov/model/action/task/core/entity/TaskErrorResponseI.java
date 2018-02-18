package org.igov.model.action.task.core.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 *
 * @author idenysenko
 */
@JsonSerialize(as = TaskErrorResponse.class)
@JsonDeserialize(as = TaskErrorResponse.class)
public interface TaskErrorResponseI {
    /**
     * @return Код ошибки
     */
    String getErrorCode();

    /**
     * @return Сообщение об ошибке
     */
    String getMessage();
    
    /**
     * @return Ид активной таски
     */
    String getsID_Order();
    
    /**
     * @return Название вкладки в которой находится документ
     */
    
    String getsDocumentStatus();
}
