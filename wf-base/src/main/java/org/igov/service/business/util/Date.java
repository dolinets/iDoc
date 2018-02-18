/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.util;

import io.swagger.annotations.ApiOperation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
/**
 *
 * @author olga
 */
@Component("dateUtil")
@Service
public class Date {

    private static final Logger LOG = LoggerFactory.getLogger(Date.class);

    @Autowired
    private RuntimeService oRuntimeService;
    
    @ApiOperation(value = "getDateFormatted_ByField - Получение отформатированного значения стринги-даты из поля процесса", notes
            = "пример activiti: dateUtil.getDateFormatted_ByField(ид_процесса, ид_поля_процесса_с_датой, формат_даты_SimpleDateFormat)")
    public String getDateFormatted_ByField(String snID_Process, String sID_Field, String sFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
        ProcessInstance processInstance = oRuntimeService.createProcessInstanceQuery().processInstanceId(snID_Process)
                .includeProcessVariables().singleResult();
        LOG.info("sID_Field: " + processInstance.getProcessVariables().get(sID_Field));
        java.util.Date oDate = (java.util.Date) processInstance.getProcessVariables().get(sID_Field);
        return sdf.format(oDate);
    }

    public Integer getDateDiff_ByField(String snID_Process, String sID_Field_DateAt,
            String sID_Field_DateTo) {
        LOG.info("getDateDiff_ByField started with params: snID_Process - {}, sID_Field_DateAt - {}, sID_Field_DateTo - {}", snID_Process, sID_Field_DateAt, sID_Field_DateTo);
        /*ProcessInstance processInstance = oRuntimeService.createProcessInstanceQuery().processInstanceId(snID_Process)
                .includeProcessVariables().singleResult();
        java.util.Date oDateAt = (java.util.Date) processInstance.getProcessVariables().get(sID_Field_DateAt);
        java.util.Date oDateTo = (java.util.Date) processInstance.getProcessVariables().get(sID_Field_DateTo);*/
        java.util.Date oDateAt = parseDate(sID_Field_DateAt);
        java.util.Date oDateTo = parseDate(sID_Field_DateTo);
        LOG.info("oDateAt - {}, oDateTo - {}", oDateAt, oDateTo);
        Integer dateDiff = getDateDiff(oDateAt, oDateTo);
        LOG.info("dateDiff is {}", dateDiff);
        return dateDiff;

    }
    
    public java.util.Date parseDate(String dateString){
        java.util.Date returnDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            returnDate = dateFormat.parse(dateString);
        }catch (ParseException e) {
            LOG.info("Error in parseDate: {}", e);
            throw new RuntimeException("Неверный формат даты");
        }
        return returnDate;
    }

    public static Integer getDateDiff(java.util.Date oDateAt, java.util.Date oDateTo) {
        return Math.round((oDateTo.getTime() - oDateAt.getTime()) / (1000 * 60 * 60 * 24));
    }

    public static String getToday(String sFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
        return sdf.format(new java.util.Date());
    }

    public static java.util.Date diff(java.util.Date oDate, int nCountNew, int nCalendarType) {
        Calendar oCalendar = Calendar.getInstance();
        if (oDate == null) {
            oDate = new java.util.Date();
        }
        oCalendar.setTime(oDate);
        oCalendar.add(nCalendarType, nCountNew);
        return oCalendar.getTime();
    }
    
    public static String convertMilliSecondsToFormattedDate(Long milliSeconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return simpleDateFormat.format(calendar.getTime());
    }
}
