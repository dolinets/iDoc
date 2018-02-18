package org.igov.service.controller.interceptor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionProcessCountUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ActionProcessCountUtils.class);

    private static final String URI_SET_ACTION_PROCESS_COUNT = "/wf/service/action/event/setActionProcessCount";
    private static final String URI_GET_ACTION_PROCESS_COUNT = "/wf/service/action/event/getActionProcessCount";

    public static Integer callSetActionProcessCount(HttpRequester httpRequester, GeneralConfig generalConfig, String sID_BP, Long nID_Service) {
        Map<String, String> mParam = new HashMap<>();
        mParam.put("sID_BP", sID_BP);
        int result = 0;
        if (nID_Service != null) {
            mParam.put("nID_Service", nID_Service.toString());
        }
        try {
            String soResponse = httpRequester.getInside(generalConfig.getSelfHostCentral() + URI_SET_ACTION_PROCESS_COUNT, mParam);
            LOG.info("Received response for updating ActionProcessCount {}", soResponse); //{"nCountYear":26}
            Map<String, Object> mReturn = (Map<String, Object>) JSONValue.parse(soResponse);
            if (mReturn != null && mReturn.containsKey("nCountYear")) {
                result = Integer.valueOf(mReturn.get("nCountYear").toString());
            }
        } catch (Exception ex) {
            LOG.error("callSetActionProcessCount: ", ex);
        }
        LOG.info("found nCountYear: " + result);
        return result;
    }

    public static Integer callGetActionProcessCount(HttpRequester httpRequester, GeneralConfig generalConfig, String sID_BP, Long nID_Service, Integer nYear) {
        Map<String, String> mParam = new HashMap<>();

        if (sID_BP != null && sID_BP.contains(":")) {
            sID_BP = StringUtils.substringBefore(sID_BP, ":");
            LOG.info("Cutting business process definition in order get business process id. sID_BP {}", sID_BP);
        }

        LOG.info("sID_BP before putting in map: " + sID_BP);
        mParam.put("sID_BP", sID_BP);

        if (nID_Service != null) {
            mParam.put("nID_Service", nID_Service.toString());
        }
        if (nYear != null) {
            mParam.put("nYear", nYear.toString());
        }

        try {
            LOG.info("generalConfig.getSelfHostCentral() URI: " + generalConfig.getSelfHostCentral() + URI_GET_ACTION_PROCESS_COUNT);

            for (String m : mParam.keySet()) {
                LOG.info("mParam elem: " + mParam.get(m));
            }

            String soResponse = httpRequester.getInside(generalConfig.getSelfHostCentral() + URI_GET_ACTION_PROCESS_COUNT, mParam);
            LOG.info("Received response for updating ActionProcessCount {}", soResponse);
            Map<String, Object> mReturn = (Map<String, Object>) JSONValue.parse(soResponse);
            if (mReturn != null && mReturn.containsKey("nCountYear")) {
                return Integer.valueOf(mReturn.get("nCountYear").toString());
            }
        } catch (Exception e) {
            LOG.info("Error occured while processing  {}", e.getMessage());
        }
        return 0;
    }

}
