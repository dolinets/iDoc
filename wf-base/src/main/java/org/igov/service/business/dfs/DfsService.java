/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.dfs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Attachment;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.temp.model.ByteArrayMultipartFile;
import org.igov.io.web.HttpRequester;
import org.igov.service.exception.DocumentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static org.igov.util.ToolWeb.base64_encode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.igov.io.db.kv.statical.IBytesDataStorage;
import org.igov.service.business.action.event.HistoryEventService;
import org.igov.service.controller.ActionTaskCommonController;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author olga
 */
@Service
public class DfsService {

    private static final Logger LOG = LoggerFactory.getLogger(DfsService.class);

    private final static String CONTENT_TYPE = "text/xml; charset=utf-8";

    @Autowired
    GeneralConfig generalConfig;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HttpRequester oHttpRequester;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryEventService historyEventService;
    @Autowired
    private IBytesDataStorage durableBytesDataStorage;

    public String getAnswer(String sID_Task, String snID_Process, String sINN, String snCountYear, String sFile_XML_SWinEd_Filter) {
        StringBuilder asID_Attach_Dfs = new StringBuilder();
        List<ByteArrayMultipartFile> aByteArrayMultipartFile = getAnswer(sINN, snCountYear);
        LOG.info("getAnswer snID_Process: " + snID_Process + " snCountYear: " + snCountYear
                + "aByteArrayMultipartFile.size()=" + aByteArrayMultipartFile.size());
        String sFile_XML_SWinEd_Value = (String) runtimeService.getVariable(snID_Process, "oFile_XML_SWinEd");
        String sFileName_XML_SWinEd_Answer_Value = (String) runtimeService.getVariable(snID_Process, "sFileName_XML_SWinEd_Answer");
        String saName_Attach_Dfs_Value = (String) runtimeService.getVariable(snID_Process, "saName_Attach_Dfs");
        LOG.debug("saName_Attach_Dfs: " + saName_Attach_Dfs_Value);
        String saName_Attach_Dfs = "";
        boolean bExist_Attach_Dfs_Answer = false;
        try {
            Attachment oAttachment_Document = taskService.getAttachment(sFile_XML_SWinEd_Value); //sFileName_XML_SWinEd_Answer=F1401801
            if (oAttachment_Document != null) {
                String sID_Order = generalConfig.getOrderId_ByProcess(Long.valueOf(snID_Process));
                String sAttachmentName_Document = oAttachment_Document.getName();
                LOG.info("getAnswer sAttachmentName_Document=" + sAttachmentName_Document + ", sID_Order=" + sID_Order);
                sAttachmentName_Document = sAttachmentName_Document.replaceAll(".xml", "");
                
                for (ByteArrayMultipartFile oByteArrayMultipartFile : aByteArrayMultipartFile) {
                    String sFileName = oByteArrayMultipartFile.getOriginalFilename();
                    String sFileContentType = oByteArrayMultipartFile.getContentType() + ";" + oByteArrayMultipartFile.getExp();
                    if ((sFileName.contains(sAttachmentName_Document) && !sFileName.endsWith(".xml"))
                            || (bExist_Attach_Dfs_Answer = sFileName.contains(sFileName_XML_SWinEd_Answer_Value))) { //"F1401801"
                        LOG.info("ToAttach-PROCESS Found sFileName=" + sFileName + " sAttachmentName_Document=" + sAttachmentName_Document);
                        LOG.info("ToAttach-PROCESS saName_Attach_Dfs_Value=" + saName_Attach_Dfs_Value + " saName_Attach_Dfs=" + saName_Attach_Dfs);
                        LOG.info("bExist_Attach_Dfs_Answer=" + bExist_Attach_Dfs_Answer);
                        
                        if (saName_Attach_Dfs_Value == null || !saName_Attach_Dfs_Value.contains(sFileName)) {
                            LOG.info("sFile_XML_SWinEd_Filter is: {}", sFile_XML_SWinEd_Filter);
                            if (sFile_XML_SWinEd_Filter == null 
                                    || "".equalsIgnoreCase(sFile_XML_SWinEd_Filter.trim()) 
                                    || sFileName.contains(sFile_XML_SWinEd_Filter)) {
                                saName_Attach_Dfs = saName_Attach_Dfs + sFileName + ",";
                                Attachment oAttachment = taskService.createAttachment(sFileContentType,
                                        sID_Task, snID_Process, sFileName, oByteArrayMultipartFile.getName(), oByteArrayMultipartFile.getInputStream());
                                LOG.info("oAttachment {}", oAttachment);
                                if (oAttachment != null) {
                                    asID_Attach_Dfs.append(oAttachment.getId()).append(",");
                                    LOG.info("oAttachment.getId()=" + oAttachment.getId());
                                    try {
                                        String sMail = "";
                                        BufferedInputStream oBufferedInputStream = new BufferedInputStream(oByteArrayMultipartFile.getInputStream());
                                        byte[] aByte = IOUtils.toByteArray(oBufferedInputStream);
                                        saveServiceMessage_EncryptedFile("Отримана відповідь від Державної Фіскальної Служби", "Отримана відповідь від Державної фіскальної служби за Вашим запитом.", aByte, sID_Order, sMail, sFileName, sFileContentType);
                                    } catch (Exception ex) {
                                        LOG.error("ToJournal sFileName=" + sFileName + " sAttachmentName_Document=" + sAttachmentName_Document + ":" + ex.getMessage());
                                        java.util.logging.Logger.getLogger(ActionTaskCommonController.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } else {
                                    LOG.info("Attach is null!!! sFileName: " + sFileName);
                                }
                            } else{
                                LOG.info("We don't need this file now skip sFileName: " + sFileName);
                            }
                        } else {
                            LOG.info("skip sFileName: " + sFileName);
                        }
                    } else {
                        LOG.info("ToAttach-SKIP sFileName=" + sFileName + " sAttachmentName_Document=" + sAttachmentName_Document);
                    }
                }
            } else {
                LOG.info("Can't find attachmett oFile_XML_SWinEd: " + sFile_XML_SWinEd_Value);
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(ActionTaskCommonController.class.getName()).log(Level.SEVERE, null, ex);
        }

        String sID_Attach_Dfs = "";
        if (asID_Attach_Dfs.length() > 0) {
            sID_Attach_Dfs = asID_Attach_Dfs.deleteCharAt(asID_Attach_Dfs.length() - 1).toString();
        }
        runtimeService.setVariable(snID_Process, "anID_Attach_Dfs", sID_Attach_Dfs);
        taskService.setVariable(sID_Task, "anID_Attach_Dfs", sID_Attach_Dfs);
        runtimeService.setVariable(snID_Process, "bExist_Attach_Dfs_Answer", bExist_Attach_Dfs_Answer);
        runtimeService.setVariable(snID_Process, "saName_Attach_Dfs", saName_Attach_Dfs_Value + saName_Attach_Dfs);
        return asID_Attach_Dfs.toString();
    }

    protected void saveServiceMessage_EncryptedFile(String sHead, String sBody, byte[] aByte, String sID_Order, String sMail, String sFileName, String sFileContentType) {

        final Map<String, String> mParam = new HashMap<>();
        mParam.put("sHead", sHead);//"Відправлено листа"
        mParam.put("sBody", sBody);
        mParam.put("sID_Order", sID_Order);
        mParam.put("sMail", sMail);
        mParam.put("sFileName", sFileName);
        mParam.put("sFileContentType", sFileContentType);
        mParam.put("nID_SubjectMessageType", "" + 12L);
        mParam.put("sID_DataLinkSource", "Region");
        mParam.put("sID_DataLinkAuthor", "SFS");
        String sID_DataLink;
        sID_DataLink = durableBytesDataStorage.saveData(aByte); //sBody.getBytes(Charset.forName("UTF-8"))
        mParam.put("sID_DataLink", sID_DataLink);

        mParam.put("RequestMethod", RequestMethod.GET.name());

        LOG.info("ToJournal-PROCESS mParam=" + mParam);

        ScheduledExecutorService oScheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        Runnable oRunnable = new Runnable() {

            @Override
            public void run() {
                LOG.info(
                        "try to save service message with params with a delay: (params={})",
                        mParam);
                String jsonServiceMessage;
                try {
                    jsonServiceMessage = historyEventService
                            .addServiceMessage(mParam);
                    LOG.info("(jsonServiceMessage={})", jsonServiceMessage);
                } catch (Exception e) {
                    LOG.error("( saveServiceMessage error={})", e.getMessage());
                }
            }
        };
        // run saving message in 10 seconds so history event will be in the
        // database already by that time
        oScheduledExecutorService.schedule(oRunnable, 10, TimeUnit.SECONDS);
        oScheduledExecutorService.shutdown();

        LOG.info(
                "Configured thread to run in 10 seconds with params: (params={})",
                mParam);
    }

    public String send(String content, String fileName, String email) throws Exception {
        LOG.info("content: " + content + " fileName: " + fileName + " email: " + email);
        String body = createBody_Send(content, fileName, email);
        return oHttpRequester.postInside(generalConfig.getsURL_DFS(), null, body, CONTENT_TYPE);
    }

    private String createBody_Send(String content, String fileName, String email) {
        String result = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">")
                .append("<soap12:Body>")
                .append("<Send xmlns=\"http://govgate/\">")
                .append("<fileName>").append(fileName).append("</fileName>")
                .append("<senderEMail>").append(email).append("</senderEMail>")
                .append("<data>").append(base64_encode(content)).append("</data>")
                .append("</Send>")
                .append("</soap12:Body>")
                .append("</soap12:Envelope>").toString();
        return result;
    }

    public List<ByteArrayMultipartFile> getAnswer(String INN, String snCountYear) {
        List<ByteArrayMultipartFile> result = new ArrayList<>();
        try {
            String responseBody = getMessages(INN);
            LOG.info("getMessages responseBody: " + responseBody + " snCountYear: " + snCountYear + " INN: " + INN);
            List<String> resultMessages = getContentFromXml(responseBody, "string");
            LOG.info("getMessages resultMessage: " + resultMessages);
            for (String resultMessage : resultMessages) {
                if (resultMessage != null) {
                    responseBody = receive(resultMessage);
                    LOG.info("receive responseBody: " + responseBody);
                    List<String> fileNames = getContentFromXml(responseBody, "fileName");
                    List<String> fileContents = getContentFromXml(responseBody, "messageData");
                    LOG.info("receive fileNames: " + fileNames);
                    if (fileNames != null && fileNames.size() > 0 && fileContents != null && fileContents.size() > 0
                            && fileNames.get(0) != null && snCountYear != null && fileNames.get(0).contains(snCountYear)) {
                        String fileName = fileNames.get(0);
                        byte[] fileContent = Base64.decodeBase64(fileContents.get(0));
                        if (fileName != null && fileContent != null && fileContent.length > 0) {
                            ByteArrayMultipartFile oByteArrayMultipartFile = new ByteArrayMultipartFile(fileContent, fileName,
                                    fileName, "text/plain");
                            result.add(oByteArrayMultipartFile);
                            responseBody = delete(resultMessage);
                            LOG.info("delete responseBody: " + responseBody);
                        }
                    } else {
                        LOG.info("skip fileNames: " + fileNames);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error("getAnswer: ", ex);
        }
        return result;
    }

    private String getMessages(String inn) throws Exception {
        LOG.info("inn: " + inn);
        String body = createBody_GetMessages(inn);
        return oHttpRequester.postInside(generalConfig.getsURL_DFS(), null, body, CONTENT_TYPE);
    }

    private String createBody_GetMessages(String inn) {
        String result = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">")
                .append("<soap12:Body>")
                .append("<GetMessages xmlns=\"http://govgate/\">")
                .append("<signedEDRPOU>").append(base64_encode(inn)).append("</signedEDRPOU>")
                .append("</GetMessages>")
                .append("</soap12:Body>")
                .append("</soap12:Envelope>").toString();
        return result;
    }

    private String receive(String massageID) throws Exception {
        LOG.info("massageID: " + massageID);
        String body = createBody_Receive(massageID);
        return oHttpRequester.postInside(generalConfig.getsURL_DFS(), null, body, CONTENT_TYPE);
    }

    private String createBody_Receive(String massageID) {
        String result = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">")
                .append("<soap12:Body>")
                .append("<Receive xmlns=\"http://govgate/\">")
                .append("<signedMsgId>").append(base64_encode(massageID)).append("</signedMsgId>")
                .append("</Receive>")
                .append("</soap12:Body>")
                .append("</soap12:Envelope>").toString();
        return result;
    }

    private String delete(String massageID) throws Exception {
        LOG.info("massageID: " + massageID);
        String body = createBody_Delete(massageID);
        return oHttpRequester.postInside(generalConfig.getsURL_DFS(), null, body, CONTENT_TYPE);
    }

    private String createBody_Delete(String massageID) {
        String result = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">")
                .append("<soap12:Body>")
                .append("<Delete xmlns=\"http://govgate/\">")
                .append("<signedMsgId>").append(base64_encode(massageID)).append("</signedMsgId>")
                .append("</Delete>")
                .append("</soap12:Body>")
                .append("</soap12:Envelope>").toString();
        return result;
    }

    private static List<String> getContentFromXml(String xmlDocument, String tagName) {
        List<String> result = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        org.w3c.dom.Document doc;
        try {
            builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlDocument));
            doc = builder.parse(is);
            NodeList nodeList = doc.getElementsByTagName(tagName);
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Node nodeId = nodeList.item(i);
                result.add(nodeId.getTextContent());
                LOG.debug("nodeId.value: " + nodeId.getNodeValue() + " nodeId.getAttributes: " + nodeId.getAttributes().getLength()
                        + " nodeId.getTextContent: " + nodeId.getTextContent() + " nodeId.getNodeName(): " + nodeId.getNodeName());
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DocumentNotFoundException("Can't parse Session ID.", e);
        }
        return result;
    }
}
