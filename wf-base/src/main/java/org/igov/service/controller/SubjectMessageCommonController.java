package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.igov.io.GeneralConfig;
import org.igov.io.sms.ManagerSMS;
import org.igov.io.sms.ManagerSMS_New;
import org.igov.io.web.HttpRequester;
import org.igov.model.action.event.HistoryEventDao;
import org.igov.model.action.event.HistoryEvent_ServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@Api(tags = {"SubjectMessageCommonController -- Сообщения субьектов"})
@RequestMapping(value = "/subject/message")
public class SubjectMessageCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectMessageCommonController.class);
    
    @Autowired
    private ManagerSMS_New managerSMS;

    @Autowired
    GeneralConfig generalConfig;

    @Autowired
    private HttpRequester oHttpRequester;
    
    @Autowired
    private ManagerSMS smsManager;
    
    @Autowired
    private HistoryEventDao historyEventDao;
    
    @Autowired
    private HistoryEvent_ServiceDao historyEventServiceDao;
    
      
    /**
     * Колбек для сервиса отправки СМС
     *
     * @param soData_JSON
     * @return 
     */
    @RequestMapping(value = "/getCallbackSMS_PB", method = {RequestMethod.POST,
        RequestMethod.GET}, produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String callbackSMS(@RequestBody String soData_JSON) {
        LOG.debug("callback JSON={}", soData_JSON);
        String ret = managerSMS.saveCallbackSMS(soData_JSON);
        LOG.info("save callback JSON={}", ret);

        return "";
    }

    @ApiOperation(value = "/sendSms", notes = "##### Контроллер отправки смс с проверкой номера по оператору абонента\n")
    @RequestMapping(value = "/sendSms", method = {RequestMethod.POST, RequestMethod.GET}, produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String sendSms(@ApiParam(value = "Номер телефона в формате 380XXXXXXXXX", required = true)@RequestParam(value = "phone") String phone, 
            @ApiParam(value = "Текст сообщения", required = true) @RequestParam(value = "message") String message,
            @ApiParam(value = "Номер заявки", required = false) @RequestParam(value = "sID_Order", required = false, defaultValue = "0-00") String sID_Order){
        try{
            String resp = smsManager.sendSms(phone, message, sID_Order);
            return resp;
        }catch (Exception ex){
            return ex.toString();
        }
    }
}
