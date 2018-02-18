package org.igov.service.business.util;

import org.igov.model.core.AbstractEntity;

public class SubjectUtils {

    /**
     * workaround
     */
    public static void checkID(AbstractEntity entity) {
        /* временно закомментировал, пока не разберемся с генерацией идишек хибернейтом, чтобы различать сабджектов
         созданных с кнопки и из .csv
        if (entity != null && entity.getId() != null && entity.getId() < 99_000) {
            throw new RuntimeException("wrong entity id: " + entity.getId() + ", class: " + entity.getClass());
        }*/
    }

}
