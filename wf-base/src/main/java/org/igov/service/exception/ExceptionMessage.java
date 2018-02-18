package org.igov.service.exception;

/**
 * Интерфейс для обьявления текстов ошибок для ексепшенов.
 * @author idenysenko on 23/08/17
 */
public interface ExceptionMessage {
    
    String DOCUMENT_MODIFIED = "Документ змінено!";
    String ACCESS_DENIED = "Немає доступу на данну дiю";
    String SESSION_ENDED = "Час сесії минув. Потрібна повторна авторизація";
    String ALREADY_PRESENT = "ПІБ вже існує на цьому кроці";
    String REMOVE_EXTENAL_WORKER = "Вибачте. Ви не можете поки що видалити стороннього працівника! Ця дія стане можливою зовсім скоро.";
}
