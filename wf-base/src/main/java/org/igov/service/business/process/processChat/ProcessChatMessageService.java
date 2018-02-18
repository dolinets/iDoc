package org.igov.service.business.process.processChat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Optional;
import org.activiti.engine.RuntimeService;
import org.igov.model.document.DocumentStep;
import org.igov.model.document.DocumentStepDao;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.model.document.DocumentStepSubjectRightDao;
import org.igov.model.process.processChat.*;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.exception.CommonServiceException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("processChatMessageService")
public class ProcessChatMessageService {

    @Autowired
    private ProcessChatMessageDao processChatMessageDao;

    @Autowired
    private ProcessChatMessageTreeDao processChatMessageTreeDao;

    @Autowired
    private ProcessChatMessageTreeService processChatMessageTreeService;
    
    @Autowired
    private EmailProcessSubjectService oEmailProcessSubjectService;
    
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    @Autowired
    private DocumentStepService oDocumentStepService;
    
    @Autowired
    private RuntimeService oRuntimeService;
    
    @Autowired
    private DocumentStepDao oDocumentStepDao;
    
    @Autowired
    private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;
    
    @Autowired
    private ProcessLinkService oProcessLinkService;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessChatMessageService.class);

    /**
     * Save ProcessChatMessage
     *
     * @param oProcessChat
     * @param sKeyGroup_Author
     * @param sBody
     * @param nID_ProcessChatMessage_Parent
     * @return
     */
    protected ProcessChatMessage setProcessChatMessage(ProcessChat oProcessChat, String sKeyGroup_Author, String sLoginReferent, String sBody, Long nID_ProcessChatMessage_Parent) throws CommonServiceException, Exception{
        ProcessChatMessage processChatMessage = null;
        try {           
            //в случае если валидация провалена, сообщение не будет создано вообще
            if (nID_ProcessChatMessage_Parent != null) {
                boolean isEnableToUpdate = isValid(nID_ProcessChatMessage_Parent);
                LOG.info("isEnableToUpdate: {}", isEnableToUpdate);

                if (!isEnableToUpdate) {
                    throw new CommonServiceException(new Exception().getMessage(), "Вибачте, Ви не можете відповісти на це повідомлення через те, що його було вилучено!");
                }
            }
            
            processChatMessage = new ProcessChatMessage();
            processChatMessage.setoProcessChat(oProcessChat);
            processChatMessage.setsKeyGroup_Author(sKeyGroup_Author);
            processChatMessage.setsLoginReferent(sLoginReferent);

            SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sKeyGroup_Author).orNull();            
            String sFIO = oSubjectGroup.getName();            
            if (!sFIO.isEmpty() && sFIO != null) {               
                processChatMessage.setsFIO_Author(sFIO);                
            }
            
            oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLoginReferent).orNull();            
            String sFIO_Referent = oSubjectGroup.getName();            
            if (sFIO_Referent != null && !sFIO_Referent.isEmpty()) {               
                processChatMessage.setsFIO_Referent(sFIO_Referent);              
            }
            
            processChatMessage.setsDate(new DateTime(new Date()));
            processChatMessage.setsBody(sBody);           
           
            LOG.info(String.format("The new instance of ProcessChatMessage with "
                    + "nID_ProcessChat=%s, sKeyGroup_Author=%s, sFIO=%s, sDate=%s, sBody=%s was created",
                    oProcessChat.getId(), sKeyGroup_Author, sFIO, processChatMessage.getsDate().toString("D"), sBody));
            processChatMessage = processChatMessageDao.setProcessChatMessage(processChatMessage);
            LOG.info(String.format("Entity was added with id=%s", processChatMessage.getId()));
           
            if (nID_ProcessChatMessage_Parent != null) {             
                
                ProcessChatMessageTree processChatMessageTree = new ProcessChatMessageTree();
                processChatMessageTree.setProcessChatMessageParent(processChatMessageDao.findByIdExpected(nID_ProcessChatMessage_Parent));
                processChatMessageTree.setProcessChatMessageChild(processChatMessage);
                processChatMessageTreeDao.saveOrUpdate(processChatMessageTree);
                LOG.info(String.format("Entity was added of ProcessChatMessageTree was added"));
            }
        
        resetAuthorUrgents(oProcessChat.getnID_Process_Activiti().toString(), sKeyGroup_Author);
        } catch (Exception e) {
            LOG.warn("(Fail set process {})", e.getMessage());   
            throw e;
        }
        return processChatMessage;
    }

    /**
     * Get Catalog of ProcessChatMessage
     *
     * @param nID_Process_Activiti
     * @param aProcessChat
     * @return
     */
    public List<ProcessChat> getCatalogProcessChatMessage(Long nID_Process_Activiti, List<ProcessChat> aProcessChat) {

        LOG.info("getCatalogProcessChatMessage start...");
        LOG.debug("nID_Process_Activiti: " + nID_Process_Activiti);

        List<ProcessChatMessage> aProcessChatMessageByProcessActiviti = new ArrayList<>(processChatMessageDao.findAllBy("oProcessChat.nID_Process_Activiti", nID_Process_Activiti));
        List<ProcessChatMessageTree> aProcessChatMessageRelations = processChatMessageTreeService.getProcessChatMessageRelations(nID_Process_Activiti);
        List<ProcessChatMessageParentNode> parentProcessChatMessage = new ArrayList<>();
        Map<Long, List<ProcessChatMessage>> subjToNodeMap = new HashMap<>();
        ProcessChatMessageParentNode parentChatMessage;
        Set<Long> idParentList = new LinkedHashSet<>();
        List<Long> aChildID = new ArrayList<>();
        String sFIO = "";
        String sFIO_Referent = "";

        for (ProcessChatMessageTree processChatMessageTree : aProcessChatMessageRelations) {
            final ProcessChatMessage parent = processChatMessageTree.getProcessChatMessageParent();  
            
            parentChatMessage = new ProcessChatMessageParentNode();
            final ProcessChatMessage child = processChatMessageTree.getProcessChatMessageChild();    
            
            sFIO = getFIO(child.getsKeyGroup_Author());
            if (sFIO != null && !sFIO.isEmpty()) {
                child.setsFIO_Author(sFIO);
            }
            
            sFIO_Referent = getFIO(child.getsLoginReferent());
            if (sFIO_Referent != null && !sFIO_Referent.isEmpty()) {
                child.setsFIO_Referent(sFIO_Referent);
            }
            
            //устанавливаем парентов для всех чатов по sKeyGroup
            if (!idParentList.contains(parent.getId())) {
                idParentList.add(parent.getId());
                // устанавливаем парентов
                parentChatMessage.setGroup(parent);
                // доавляем детей
                parentChatMessage.addChild(child);
                parentProcessChatMessage.add(parentChatMessage);
                // мапа парент_id -ребенок
                subjToNodeMap.put(parent.getId(), parentChatMessage.getChildren());
                aChildID.add(child.getId());
            } else {
                for (ProcessChatMessageParentNode processChatMessageParentNode : parentProcessChatMessage) {
                    // убираем дубликаты
                    if (processChatMessageParentNode.getGroup().getId().equals(parent.getId())) {
                        // если дубликат парента-добавляем его детей к
                        // общему списку
                        processChatMessageParentNode.getChildren().add(child);
                        // мапа парент_id -ребенок
                        subjToNodeMap.put(parent.getId(), processChatMessageParentNode.getChildren());
                        aChildID.add(child.getId());
                    }
                }
            }
        }
        for (ProcessChat oProcessChat : aProcessChat) {
            Long nID_ProcessChat = oProcessChat.getId();
            List<ProcessChatMessage> result = new ArrayList();
            ProcessChatMessage aCatalogProcessChatMessage;

            List<ProcessChatMessage> aProcessChatMessageBynIDProcessChat = aProcessChatMessageByProcessActiviti.stream()
                    .filter(m -> m.getoProcessChat().getId().equals(nID_ProcessChat)).collect(Collectors.toList());
            for (ProcessChatMessage oProcessChatMessage : aProcessChatMessageBynIDProcessChat) {
                
                sFIO = getFIO(oProcessChatMessage.getsKeyGroup_Author());                
                if (!sFIO.isEmpty() && sFIO != null) {
                    oProcessChatMessage.setsFIO_Author(sFIO);                    
                }
                
                sFIO_Referent = getFIO(oProcessChatMessage.getsLoginReferent());
                if (sFIO_Referent != null && !sFIO_Referent.isEmpty()) {
                    oProcessChatMessage.setsFIO_Referent(sFIO_Referent);
                }
                
                if (aChildID.contains(oProcessChatMessage.getId())) {
                    continue;
                }
                if (subjToNodeMap.containsKey(oProcessChatMessage.getId())) {

                    aCatalogProcessChatMessage = getProcessChatMessageTree(oProcessChatMessage, subjToNodeMap);                    
                    result.add(aCatalogProcessChatMessage);
                } else {                    
                    result.add(oProcessChatMessage);
                }                
            }
            oProcessChat.setaProcessChatMessage(result);
        }        
        return aProcessChat;
    }

    private ProcessChatMessage getProcessChatMessageTree(ProcessChatMessage oProcessChatMessage, Map<Long, List<ProcessChatMessage>> aSubjToNodeMap) {
        createChildrenTreeCatalog(oProcessChatMessage, aSubjToNodeMap);

        return oProcessChatMessage;
    }

    /**
     * Get Catalog of ProcessChatMessage
     *
     * @param oProcessChatMessage
     * @param aSubjToNodeMap
     * @return
     */
    private void createChildrenTreeCatalog(ProcessChatMessage oProcessChatMessage, Map<Long, List<ProcessChatMessage>> aSubjToNodeMap) {
        LOG.info("createChildrenTreeCatalog start...");
        List<ProcessChatMessage> aChildResult = aSubjToNodeMap.get(oProcessChatMessage.getId());
        if (aChildResult == null || aChildResult.isEmpty()) {
            return;
        }
        for (ProcessChatMessage oMessage : aChildResult) {  
            oProcessChatMessage.setaProcessChatMessageChild(aSubjToNodeMap.get(oProcessChatMessage.getId()));
            createChildrenTreeCatalog(oMessage, aSubjToNodeMap);
        }
    }

    /**
     * Update ProcessChatMessage
     *
     * @param nID_ProcessChatMessage
     * @param sKeyGroup_Author
     * @param sBody
     * @return oProcessChatMessage
     */
    public ProcessChatMessage updateProcessChatMessage(Long nID_ProcessChatMessage, String sKeyGroup_Author, String sLoginReferent, String sBody, Long nID_Process_Activiti, String sKeyGroup) throws CommonServiceException {
        LOG.info("update ProcessChatMessage started...");
        
        boolean isEnableToUpdate = isValid(nID_ProcessChatMessage);
        LOG.info("isEnableToUpdate: {}", isEnableToUpdate);

        if (!isEnableToUpdate) {
            throw new CommonServiceException(new Exception().getMessage(),"Вибачте, Ви не можете відредагувати це повідомлення через те, що його було вилучено!");
        }
        
        ProcessChatMessage oProcessChatMessage;
        oProcessChatMessage = processChatMessageDao.findByIdExpected(nID_ProcessChatMessage);
             
        if (oProcessChatMessage.getsKeyGroup_Author().equals(sKeyGroup_Author)) {
            oProcessChatMessage.setsBody(sBody);
            processChatMessageDao.saveOrUpdate(oProcessChatMessage);
            oEmailProcessSubjectService.sendEmail_comment(nID_Process_Activiti, sKeyGroup, sKeyGroup_Author, sBody);
            return oProcessChatMessage;
        } else {
            LOG.error("The message body can not be changed by different author");
        }
        return oProcessChatMessage;

    }

    /**
     * Delete ProcessChatMessage
     *
     * @param nID_ProcessChatMessage
     * @param sKeyGroup_Author
     * @throws org.igov.service.exception.CommonServiceException
     */
    public void removeProcessChatMessage(Long nID_ProcessChatMessage, String sKeyGroup_Author) throws Exception{
        LOG.info("remove ProcessChatMessage started...");
        ProcessChatMessage oProcessChatMessage = processChatMessageDao.findByIdExpected(nID_ProcessChatMessage);
        Optional<ProcessChatMessageTree> oProcessChatMessageTree = processChatMessageTreeDao.findBy("processChatMessageParent.id", nID_ProcessChatMessage);
        if (oProcessChatMessage.getsKeyGroup_Author().equals(sKeyGroup_Author) && !oProcessChatMessageTree.isPresent()) {
            Optional<ProcessChatMessageTree> oProcessChatMessageTreeToDelete = processChatMessageTreeDao.findBy("processChatMessageChild.id", nID_ProcessChatMessage);
            if (oProcessChatMessageTreeToDelete.isPresent()) {
                processChatMessageTreeDao.delete(oProcessChatMessageTreeToDelete.get().getId());
            } else {
                processChatMessageDao.delete(nID_ProcessChatMessage);
            }
        } else {
            LOG.warn("Error occured while removing ProcessChatMessage with nID_ProcessChatMessage=%s, and sKeyGroup_Author=%s", nID_ProcessChatMessage, sKeyGroup_Author);
            throw new CommonServiceException("403", "Сообщение нельзя удалить!");
        }
    }
    
    private String getFIO(String sLogin) {     
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();        
        String sFIO = oSubjectGroup.getName();        
        return sFIO;
    }
    
    public boolean isValid(Long nId_processChatMessage) {
        boolean isEnable = true;
        
        if(!processChatMessageDao.exists(nId_processChatMessage)){
            isEnable = false;
        }

        return isEnable;
    }
    
    private void resetAuthorUrgents(String snID_Process_Activiti, String sLoginAuthor) throws JsonProcessingException{
        String sKeyStepActive = oDocumentStepService.getActiveStepName(snID_Process_Activiti);
        LOG.info("sKeyStepActive in resetAuthorUrgents {}", sKeyStepActive);
        LOG.info("snID_Process_Activiti in resetAuthorUrgents {}", snID_Process_Activiti);
        LOG.info("sLoginAuthor in resetAuthorUrgents {}", sLoginAuthor);

        if (sKeyStepActive != null) {
            DocumentStep oDocumentStep = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKeyStepActive);
            if (oDocumentStep != null) {

                DocumentStepSubjectRight oDocumentStepSubjectRight_AuthorOfComment = null;
                boolean bAdd = true;
                String sLoginProcessAuthor = (String) oRuntimeService.getVariable(snID_Process_Activiti, "sLoginAuthor");

                for (DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep.aDocumentStepSubjectRight()) {
                    LOG.info("oDocumentStepSubjectRight.getsKey_GroupPostfix {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());
                    LOG.info("oDocumentStepSubjectRight.getbUrgent {}", oDocumentStepSubjectRight.getbUrgent());
                    if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sLoginAuthor) && oDocumentStepSubjectRight.getbUrgent() != null
                            && oDocumentStepSubjectRight.getbUrgent() == true) {
                        oDocumentStepSubjectRight.setbUrgent(null);
                        Long nID = oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight).getId();
                        LOG.info("oDocumentStepSubjectRight bUrgent after saving {}",
                                oDocumentStepSubjectRightDao.findByIdExpected(nID).getbUrgent());
                        oDocumentStepSubjectRight_AuthorOfComment = oDocumentStepSubjectRight;
                        LOG.info("sLoginProcessAuthor {}", sLoginProcessAuthor);
                        break;
                    }
                }

                if (oDocumentStepSubjectRight_AuthorOfComment != null) {
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight_DocumentAuthor : oDocumentStep.aDocumentStepSubjectRight()) {
                        if (oDocumentStepSubjectRight_DocumentAuthor.getsKey_GroupPostfix().equals(sLoginProcessAuthor)) {
                            bAdd = false;
                            oDocumentStepSubjectRight_DocumentAuthor.setbUrgent(true);
                            oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_DocumentAuthor);

                        }
                    }

                    if (bAdd) {
                        oDocumentStepService.buildDefaultRule(sLoginProcessAuthor, oDocumentStepSubjectRight_AuthorOfComment, oDocumentStep, null, true, sLoginAuthor);
                    }
                }
                oProcessLinkService.syncProcessLinks(snID_Process_Activiti, sLoginAuthor);
            }
        }
    }
}
