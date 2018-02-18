package org.igov.service.business.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

/**
 *
 * @author idenysenko
 */
public final class BusinessDaysCalculator {
    
    /**
     * Добавить только рабочие дни
     * 
     * @param workingDays кол-во дней которое нужно добавить
     * @return стратегию корректировки
     */
    public static TemporalAdjuster addWorkingDays(long workingDays) {
        return TemporalAdjusters.ofDateAdjuster(d -> addWorkingDay(d, workingDays));
    }

    private static LocalDate addWorkingDay(LocalDate startingDate, long workingDays) {
        if (workingDays == 0) {
            return nextOrSameWorkingDay(startingDate);
        }

        LocalDate result = startingDate;
        int step = Long.signum(workingDays); 

        for (long i = 0; i < Math.abs(workingDays); i++) {
            result = nextWorkingDay(result, step);
        }

        return result;
    }

    private static LocalDate nextOrSameWorkingDay(LocalDate date) {
        return isWeekEnd(date) ? nextWorkingDay(date, 1) : date;
    }

    private static LocalDate nextWorkingDay(LocalDate date, int step) {
        do {
            date = date.plusDays(step);
        } while (isWeekEnd(date));
        return date;
    }

    private static boolean isWeekEnd(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
