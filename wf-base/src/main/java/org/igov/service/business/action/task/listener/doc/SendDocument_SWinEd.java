package org.igov.service.business.action.task.listener.doc;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.temp.IBytesDataInmemoryStorage;
import org.igov.io.db.kv.temp.model.ByteArrayMultipartFile;
import org.igov.io.web.HttpRequester;
import static org.igov.service.business.action.task.core.AbstractModelTask.getByteArrayMultipartFileFromStorageInmemory;
import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;
import org.igov.util.swind.GateSoapProxy;
import org.igov.util.swind.ProcessResult;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("SendDocument_SWinEd")
public class SendDocument_SWinEd implements TaskListener {

    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LoggerFactory.getLogger(SendDocument_SWinEd.class);
    
    //private final static String URL = "http://217.76.198.151/Websrvgate/gate.asmx";
    //public final static String URL = "http://109.237.89.107:1220/gate.asmx"; //тестовый
    //public final static String URL = "http://217.76.198.151/Websrvgate/gate.asmx"; //боевой
    
    private Expression sEmail;
    
    private Expression sID_File_XML_SWinEd;
    
    @Autowired
    GeneralConfig generalConfig;

    @Autowired
    private IBytesDataInmemoryStorage oBytesDataInmemoryStorage;

    @Override
    public void notify(DelegateTask delegateTask) {

        DelegateExecution execution = delegateTask.getExecution();
        String sID_File_XML_SWinEdValue = getStringFromFieldExpression(this.sID_File_XML_SWinEd, execution);
        String sEmailValue = getStringFromFieldExpression(this.sEmail, execution);
        String resp = "[none]";
        try {
            LOG.info("sID_File_XML_SWinEdValue: " + sID_File_XML_SWinEdValue);
            byte[] oFile_XML_SWinEd = oBytesDataInmemoryStorage.getBytes(sID_File_XML_SWinEdValue);
            ByteArrayMultipartFile oByteArrayMultipartFile = getByteArrayMultipartFileFromStorageInmemory(oFile_XML_SWinEd);
            LOG.info("sEmailValue : " + sEmailValue 
                    + " oByteArrayMultipartFile.getOriginalFilename(): " + oByteArrayMultipartFile.getOriginalFilename());
            if (oFile_XML_SWinEd != null) {
                GateSoapProxy gate = new GateSoapProxy(generalConfig.getsURL_DFS());
                LOG.info("!!! Before sending request to gate web service. sID_File_XML_SWinEdValue:" + oByteArrayMultipartFile.getOriginalFilename() + 
                		" sEmailValue:" + sEmailValue + " endpoint:" + gate.getEndpoint() + " content:" + oByteArrayMultipartFile.getBytes());
                ProcessResult result = gate.send(oByteArrayMultipartFile.getOriginalFilename(), sEmailValue, oByteArrayMultipartFile.getBytes());

                //ProcessResult result = gate.send(oByteArrayMultipartFile.getOriginalFilename(), sEmailValue, 
                //        new String(oByteArrayMultipartFile.getBytes(), "windows-1251").getBytes());
                
                /*String originalFilename_utf8 = oByteArrayMultipartFile.getOriginalFilename();
                String utf8String_windows_1251= new String(originalFilename_utf8.getBytes("UTF-8"), "windows-1251");
                ProcessResult result = gate.send(utf8String_windows_1251, sEmailValue, 
                        new String(oByteArrayMultipartFile.getBytes(), "windows-1251").getBytes());*/
                
                
                LOG.info("!!!response:" + result.getValue());
                if(!ProcessResult.GATE_OK.getValue().equalsIgnoreCase(result.getValue().trim())){
                    throw new RuntimeException("Сервис ДФС вернул ошибку " + result.getValue() + " на запрос: " + oByteArrayMultipartFile.getOriginalFilename() 
                            + " " + new String(oByteArrayMultipartFile.getBytes()));
                }
                resp = result.getValue();
            } else {
                LOG.info("sID_File_XML_SWinEdValue: " + sID_File_XML_SWinEdValue + " oFile_XML_SWinEd is null!!!");
            }
            execution.setVariable("result", resp);
        } catch (Exception ex) {
            LOG.error("!!! Error in SendDocument_SWinEd sID_File_XML_SWinEdValue=" + sID_File_XML_SWinEdValue, ex);
            execution.setVariable("result", resp);
        }
    }

}
