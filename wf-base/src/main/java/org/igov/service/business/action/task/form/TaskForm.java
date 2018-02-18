package org.igov.service.business.action.task.form;

import io.swagger.annotations.ApiOperation;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import org.activiti.engine.RuntimeService;
import org.apache.commons.io.IOUtils;
import org.igov.service.conf.AttachmetService;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


/**
 *
 * @author Kovylin
 */
@Component("taskForm")
@Service
public class TaskForm {

    private static final Logger LOG = LoggerFactory.getLogger(TaskForm.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private AttachmetService oAttachmetService;

    @Deprecated
    public List<String> getLoginsFromField(String snID_Process_Activiti, String sID_Field) throws Exception {
        LOG.info("getLoginsFromField started");
        return getLoginsFromField(snID_Process_Activiti, sID_Field, null);
    }
    
    @Deprecated
    public List<String> getLoginsFromField(String snID_Process_Activiti, String sID_Field, String sID_FieldTable) throws Exception {
        return getValuesFromTableField(snID_Process_Activiti, sID_Field, sID_FieldTable);
    }

    @ApiOperation(value = "getValuesFromTableField - Получение массива значений из стобца таблицы.", notes
            = "пример activiti: taskForm.getValuesFromTableField(ид_процесса, ид_поля_процесса_с_таблицей, имя_столбца_со_значениями)")
    public List<String> getValuesFromTableField(String snID_Process_Activiti, String sID_FieldTable, String sID_FieldFromTable) throws Exception {

        LOG.info("getLoginsFromField started {} {} {} ", snID_Process_Activiti, sID_FieldTable, sID_FieldFromTable);
        List<String> asLogin = new LinkedList();

        if (snID_Process_Activiti == null || sID_FieldTable == null) {
            throw new RuntimeException("Error in getLoginsFromField - null incoming value");
        }

        try {

            if (sID_FieldFromTable == null) {
                sID_FieldFromTable = "sLogin_isExecute";
            }

            String sValue = (String) runtimeService.getVariable(snID_Process_Activiti, sID_FieldTable);
            LOG.info("sValue {}", sValue);

            // String soJSON=(String)
            // runtimeService.getVariable(snID_Process_Activiti, sID_Field);
            if (sValue != null && sValue.startsWith("{")) {// TABLE
                JSONParser parser = new JSONParser();

                org.json.simple.JSONObject oTableJSONObject = (org.json.simple.JSONObject) parser.parse(sValue);
                LOG.debug("oTableJSONObject {}", oTableJSONObject.toJSONString());
                InputStream oAttachmet_InputStream = oAttachmetService.getAttachment(null, null,
                        (String) oTableJSONObject.get("sKey"), (String) oTableJSONObject.get("sID_StorageType"))
                        .getInputStream();

                org.json.simple.JSONObject oJSONObject = (org.json.simple.JSONObject) parser
                        .parse(IOUtils.toString(oAttachmet_InputStream, "UTF-8"));
                LOG.debug("oTableJSONObject in listener: " + oJSONObject.toJSONString());

                LOG.debug("oJSONObject in cloneDocumentStepFromTable is {}", oJSONObject.toJSONString());

                org.json.simple.JSONArray aJsonRow = (org.json.simple.JSONArray) oJSONObject.get("aRow");

                if (aJsonRow != null) {
                    for (int i = 0; i < aJsonRow.size(); i++) {
                        org.json.simple.JSONObject oJsonField = (org.json.simple.JSONObject) aJsonRow.get(i);
                        LOG.debug("oJsonField in cloneDocumentStepFromTable is {}", oJsonField);
                        if (oJsonField != null) {
                            org.json.simple.JSONArray aJsonField = (org.json.simple.JSONArray) oJsonField.get("aField");
                            LOG.debug("aJsonField in cloneDocumentStepFromTable is {}", aJsonField);
                            if (aJsonField != null) {
                                for (int j = 0; j < aJsonField.size(); j++) {
                                    org.json.simple.JSONObject oJsonMap = (org.json.simple.JSONObject) aJsonField.get(j);
                                    LOG.info("oJsonMap in cloneDocumentStepFromTable is {}", oJsonMap);
                                    if (oJsonMap != null) {
                                        Object oId = oJsonMap.get("id");
                                        if (((String) oId).equals(sID_FieldFromTable) //|| ((String) oId).equals("sID_Group_Activiti_isExecute")
                                                //|| ((String) oId).equals("sLogin_Approver")
                                                //|| ((String) oId).equals("sLogin_Addressee")
                                                ) {
                                            Object oValue = oJsonMap.get("value");
                                            if (oValue != null && !oValue.equals("")) {
                                                LOG.info("oValue in cloneDocumentStepFromTable is {}", oValue);
                                                asLogin.add(oValue.toString());
                                            } else {
                                                LOG.info("oValue in cloneDocumentStepFromTable is null");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LOG.info("JSON array is null in cloneDocumentStepFromTable is null");
                }
            } else {// Simple field with login
                if (sValue != null && !sValue.equals("")) {
                    asLogin.add(sValue);
                }
            }
        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sID_Field=" + sID_FieldTable + ")", oException);
            throw oException;
        }
        return asLogin;
    }
    
    public String getToday(String sFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
        return sdf.format(new java.util.Date());
    }
}
