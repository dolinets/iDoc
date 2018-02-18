package org.igov.service.business.subject.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.igov.model.action.event.HistoryEventDao;
import org.igov.model.action.event.HistoryEvent_Service;
import org.igov.model.action.event.HistoryEvent_ServiceDao;
import org.igov.model.subject.message.SubjectMessage;
import org.igov.service.exception.CommonServiceException;
import org.igov.util.JSON.JsonRestUtils;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Oleksii Khalikov
 */
@Service
public class MessageService {
    private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private GeneralConfig oGeneralConfig;

    @Autowired
    private HttpRequester oHttpRequester;
    
    @Autowired
    private HistoryEventDao historyEventDao;
    
    @Autowired
    private HistoryEvent_ServiceDao historyEventServiceDao;
    
    private String getServiceMessages(String sID_Order) throws CommonServiceException, JsonProcessingException {
        Long nID_HistoryEvent_Service;
        List<SubjectMessage> aSubjectMessage = new ArrayList();
        List<Object> aoSubjectMessage = new ArrayList();
        try {
            HistoryEvent_Service oHistoryEvent_Service = historyEventServiceDao.getOrgerByID(sID_Order);
            nID_HistoryEvent_Service = oHistoryEvent_Service.getId();
            
            aoSubjectMessage.addAll(historyEventDao.getHistoryEvents(null, nID_HistoryEvent_Service, false));
            
        } catch (Exception e) {
            LOG.error("FAIL: {}", e);
            //LOG.trace("FAIL:", e);
            //throw new CommonServiceException(500, "[setServiceMessage]{sID_Order=" + sID_Order + "}:" + e.getMessage());
        }
        return JsonRestUtils.toJson(aoSubjectMessage);
    }
    
    
    /**
     * Получение сообщений по заявке
     * @param nID_Process - номер-ИД процесса
     * @return массив сообщений (строка JSON)
     */
    public String gerOrderMessagesByProcessInstanceID(Long nID_Process) throws Exception {
        String sID_Order = oGeneralConfig.getOrderId_ByProcess(nID_Process);
        Map<String, String> params = new HashMap<>();
        int nID_Server_current = oGeneralConfig.getSelfServerId();
        
        params.put("sID_Order", sID_Order);
        
        LOG.info("SelfHost in gerOrderMessagesByProcessInstanceID: {}", oGeneralConfig.getSelfHost());
        
        /*if(oGeneralConfig.getSelfHost().contains("region")){
            params.put("isRegion", "true");
        }*/
        
        /*if(nID_Server_current == Integer.parseInt(sID_Order.split("-")[0])){
            LOG.info("we get messages from region server");
            params.put("isRegion", "true");
        }*/
        
        String soResponse = "";
       
        if(oGeneralConfig.getSelfHostCentral().equals(oGeneralConfig.getSelfHost())){
            //this check is for servers, where central is absent
            soResponse = getServiceMessages(sID_Order);
        }
        else if(nID_Server_current == Integer.parseInt(sID_Order.split("-")[0])){
            soResponse = getServiceMessages(sID_Order);
        }
        else{
            String sURL = oGeneralConfig.getSelfHostCentral() + "/wf/service/subject/message/getServiceMessages";
            soResponse = oHttpRequester.getInside(sURL, params);
        }
        
        LOG.info("(soResponse={})", soResponse);
        return soResponse;
    }

}
