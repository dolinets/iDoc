package org.igov.service.business.document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import javassist.NotFoundException;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.igov.io.GeneralConfig;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.model.action.vo.DocumentSubmitedUnsignedVO;
import org.igov.model.core.AbstractEntity;
import org.igov.model.core.GenericEntityDao;
import org.igov.model.document.*;
import org.igov.model.subject.SubjectContactTypeDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.email.EmailService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.business.subject.SubjectGroupTreeService;
import org.igov.service.business.subject.SubjectService;
import org.igov.service.business.util.CustomRegexPattern;
import org.igov.service.business.util.Date;
import org.igov.service.exception.CRCInvalidException;
import org.igov.service.exception.DocumentAccessException;
import org.igov.service.exception.RecordNotFoundException;
import org.igov.util.Tool;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.igov.io.fs.FileSystemData.getFileData_Pattern;

@Component("documentStepService")
@Service
public class DocumentStepService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStepService.class);

    @Autowired
    @Qualifier("documentStepDao")
    private GenericEntityDao<Long, DocumentStep> oDocumentStepDao;
    @Autowired
    private DocumentStepDao oDocumentStepDaoNew;
    @Autowired
    private GenericEntityDao<Long, DocumentStepType> oDocumentStepTypeDao;
    @Autowired
    private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;
    /*Field-backup - do not delete @Autowired
    private DocumentStepSubjectRightFieldDao oDocumentStepSubjectRightFieldDao;*/
    @Autowired
    private DocumentSubjectRightPermitionDao oDocumentSubjectRightPermitionDao;
    @Autowired
    private TaskForm oTaskForm;
    @Autowired
    private FormService oFormService;
    @Autowired
    private TaskService oTaskService;
    @Autowired
    private RuntimeService oRuntimeService;
    @Autowired
    private HistoryService oHistoryService;
    @Autowired
    protected RepositoryService repositoryService;
    @Autowired
    private IdentityService oIdentityService;
    @Autowired
    GeneralConfig generalConfig;
    @Autowired
    private ActionTaskService oActionTaskService;
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;
    @Autowired
    private UsersService oUsersService;
    @Autowired
    private ProcessLinkService oProcessLinkService;
    @Autowired
    private DocumentStepSubjectRightService oDocumentStepSubjectRightService;
    @Autowired
    private SubjectService oSubjectService;
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    @Autowired
    private SubjectContactTypeDao oSubjectContactTypeDao;
    @Autowired
    private EmailService oEmailService;
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;
        
    public List<DocumentStep> setDocumentSteps(String snID_Process_Activiti, String soJSON) {
        JSONObject oJSON = new JSONObject(soJSON);
        List<DocumentStep> aDocumentStep_Result = new ArrayList<>();
        List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = new ArrayList<>();
        // process common step if it exists
        Object oStep_Common = oJSON.opt("_");
        DocumentStepType oDocumentStepType = new DocumentStepType();
        oDocumentStepType.setId(1L);

        if (oStep_Common == null) {
            oStep_Common = oJSON.opt("_:Watch");
            oDocumentStepType.setId(7L);
        }

        LOG.info("snID_Process_Activiti {} Common step is - {}", snID_Process_Activiti, oStep_Common);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRightToSet_Common = new ArrayList<>();

        if (oStep_Common != null) {
            DocumentStep oDocumentStep_Common = mapToDocumentStep(oStep_Common);
            oDocumentStep_Common.setnOrder(0L);// common step with name "_" has
            // order 0
            oDocumentStep_Common.setsKey_Step("_");
            oDocumentStep_Common.setSnID_Process_Activiti(snID_Process_Activiti);
            oDocumentStep_Common.setoDocumentStepType(oDocumentStepType);
            List<DocumentStepSubjectRight> aDocumentStepSubjectRightToSet = oDocumentStep_Common.aDocumentStepSubjectRight();
            if (aDocumentStepSubjectRightToSet != null) {
                for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRightToSet) {
                    if (!oDocumentStepSubjectRight.getsKey_GroupPostfix().startsWith("_default_")) {

                        aDocumentStepSubjectRightToSet_Common.add(oDocumentStepSubjectRight);
                    }
                }
            }
            oDocumentStepDao.saveOrUpdate(oDocumentStep_Common);
            aDocumentStep_Result.add(oDocumentStep_Common);
        }
        // process all other steps
        // first of all we filter common step with name "_" and then just
        // convert each step from JSON to POJO
        List<String> asKey_Step = Arrays.asList(JSONObject.getNames(oJSON));
        LOG.info("asKey_Step: {}", asKey_Step);
        List<String> asKey_Step_ExcludeCommon = asKey_Step.stream().filter(sKey_Step -> !"_".equals(sKey_Step.split(":")[0]))
                .collect(Collectors.toList());
        Collections.sort(asKey_Step_ExcludeCommon, (a, b)
                -> Long.valueOf(a.split(":")[0].replaceAll("[^?0-9]+", "")) < Long.valueOf(b.split(":")[0].replaceAll("[^?0-9]+", "")) ? -1
                : Long.valueOf(a.split(":")[0].replaceAll("[^?0-9]+", "")) < Long.valueOf(b.split(":")[0].replaceAll("[^?0-9]+", "")) ? 0 : 1);
        LOG.info("List of steps: {}", asKey_Step_ExcludeCommon);

        long i = 1L;
        for (String sKey_Step : asKey_Step_ExcludeCommon) {
            String[] asKey_Step_Split = sKey_Step.split(":");
            if (asKey_Step_Split.length == 2) {
                oDocumentStepType = oDocumentStepTypeDao.findByExpected("name", asKey_Step_Split[1]);
            }
            LOG.info("sKeyStep in setDocumentSteps is: {}", sKey_Step);
            List<String> asKey_Step_Common = new ArrayList<>();
            asKey_Step_Common.add(sKey_Step);
            aDocumentSubjectRightPermition
                    .addAll(getDocumentSubjectRightPermitions(oJSON.get(sKey_Step),
                            false, asKey_Step_Common));
            DocumentStep oDocumentStep = mapToDocumentStep(oJSON.get(sKey_Step));
            oDocumentStep.setnOrder(i++);
            oDocumentStep.setsKey_Step(asKey_Step_Split[0]);
            oDocumentStep.setSnID_Process_Activiti(snID_Process_Activiti);
            oDocumentStep.setoDocumentStepType(oDocumentStepType);
            LOG.info("before add: snID_Process_Activiti is: {} sKey_Step is: {} rights size is: {}",
                    oDocumentStep.getSnID_Process_Activiti(), oDocumentStep.getsKey_Step(),
                    oDocumentStep.aDocumentStepSubjectRight().size());
            List<DocumentStepSubjectRight> aoDocumentStepSubjectRights_CloneFromCommon = getCommon_DocumentStepSubjectRights(
                    aDocumentStepSubjectRightToSet_Common, oDocumentStep);
            LOG.info(
                    "add common subjectRignts: snID_Process_Activiti is: {} sKey_Step is: {} aoDocumentStepSubjectRights_CloneFromCommon size is: {}",
                    oDocumentStep.getSnID_Process_Activiti(), oDocumentStep.getsKey_Step(),
                    aoDocumentStepSubjectRights_CloneFromCommon.size());
            if (oDocumentStep.aDocumentStepSubjectRight() == null) {
                oDocumentStep.setaDocumentStepSubjectRight(new ArrayList<>());
            }
            oDocumentStep.aDocumentStepSubjectRight().addAll(aoDocumentStepSubjectRights_CloneFromCommon);
            LOG.info("after add: snID_Process_Activiti is: {} sKey_Step is: {} rights size is: {}",
                    oDocumentStep.getSnID_Process_Activiti(), oDocumentStep.getsKey_Step(),
                    oDocumentStep.aDocumentStepSubjectRight().size());

            LOG.info("oDocumentStep is before saving {}", oDocumentStep);
            LOG.info("oDocumentStep right is before saving {}", oDocumentStep.aDocumentStepSubjectRight());
            oDocumentStep = oDocumentStepDao.saveOrUpdate(oDocumentStep);
            aDocumentStep_Result.add(oDocumentStep);
        }

        LOG.info("Result list of steps: {}", aDocumentStep_Result);

        aDocumentSubjectRightPermition.addAll(getDocumentSubjectRightPermitions(oStep_Common, oStep_Common != null, asKey_Step_ExcludeCommon));
        //LOG.info("aDocumentSubjectRightPermition size is {}", aDocumentSubjectRightPermition.size());

        LOG.info("aDocumentSubjectRightPermition isn't null");
        for (DocumentStep oDocumentStep_Result : aDocumentStep_Result) {
            LOG.info("oDocumentStep_Result sKey_Step {}", oDocumentStep_Result.getsKey_Step());
            if (oDocumentStep_Result.aDocumentStepSubjectRight() != null) {
                LOG.info("oDocumentStep_Result rights is {}", oDocumentStep_Result.aDocumentStepSubjectRight());
                for (DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep_Result.aDocumentStepSubjectRight()) {
                    List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition_new = new ArrayList<>();
                    for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                        //LOG.info("oDocumentSubjectRightPermition.getsKeyGroup_Postfix() is {}", oDocumentSubjectRightPermition.getsKeyGroup_Postfix());
                        //LOG.info("oDocumentStepSubjectRight.getsKey_GroupPostfix() is {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());

                        if ((oDocumentSubjectRightPermition.getsKeyGroup_Postfix()
                                .equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())
                                //&& (oDocumentSubjectRightPermition.getnID_DocumentStepSubjectRight() == null))
                                && (oDocumentSubjectRightPermition.getoDocumentStepSubjectRight() == null))
                                && oDocumentSubjectRightPermition.getsKey_Step().equals(oDocumentStep_Result.getsKey_Step())) {
                            oDocumentSubjectRightPermition.setoDocumentStepSubjectRight(oDocumentStepSubjectRight);
                            oDocumentSubjectRightPermitionDao.saveOrUpdate(oDocumentSubjectRightPermition);
                            LOG.info("oDocumentSubjectRightPermition saved is id: {} "
                                    + "DocumentStepSubjectRight id: {} "
                                    + "DocumentStepSubjectRight group: {}",
                                    oDocumentSubjectRightPermition.getId(),
                                    oDocumentStepSubjectRight.getId(),
                                    oDocumentStepSubjectRight.getsKey_GroupPostfix());
                            aDocumentSubjectRightPermition_new.add(oDocumentSubjectRightPermition);
                        }
                    }
                                        
                    //----OLD PERMITION SCHEMA
                    //oDocumentStepSubjectRight.setaDocumentSubjectRightPermition(aDocumentSubjectRightPermition_new);
                    //oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);---
                }
            }
        }
        return aDocumentStep_Result;
    }

    private boolean isNew_DocumentStepSubjectRight(String snID_Process_Activiti, String sKey_Step_Document,
            String sKey_GroupPostfix_New) {
        /*
        List<DocumentStep> aCheckDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                snID_Process_Activiti);
        boolean isNew = true;
        for (DocumentStep oCheckDocumentStep : aCheckDocumentStep) {
            if (oCheckDocumentStep.getsKey_Step().equals(sKey_Step_Document)) {
                return isNew_DocumentStepSubjectRights(oCheckDocumentStep, sKey_GroupPostfix_New);
            }
        }
        return isNew;
        */
        DocumentStep oCheckDocumentStep = getDocumentStep(snID_Process_Activiti, sKey_Step_Document);
        return isNew_DocumentStepSubjectRights(oCheckDocumentStep, sKey_GroupPostfix_New);
    }

    private boolean isNew_DocumentStepSubjectRights(DocumentStep oDocumentStep, String sKey_GroupPostfix_New) {
        List<DocumentStepSubjectRight> aoDocumentStepSubjectRight = oDocumentStepSubjectRightDao.findAllBy("documentStep", oDocumentStep);
        if (aoDocumentStepSubjectRight == null) {
            aoDocumentStepSubjectRight = new ArrayList<>();
        }
        
        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aoDocumentStepSubjectRight) {
            if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equalsIgnoreCase(sKey_GroupPostfix_New)) {
                //&& oDocumentStepSubjectRight.getDocumentStep().getsKey_Step().equals(oDocumentStep.getsKey_Step())) {
                LOG.info(
                        "double DocumentStepSubjectRight: snID_Process_Activiti is: {} sKey_Step is: {} sKey_GroupPostfix: {}",
                        oDocumentStep.getSnID_Process_Activiti(), oDocumentStep.getsKey_Step(),
                        oDocumentStepSubjectRight.getsKey_GroupPostfix());
                return false;
            }
        }
        return true;
    }

    private List<DocumentStepSubjectRight> getCommon_DocumentStepSubjectRights(
            List<DocumentStepSubjectRight> aDocumentStepSubjectRightToSet_Common, DocumentStep oDocumentStep) {

        List<DocumentStepSubjectRight> aoDocumentStepSubjectRight_New = new ArrayList<>();
        if (!aDocumentStepSubjectRightToSet_Common.isEmpty()) {
            for (DocumentStepSubjectRight oDocumentStepSubjectRightToSet_Common : aDocumentStepSubjectRightToSet_Common) {
                if (!isNew_DocumentStepSubjectRights(oDocumentStep,
                        oDocumentStepSubjectRightToSet_Common.getsKey_GroupPostfix())) {
                    continue;
                }
                DocumentStepSubjectRight oDocumentStepSubjectRight_New = new DocumentStepSubjectRight();
                oDocumentStepSubjectRight_New.setDocumentStep(oDocumentStep);
                oDocumentStepSubjectRight_New
                        .setsKey_GroupPostfix(oDocumentStepSubjectRightToSet_Common.getsKey_GroupPostfix());
                oDocumentStepSubjectRight_New.setbWrite(oDocumentStepSubjectRightToSet_Common.getbWrite());
                oDocumentStepSubjectRight_New.setbNeedECP(oDocumentStepSubjectRightToSet_Common.getbNeedECP());
                Object sName = oDocumentStepSubjectRightToSet_Common.getsName();
                if (sName != null) {
                    oDocumentStepSubjectRight_New.setsName((String) sName);
                }

                /*
                Field-backup - do not delete
                List<DocumentStepSubjectRightField> aoDocumentStepSubjectRightField_New = new ArrayList<>();
                for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_From : oDocumentStepSubjectRightToSet_Common
                        .getDocumentStepSubjectRightFields()) {
                    DocumentStepSubjectRightField oDocumentStepSubjectRightField_New = new DocumentStepSubjectRightField();
                    oDocumentStepSubjectRightField_New.setbWrite(oDocumentStepSubjectRightField_From.getbWrite());
                    oDocumentStepSubjectRightField_New
                            .setsMask_FieldID(oDocumentStepSubjectRightField_From.getsMask_FieldID());
                    oDocumentStepSubjectRightField_New.setDocumentStepSubjectRight(oDocumentStepSubjectRight_New);
                    aoDocumentStepSubjectRightField_New.add(oDocumentStepSubjectRightField_New);
                }
                oDocumentStepSubjectRight_New.setDocumentStepSubjectRightFields(aoDocumentStepSubjectRightField_New);*/

                if (isNew_DocumentStepSubjectRight(oDocumentStep.getSnID_Process_Activiti(),
                        oDocumentStep.getsKey_Step(), oDocumentStepSubjectRight_New.getsKey_GroupPostfix())) {
                    aoDocumentStepSubjectRight_New.add(oDocumentStepSubjectRight_New);
                    LOG.info("oDocumentStepSubjectRight: {} is added", oDocumentStepSubjectRight_New);
                }

                LOG.info(
                        "in adding: snID_Process_Activiti is: {} sKey_Step is: {} sKey_GroupPostfix is: {} right size is: {} ",
                        oDocumentStepSubjectRight_New.getDocumentStep().getSnID_Process_Activiti(),
                        oDocumentStepSubjectRight_New.getDocumentStep().getsKey_Step(),
                        oDocumentStepSubjectRight_New.getsKey_GroupPostfix());
                        //Field-backup - do not delete oDocumentStepSubjectRight_New.getDocumentStepSubjectRightFields().size());
            }
        }
        return aoDocumentStepSubjectRight_New;

    }

    private List<DocumentSubjectRightPermition> getDocumentSubjectRightPermitions(Object oStep_JSON, boolean isCommonStep, List<String> asKey_Step_ExcludeCommon) {
        List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = new ArrayList<>();

        JSONObject oStep = (JSONObject) oStep_JSON;
        LOG.info("try to parse step: {}", oStep);
        LOG.info("isCommonStep {}", isCommonStep);

        if (isCommonStep) {
            asKey_Step_ExcludeCommon.add("_");
        }

        LOG.info("stepCount {}", asKey_Step_ExcludeCommon.size());

        if (oStep == null) {
            return null;
        }

        String[] asKey_Group = JSONObject.getNames(oStep);
        if (asKey_Group != null) {
            for (String sKey_Group : asKey_Group) {
                LOG.info("sKey_Group={}", sKey_Group);
                JSONObject oGroup = oStep.optJSONObject(sKey_Group);
                JSONArray aPermition = oGroup.optJSONArray("aPermition");
                LOG.info("aPermition is {}", aPermition);

                if (aPermition != null) {
                    for (int j = 0; j < asKey_Step_ExcludeCommon.size(); j++) {
                        for (int i = 0; i < aPermition.length(); i++) {
                            LOG.info("Permition elem is {}", aPermition.getString(i));
                            DocumentSubjectRightPermition oDocumentSubjectRightPermition = new DocumentSubjectRightPermition();
                            //переделали. не актуально. TODO: удалить поле sID_Group_Activiti из базы 
                            /*String[] asPermition = aPermition.getString(i).split(":");
                            oDocumentSubjectRightPermition.setPermitionType(asPermition[0]);
                            if(asPermition.length > 1){
                                oDocumentSubjectRightPermition.setsID_Group_Activiti(asPermition[1]);
                            }*/
                            oDocumentSubjectRightPermition.setPermitionType(aPermition.getString(i));
                            oDocumentSubjectRightPermition.setsKeyGroup_Postfix(sKey_Group);
                            oDocumentSubjectRightPermition.setsKey_Step(asKey_Step_ExcludeCommon.get(j));
                            //oDocumentSubjectRightPermition.setnID_DocumentStepSubjectRight(oDocumentStepSubjectRight.getId());

                            JSONObject oPermitionAcceptor = oGroup.optJSONObject("oPermitions_AddAcceptor");
                            LOG.info("oPermitionAcceptor is {}", oPermitionAcceptor);

                            if (oPermitionAcceptor != null && aPermition.getString(i).equals("AddAcceptor")) {
                                LOG.info("oPermitionAcceptor sKeyGroupe_Source is {}", oPermitionAcceptor.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsKeyGroupeSource((String) oPermitionAcceptor.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsID_Group_Activiti((String) oPermitionAcceptor.get("sGroup_Activiti"));
                            }

                            JSONObject oPermitionVisor = oGroup.optJSONObject("oPermitions_AddVisor");
                            LOG.info("oPermitionVisor is {}", oPermitionVisor);

                            if (oPermitionVisor != null && aPermition.getString(i).equals("AddVisor")) {
                                LOG.info("oPermitionVisor sKeyGroupe_Source is {}", oPermitionVisor.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsKeyGroupeSource((String) oPermitionVisor.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsID_Group_Activiti((String) oPermitionVisor.get("sGroup_Activiti"));
                            }

                            JSONObject oPermitionViwer = oGroup.optJSONObject("oPermitions_AddViewer");
                            LOG.info("oPermitionViwer is {}", oPermitionViwer);

                            if (oPermitionViwer != null && aPermition.getString(i).equals("AddViewer")) {
                                LOG.info("oPermitionViwer sKeyGroupe_Source is {}", oPermitionViwer.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsKeyGroupeSource((String) oPermitionViwer.get("sKeyGroupe_Source"));
                                oDocumentSubjectRightPermition.setsID_Group_Activiti((String) oPermitionViwer.get("sGroup_Activiti"));
                            }

                            JSONObject oPermitionDelegate = oGroup.optJSONObject("oPermitions_Delegate");
                            LOG.info("oPermitionDelegate is {}", oPermitionDelegate);

                            if (oPermitionDelegate != null && aPermition.getString(i).equals("Delegate")) {
                                if (!oPermitionDelegate.isNull("sKeyGroupe_Source")) {
                                    oDocumentSubjectRightPermition.setsID_Group_Activiti((String) oPermitionDelegate.get("sKeyGroupe_Source"));
                                }

                                oDocumentSubjectRightPermition.setsID_Group_Activiti((String) oPermitionDelegate.get("sGroup_Activiti"));

                            }

                            JSONObject oPermitionSetUrgent = oGroup.optJSONObject("oPermitions_SetUrgent");
                            LOG.info("oPermitionDelegate is {}", oPermitionSetUrgent);

                            if (oPermitionSetUrgent != null && aPermition.getString(i).equals("SetUrgent")) {
                                oDocumentSubjectRightPermition.setSoValue(((JSONArray) oPermitionSetUrgent.get("sKey_Step")).toString());
                            }

                            aDocumentSubjectRightPermition.add(oDocumentSubjectRightPermition);
                        }
                    }
                }
            }
        }
        return aDocumentSubjectRightPermition;
    }

    private DocumentStep mapToDocumentStep(Object oStep_JSON) {
        JSONObject oStep = (JSONObject) oStep_JSON;
        LOG.info("try to parse step: {}", oStep);
        if (oStep == null) {
            return null;
        }

        DocumentStep oDocumentStep = new DocumentStep();
        String[] asKey_Group = JSONObject.getNames(oStep);
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = new ArrayList<>();
        if (asKey_Group != null) {
            for (String sKey_Group : asKey_Group) {

                JSONObject oGroup = oStep.optJSONObject(sKey_Group);
                LOG.info("group for step: {}", oGroup);

                if (oGroup == null) {
                    continue;
                }
                DocumentStepSubjectRight oDocumentStepSubjectRight = new DocumentStepSubjectRight();
                oDocumentStepSubjectRight.setsKey_GroupPostfix(sKey_Group);
                Boolean bWrite = null;

                //if((oGroup.get("bWrite") != null )&& (!"null".equals((String)oGroup.get("bWrite")))){
                if (!oGroup.isNull("bWrite")) {
                    LOG.info("oGroup.opt(bWrite) {}", oGroup.opt("bWrite"));
                    bWrite = (Boolean) oGroup.opt("bWrite");
                }

                /*if (bWrite == null) {
                    throw new IllegalArgumentException("Group " + sKey_Group + " hasn't property bWrite. Probably your json is wrong");
                }*/
                oDocumentStepSubjectRight.setbWrite(bWrite);

                Object oNeedECP = oGroup.opt("bNeedECP");
                boolean bNeedECP = false;
                if (oNeedECP != null) {
                    bNeedECP = (boolean) oNeedECP;
                }
                oDocumentStepSubjectRight.setbNeedECP(bNeedECP);

                Object sName = oGroup.opt("sName");
                if (sName != null) {
                    oDocumentStepSubjectRight.setsName((String) sName);
                }

                /* Field-backup - do not delete List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField = mapToFields(oGroup,
                        oDocumentStepSubjectRight);
                oDocumentStepSubjectRight.setDocumentStepSubjectRightFields(aDocumentStepSubjectRightField); */
                oDocumentStepSubjectRight.setDocumentStep(oDocumentStep);
                aDocumentStepSubjectRight.add(oDocumentStepSubjectRight);
            }
        }
        oDocumentStep.setaDocumentStepSubjectRight(aDocumentStepSubjectRight);
        return oDocumentStep;
    }

    public DocumentStep getDocumentStep(String snID_Process_Activiti, String sKey_Step) {
        DocumentStep oDocumentStep = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
        if (oDocumentStep == null) {
            throw new IllegalArgumentException("Cant find step by snID_Process_Activiti=" + snID_Process_Activiti + ", sKey_Step=" + sKey_Step);
        }
        return oDocumentStep;
    }

    /**
     * Удаление прав на документ по степу.
     *
     * @param snID_Process_Activiti ид процесса
     * @param sKey_Step степ с которого нужно удалять
     * @param sKey_Group персонализированная группа (логин) для которой нужно удалить права
     * @param sKey_GroupAuthor персонализированная группа (логин) по которой проверяются права на удаление
     */
    public void removeDocumentStepSubject(String snID_Process_Activiti, String sKey_Step, String sKey_Group,
                                          String sKey_GroupAuthor) {
        LOG.info("removeDocumentStepSubject started... sKey_Group={}, snID_Process_Activiti={}, sKey_Step={}",
                sKey_Group, snID_Process_Activiti, sKey_Step);
        DocumentStep oDocumentStep = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
        LOG.info("current oDocumentStep is {}", oDocumentStep);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRightFromStep = oDocumentStep.aDocumentStepSubjectRight();
        LOG.info("rights from current step: {}", aDocumentStepSubjectRightFromStep);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRightToRemove = aDocumentStepSubjectRightFromStep.stream()
                .filter(oDocumentRight -> sKey_Group.equals(oDocumentRight.getsKey_GroupPostfix()))
                .collect(Collectors.toList());
        LOG.info("aDocumentStepSubjectRightToRemove={}", aDocumentStepSubjectRightToRemove);
        //степ содержит лист райтов, чтобы удалить райт и он не восстанавливался по каскаду - нужно сначала
        //удалить райт из листа степа
        aDocumentStepSubjectRightFromStep.removeAll(aDocumentStepSubjectRightToRemove);
        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRightToRemove) {
            //проверяем права на удаление, если поле того, кто делает действие (доверителя) заполнено
            //сверяем с тем что прошло с фронта
            if (oDocumentStepSubjectRight.getsKey_GroupAuthor() != null && sKey_GroupAuthor != null
                    && !sKey_GroupAuthor.equals(oDocumentStepSubjectRight.getsKey_GroupAuthor())) {
                throw new DocumentAccessException(DocumentAccessException.ACCESS_DENIED);
            }
            oDocumentSubjectRightPermitionDao.delete(oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight.getId()));
            oDocumentStepSubjectRightDao.delete(oDocumentStepSubjectRight);
            
            DocumentStep oDocumentStep_after = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
            LOG.info("oDocumentStep_after.aDocumentStepSubjectRight() {}", oDocumentStep_after.aDocumentStepSubjectRight());
        }
        //Рекурсивный вызов!!!
        //если на удаление приходит sKey_Step = "_" считаем что нужно удалять участника так же с активного степа
        //когда добавляем просмотрщика через кнопку, ему даются права на активный степ и дефолтный (удаляем соответсвенно)
        if (sKey_Step.equals("_")) {
            String sKey_Step_Active = getActiveStepName(snID_Process_Activiti);
            LOG.info("Case when we need delete right from active step {} except default.", sKey_Step_Active);
            removeDocumentStepSubject(snID_Process_Activiti, sKey_Step_Active, sKey_Group, sKey_GroupAuthor);
        }
    }

    private void delegate(String sKey_Group, String snID_Process_Activiti, String sKey_Group_Delegate, String sKey_Step) throws Exception {
        LOG.info("delegate was started");
        cloneDocumentStepSubject(snID_Process_Activiti, sKey_Group, sKey_Group_Delegate, sKey_Step, true);
        DocumentStep oDocumentStep_Curr = null;
        /* try optimize
        List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        for (DocumentStep oDocumentStep : aDocumentStep) {
            if (oDocumentStep.getsKey_Step().equals(sKey_Step)) {
                LOG.info("sKey_Step is {}", sKey_Step);
                oDocumentStep_Curr = oDocumentStep;
            }
        }
        */
        oDocumentStep_Curr = getDocumentStep(snID_Process_Activiti, sKey_Step);
        DocumentStepSubjectRight oDocumentStepSubjectRight_From = null;
        DocumentStepSubjectRight oDocumentStepSubjectRight_To = null;

        if (oDocumentStep_Curr != null) {
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Curr = oDocumentStep_Curr.aDocumentStepSubjectRight();

            for (DocumentStepSubjectRight oDocumentStepSubjectRight_Curr : aDocumentStepSubjectRight_Curr) {
                if (sKey_Group.equals(oDocumentStepSubjectRight_Curr.getsKey_GroupPostfix())) {
                    LOG.info("sKey_Group {} is equals {} in delegateDocumentStepSubject", sKey_Group, oDocumentStepSubjectRight_Curr.getsKey_GroupPostfix());
                    oDocumentStepSubjectRight_Curr.setbWrite(null);
                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_Curr);
                    oDocumentStepSubjectRight_From = oDocumentStepSubjectRight_Curr;
                }

                if (sKey_Group_Delegate.equals(oDocumentStepSubjectRight_Curr.getsKey_GroupPostfix())) {
                    oDocumentStepSubjectRight_To = oDocumentStepSubjectRight_Curr;
                }
            }
        }
        if (oDocumentStepSubjectRight_From != null && oDocumentStepSubjectRight_To != null) {
            LOG.info("oDocumentStepSubjectRight_From id is {} sLogin is {}", oDocumentStepSubjectRight_From.getId(), oDocumentStepSubjectRight_From.getsKey_GroupPostfix());
            LOG.info("oDocumentStepSubjectRight_To id is {} sLogin is {}", oDocumentStepSubjectRight_To.getId(), oDocumentStepSubjectRight_To.getsKey_GroupPostfix());
            
            
            //----OLD PERMITION SCHEMA for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : 
            //        for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : oDocumentStepSubjectRight_From.getaDocumentSubjectRightPermition()) {
            //    oDocumentSubjectRightPermition.setoDocumentStepSubjectRight(oDocumentStepSubjectRight_To);
            //    oDocumentSubjectRightPermitionDao.saveOrUpdate(oDocumentSubjectRightPermition);
            //}
            
            for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : 
                oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight_From.getId()))
            {
                oDocumentSubjectRightPermition.setoDocumentStepSubjectRight(oDocumentStepSubjectRight_To);
                oDocumentSubjectRightPermitionDao.saveOrUpdate(oDocumentSubjectRightPermition);
            }
        }
    }

    public void buildDefaultRule(String sKey_Group_Delegate, DocumentStepSubjectRight oDocumentStepSubjectRight,
                                 DocumentStep oDocumentStep, Boolean bWriteRule, Boolean bUrgent, String sKeyGroup) {
        LOG.info("sKeyGroupeSource is null, bWriteRule {}", bWriteRule);
        DocumentStepSubjectRight oDocumentStepSubjectRight_New = new DocumentStepSubjectRight();
        oDocumentStepSubjectRight_New.setsKey_GroupPostfix(sKey_Group_Delegate);
        //сетим того, кто дает права, чтобы потом проверять на удалении (удалить может только тот кто добавил)
        oDocumentStepSubjectRight_New.setsKey_GroupAuthor(sKeyGroup);
        oDocumentStepSubjectRight_New.setbWrite(bWriteRule);
        oDocumentStepSubjectRight_New.setsName(" ");
        oDocumentStepSubjectRight_New.setbNeedECP(oDocumentStepSubjectRight.getbNeedECP());
        oDocumentStepSubjectRight_New.setDocumentStep(oDocumentStep);
        oDocumentStepSubjectRight_New.setbUrgent(bUrgent);
        oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);

        LOG.info("oDocumentStepSubjectRightDao in buildDefaultRule is {}", oDocumentStepSubjectRight_New.getId());

        /* Field-backup - do not delete List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField_New = new ArrayList<>();
        DocumentStepSubjectRightField oDocumentStepSubjectRightField_New = new DocumentStepSubjectRightField();*/

        bWriteRule = bWriteRule == null ? false : bWriteRule;

       /* Field-backup - do not delete oDocumentStepSubjectRightField_New.setbWrite(bWriteRule);
        oDocumentStepSubjectRightField_New.setsMask_FieldID("*");
        oDocumentStepSubjectRightField_New.setDocumentStepSubjectRight(oDocumentStepSubjectRight_New);
        oDocumentStepSubjectRightFieldDao.saveOrUpdate(oDocumentStepSubjectRightField_New);
        LOG.info("DocumentStepSubjectRightField in buildDefaultRule is {}", oDocumentStepSubjectRightField_New.getId());
        aDocumentStepSubjectRightField_New.add(oDocumentStepSubjectRightField_New);
        oDocumentStepSubjectRight_New.setDocumentStepSubjectRightFields(aDocumentStepSubjectRightField_New);*/
        oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_New = oDocumentStep.aDocumentStepSubjectRight();
        aDocumentStepSubjectRight_New.add(oDocumentStepSubjectRight_New);
        oDocumentStep.setaDocumentStepSubjectRight(aDocumentStepSubjectRight_New);
        oDocumentStepDao.saveOrUpdate(oDocumentStep);
    }

    private void addVisor(DocumentStep oDocumentStep, List<DocumentStepSubjectRight> aDocumentStepSubjectRight,
            String sKey_Group, String snID_Process_Activiti, String sKey_Group_Delegate, String sKey_Step) throws Exception {
        /*LOG.info("addVisor params: oDocumentStep {}  aDocumentStepSubjectRight {} sKey_Group {} "
                + "snID_Process_Activiti {} sKey_Group_Delegate {} sKey_Step{}", oDocumentStep, aDocumentStepSubjectRight,
                sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);*/

        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
            if (sKey_Group.equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                LOG.info("sKey_Group in AddVisor is {}", sKey_Group);
                               
                //----OLD PERMITION SCHEMA List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = oDocumentStepSubjectRight.getaDocumentSubjectRightPermition();
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight.getId());
                LOG.info("aDocumentSubjectRightPermition is {}", aDocumentSubjectRightPermition);

                for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                    LOG.info("oDocumentSubjectRightPermition in AddVisor is {}", oDocumentSubjectRightPermition);
                    if (oDocumentSubjectRightPermition.getPermitionType().equals("AddVisor")) {
                        if (oDocumentSubjectRightPermition.getsKeyGroupeSource() != null) {

                            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_New = cloneDocumentStepSubject(snID_Process_Activiti,
                                    oDocumentSubjectRightPermition.getsKeyGroupeSource(), sKey_Group_Delegate, sKey_Step, true);

                            for (DocumentStepSubjectRight oDocumentStepSubjectRight_New : aDocumentStepSubjectRight_New) {
                                if (sKey_Group_Delegate.equals(oDocumentStepSubjectRight_New.getsKey_GroupPostfix())) {
                                    LOG.info("{} is equals {} in delegateDocumentStepSubject", sKey_Group, oDocumentStepSubjectRight_New.getsKey_GroupPostfix());
                                    oDocumentStepSubjectRight_New.setbWrite(false);
                                    //сетим того, кто дает права, чтобы потом проверять на удалении (удалить может только тот кто добавил)
                                    oDocumentStepSubjectRight_New.setsKey_GroupAuthor(sKey_Group);
                                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);
                                    break;
                                }
                            }

                        } else {
                            buildDefaultRule(sKey_Group_Delegate, oDocumentStepSubjectRight, oDocumentStep, false, null, sKey_Group);
                        }
                        break;

                    }
                }

                break;
            }
        }
    }

    private void addViewer(DocumentStep oDocumentStep, List<DocumentStepSubjectRight> aDocumentStepSubjectRight,
            String sKey_Group, String snID_Process_Activiti, String sKey_Group_Delegate, String sKey_Step) throws Exception {
        /*LOG.info("addVisor params: oDocumentStep {}  aDocumentStepSubjectRight {} sKey_Group {} "
                + "snID_Process_Activiti {} sKey_Group_Delegate {} sKey_Step{}", oDocumentStep, aDocumentStepSubjectRight,
                sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);*/

        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
            if (sKey_Group.equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                LOG.info("sKey_Group in AddViewer is {}", sKey_Group);
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = 
                        //----OLD PERMITION SCHEMA oDocumentStepSubjectRight.getaDocumentSubjectRightPermition();
                oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight.getId());
                LOG.info("aDocumentSubjectRightPermition is {}", aDocumentSubjectRightPermition);

                for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                    LOG.info("oDocumentSubjectRightPermition in AddViewer is {}", oDocumentSubjectRightPermition);
                    if (oDocumentSubjectRightPermition.getPermitionType().equals("AddViewer")) {
                        if (oDocumentSubjectRightPermition.getsKeyGroupeSource() != null) {

                            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_New = cloneDocumentStepSubject(snID_Process_Activiti,
                                    oDocumentSubjectRightPermition.getsKeyGroupeSource(), sKey_Group_Delegate, sKey_Step, true);

                            for (DocumentStepSubjectRight oDocumentStepSubjectRight_New : aDocumentStepSubjectRight_New) {
                                if (sKey_Group_Delegate.equals(oDocumentStepSubjectRight_New.getsKey_GroupPostfix())) {
                                    LOG.info("{} is equals {} in delegateDocumentStepSubject", sKey_Group, oDocumentStepSubjectRight_New.getsKey_GroupPostfix());
                                    oDocumentStepSubjectRight_New.setbWrite(null);
                                    //сетим того, кто дает права, чтобы потом проверять на удалении (удалить может только тот кто добавил)
                                    oDocumentStepSubjectRight_New.setsKey_GroupAuthor(sKey_Group);
                                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);
                                    break;
                                }
                            }

                        } else {
                            buildDefaultRule(sKey_Group_Delegate, oDocumentStepSubjectRight, oDocumentStep, null, null, sKey_Group);
                        }
                        break;
                    }
                }

                break;
            }
        }
    }

    private void addAcceptor(DocumentStep oDocumentStep, List<DocumentStepSubjectRight> aDocumentStepSubjectRight,
            String sKey_Group, String snID_Process_Activiti, String sKey_Group_Delegate, String sKey_Step) throws Exception {
        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
            if (sKey_Group.equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                LOG.info("sKey_Group in AddAcceptor is {}", sKey_Group);
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = 
                        //----OLD PERMITION SCHEMA oDocumentStepSubjectRight.getaDocumentSubjectRightPermition();
                oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight.getId());
                LOG.info("aDocumentSubjectRightPermition is {}", aDocumentSubjectRightPermition);

                for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                    LOG.info("oDocumentSubjectRightPermition in addAcceptor is {}", oDocumentSubjectRightPermition);
                    if (oDocumentSubjectRightPermition.getPermitionType().equals("AddAcceptor")) {
                        if (oDocumentSubjectRightPermition.getsKeyGroupeSource() != null) {

                            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_New = cloneDocumentStepSubject(snID_Process_Activiti,
                                    oDocumentSubjectRightPermition.getsKeyGroupeSource(), sKey_Group_Delegate, sKey_Step, true);

                            for (DocumentStepSubjectRight oDocumentStepSubjectRight_New : aDocumentStepSubjectRight_New) {
                                if (sKey_Group_Delegate.equals(oDocumentStepSubjectRight_New.getsKey_GroupPostfix())) {
                                    LOG.info("{} is equals {} in delegateDocumentStepSubject", sKey_Group, oDocumentStepSubjectRight_New.getsKey_GroupPostfix());
                                    oDocumentStepSubjectRight_New.setbWrite(true);
                                    //сетим того, кто дает права, чтобы потом проверять на удалении (удалить может только тот кто добавил)
                                    oDocumentStepSubjectRight_New.setsKey_GroupAuthor(sKey_Group);
                                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);
                                    break;
                                }
                            }
                        } else {
                            buildDefaultRule(sKey_Group_Delegate, oDocumentStepSubjectRight, oDocumentStep, true, null, sKey_Group);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    public void setDocumentUrgent(String snID_Process_Activiti, String sKey_Step, String sKey_Group_Editor, String sKey_Group_Urgent, Boolean bUrgent) {
        LOG.info("setDocumentUrgent started with params: snID_Process_Activiti {} sKey_Step {}, "
                + "sKey_Group_Editor {} sKey_Group_Urgent {} bUrgent {}", snID_Process_Activiti, sKey_Step, 
                sKey_Group_Editor, sKey_Group_Urgent, bUrgent);
        DocumentStep oDocumentStep = null;
        /* try optimize
        List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        for (DocumentStep oDocumentStep_curr : aDocumentStep) {
            if (oDocumentStep_curr.getsKey_Step().equals(sKey_Step)) {
                oDocumentStep = oDocumentStep_curr;
            }
        }
        if (oDocumentStep == null) {
            throw new RuntimeException("can't find oDocumentStep");
        }*/
        oDocumentStep = getDocumentStep(snID_Process_Activiti, sKey_Step);
        for (DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep.aDocumentStepSubjectRight()) {
            if (sKey_Group_Urgent == null) {
                oDocumentStepSubjectRight.setbUrgent(bUrgent);
                oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);
            } else if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sKey_Group_Urgent)) {
                oDocumentStepSubjectRight.setbUrgent(bUrgent);
                oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);
            }
        }
    }

    public List<DocumentStepSubjectRight> delegateDocumentStepSubject(String snID_Process_Activiti, String sKey_Step,
            String sKey_Group, String sKey_Group_Delegate, String sOperationType) throws Exception {

        LOG.info("started... sKey_Group={}, snID_Process_Activiti={}, sKey_Step={}", sKey_Group, snID_Process_Activiti,
                sKey_Step);
        if (snID_Process_Activiti == null || "".equals(snID_Process_Activiti.trim()) || sKey_Step == null || "".equals(sKey_Step.trim())) {
            throw new IllegalArgumentException();
        }
        validateStepRights(snID_Process_Activiti, sKey_Step, sKey_Group);
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = new LinkedList<>();
        try {
            DocumentStep oDocumentStep_Current = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
            if (oDocumentStep_Current == null) {
                throw new RuntimeException("can't find oDocumentStep");
            }
            DocumentStep oDocumentStep_Default = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, "_");

            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep_Current.aDocumentStepSubjectRight();
            for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                //для проссмотренных проверяем права на дефолтном степе
                if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sKey_Group_Delegate)
                        || (sOperationType.equals("AddViewer")
                                && oDocumentStep_Default.aDocumentStepSubjectRight().stream()
                                        .anyMatch(oRight -> oRight.getsKey_GroupPostfix().equals(sKey_Group_Delegate)))) {
                    throw new DocumentAccessException(DocumentAccessException.ALREADY_PRESENT);
                }
            }

            if (sOperationType.equals("delegate")) {
                LOG.info("delegate was started...");

                List<DocumentStep> aDocumentStep_All = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
                List<DocumentStep> aDocumentStep_ToDelegate = new ArrayList<>();
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = new ArrayList<>();

                for (DocumentStep oDocumentStep_All : aDocumentStep_All) {
                    if (!oDocumentStep_All.getsKey_Step().equals("_")) {
                        LOG.info("oDocumentStep_All {}", oDocumentStep_All.getsKey_Step());
                        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_curr = oDocumentStep_All.aDocumentStepSubjectRight();

                        for (DocumentStepSubjectRight oDocumentStepSubjectRight_curr : aDocumentStepSubjectRight_curr) {
                            LOG.info("oDocumentStepSubjectRight_curr {}", oDocumentStepSubjectRight_curr);
                            if (oDocumentStepSubjectRight_curr.getsKey_GroupPostfix().equals(sKey_Group)
                                    && oDocumentStepSubjectRight_curr.getsDate() == null) {
                                LOG.info("oDocumentStep_All was added to delegating {}", oDocumentStep_All.getsKey_Step());
                                aDocumentStep_ToDelegate.add(oDocumentStep_All);
                            }

                            if (oDocumentStep_All.getsKey_Step().equals(sKey_Step) && oDocumentStepSubjectRight_curr.getsKey_GroupPostfix().equals(sKey_Group)) {
                                aDocumentSubjectRightPermition.addAll(
                                        oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight_curr.getId()));
                                        //----OLD PERMITION SCHEMA  oDocumentStepSubjectRight_curr.getaDocumentSubjectRightPermition());
                                LOG.info("aDocumentSubjectRightPermition was founded sKey_Step: {}", oDocumentStep_All.getsKey_Step());
                            }
                        }
                    }
                }

                DocumentSubjectRightPermition DocumentSubjectRightPermition_delegate = null;

                for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                    if (oDocumentSubjectRightPermition.getPermitionType().equals("Delegate")
                            && oDocumentSubjectRightPermition.getsKeyGroupeSource() != null) {
                        DocumentSubjectRightPermition_delegate = oDocumentSubjectRightPermition;
                    }
                }

                LOG.info("DocumentSubjectRightPermition_delegate {}", DocumentSubjectRightPermition_delegate);
                
                if (DocumentSubjectRightPermition_delegate == null) {
                    for (DocumentStep oDocumentStep_ToDelegate : aDocumentStep_ToDelegate) {
                        LOG.info("oDocumentStep_ToDelegate is {}", oDocumentStep_ToDelegate.getsKey_Step());
                        delegate(sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, oDocumentStep_ToDelegate.getsKey_Step());
                    }
                } else {
                    delegate(DocumentSubjectRightPermition_delegate.getsKeyGroup_Postfix(), snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
                }
            }

            if (sOperationType.equals("AddAcceptor")) {
                addAcceptor(oDocumentStep_Current, aDocumentStepSubjectRight, sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
                addRightsToCommonStep(snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
            }

            if (sOperationType.equals("AddVisor")) {
                addVisor(oDocumentStep_Current, aDocumentStepSubjectRight, sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
                addRightsToCommonStep(snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
            }

            if (sOperationType.equals("AddViewer")) {
                addViewer(oDocumentStep_Current, aDocumentStepSubjectRight, sKey_Group, snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
                addRightsToCommonStep(snID_Process_Activiti, sKey_Group_Delegate, sKey_Step);
                //Для просмотрищка на дефолтном степе устанавливаем sKey_GroupAuthor
                oDocumentStep_Default = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, "_");
                LOG.debug("oDocumentStep_Default when add viewer {} {}", sKey_Group_Delegate, oDocumentStep_Default.aDocumentStepSubjectRight());
                List<DocumentStepSubjectRight> aoRight_ByGroup = oDocumentStep_Default.aDocumentStepSubjectRight().stream()
                        .filter(oRight -> sKey_Group_Delegate.equals(oRight.getsKey_GroupPostfix()))
                        .collect(Collectors.toList());
                LOG.debug("aoRight_ByGroup after filtering {}", aoRight_ByGroup);
                aoRight_ByGroup.forEach(oRight -> oRight.setsKey_GroupAuthor(sKey_Group));
                oDocumentStepSubjectRightDao.saveOrUpdate(aoRight_ByGroup);
            }
            oActionTaskService.addIdentityLinkToDocument(snID_Process_Activiti, sKey_Group_Delegate);
        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sKey_Step=" + sKey_Step + "" + ",sKey_GroupPostfix=" + sKey_Group + "" + ")");
            LOG.error("ERROR: ", oException);
            throw oException;
        }
        oProcessLinkService.syncProcessLinks(snID_Process_Activiti, sKey_Group);

        return aDocumentStepSubjectRight_Current;
    }

    public void addRightsToCommonStep(String snID_Process_Activiti, String sKey_GroupPostfix_New,
                                      String sKey_Step_Document_To) throws Exception {
        LOG.info("addRightsToCommonStep started...");
        LOG.info("snID_Process_Activiti {}", snID_Process_Activiti);
        LOG.info("sKey_GroupPostfix_New {}", sKey_GroupPostfix_New);
        LOG.info("sKey_Step_Document_To {}", sKey_Step_Document_To);
        
        DocumentStep oDocumentStep_Saved = getDocumentStep(snID_Process_Activiti, sKey_Step_Document_To);
        DocumentStepSubjectRight oDocumentStepSubjectRight_Saved = null;

        DocumentStep oDocumentStep_Common = getDocumentStep(snID_Process_Activiti, "_");
        LOG.info("oDocumentStep_Common id {} step {}", oDocumentStep_Common.getId(), oDocumentStep_Common.getsKey_Step());
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Common = oDocumentStep_Common.aDocumentStepSubjectRight();
        LOG.info("aDocumentStepSubjectRight_Common is {}", aDocumentStepSubjectRight_Common);
        
        boolean buildDefaultRule = true;

        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight_Common) {
            if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals("_default_view")) {
                LOG.info("we don't build default rule");
                buildDefaultRule = false;
            }
        }

        if (buildDefaultRule) {
            DocumentStep oDocumentStep_Default = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, "_");
            aDocumentStepSubjectRight_Common = oDocumentStepSubjectRightDao.findAllBy("documentStep", oDocumentStep_Default);
            
            if (oDocumentStep_Saved != null) {
                for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight_Common) {
                    if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sKey_GroupPostfix_New)) {
                        oDocumentStepSubjectRight_Saved = oDocumentStepSubjectRight;
                        return;
                    }
                }
            }

            DocumentStepSubjectRight oDocumentStepSubjectRight_New = new DocumentStepSubjectRight();
            oDocumentStepSubjectRight_New.setsKey_GroupPostfix(sKey_GroupPostfix_New);
            oDocumentStepSubjectRight_New.setbWrite(null);
            if (oDocumentStepSubjectRight_Saved != null) {
                oDocumentStepSubjectRight_New.setbNeedECP(oDocumentStepSubjectRight_Saved.getbNeedECP());
                LOG.info("oDocumentStepSubjectRight_Saved id {}", oDocumentStepSubjectRight_Saved.getId());
                // Field-backup - do not delete LOG.info("oDocumentStepSubjectRight_Saved fields count {}", oDocumentStepSubjectRight_Saved.getDocumentStepSubjectRightFields().size());
                LOG.info("getsKey_GroupPostfix {}", oDocumentStepSubjectRight_Saved.getsKey_GroupPostfix());
                //LOG.info("oDocumentStepSubjectRight_Saved id {}", oDocumentStepSubjectRight_Saved);
            } else {
                oDocumentStepSubjectRight_New.setbNeedECP(false);
            }
            oDocumentStepSubjectRight_New.setDocumentStep(oDocumentStep_Common);

            oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_New);
            LOG.info("oDocumentStepSubjectRightDao in addRightsToCommonStep is {}", oDocumentStepSubjectRight_New.getId());

            //for (DocumentStepSubjectRightField oDocumentStepSubjectRightField : oDocumentStepSubjectRight_Saved.getDocumentStepSubjectRightFields()) {
            /* Field-backup - do not delete DocumentStepSubjectRightField oDocumentStepSubjectRightField_New = new DocumentStepSubjectRightField();
            oDocumentStepSubjectRightField_New.setbWrite(false);
            oDocumentStepSubjectRightField_New.setsMask_FieldID("*");
            oDocumentStepSubjectRightField_New.setDocumentStepSubjectRight(oDocumentStepSubjectRight_New);
            oDocumentStepSubjectRightFieldDao.saveOrUpdate(oDocumentStepSubjectRightField_New);
            LOG.info("DocumentStepSubjectRightField in addRightsToCommonStep is {}", oDocumentStepSubjectRightField_New.getId());*/

        } else {
            cloneDocumentStepSubject(snID_Process_Activiti, "_default_view", sKey_GroupPostfix_New, "_", true);
        }
    }

    /**
     * author - Kovylin Yegor reset rights for login if we return on the step
     * where login already was method supports field masks changing
     */
    private void reCloneRight(List<DocumentStepSubjectRight> aDocumentStepSubjectRight_To, DocumentStepSubjectRight oDocumentStepSubjectRight_From, String sKey_GroupPostfix_New) {

        try {

            //Field-backup - do not delete List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField_ToRemove = new ArrayList<>();

            for (DocumentStepSubjectRight oDocumentStepSubjectRight_To : aDocumentStepSubjectRight_To) {
                if (oDocumentStepSubjectRight_To.getsKey_GroupPostfix().equals(sKey_GroupPostfix_New)) {
                    //if sLogin already was on the step - we reset right for it    
                    oDocumentStepSubjectRight_To.setsDate(null);
                    oDocumentStepSubjectRight_To.setsDateECP(null);
                    oDocumentStepSubjectRight_To.setsKey_GroupAuthor(null);

                    /* Field-backup - do not delete List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField_From = 
                            oDocumentStepSubjectRight_From.getDocumentStepSubjectRightFields();

                    List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField_To = 
                            oDocumentStepSubjectRight_To.getDocumentStepSubjectRightFields();

                    for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_From : aDocumentStepSubjectRightField_From) {
                        //found new right by mask and bWrite
                        boolean isNewFieldRght = true;

                        for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_To : aDocumentStepSubjectRightField_To) {
                            if (oDocumentStepSubjectRightField_From.getsMask_FieldID().equals(oDocumentStepSubjectRightField_To.getsMask_FieldID())
                                    && oDocumentStepSubjectRightField_From.getbWrite().equals(oDocumentStepSubjectRightField_To.getbWrite())) {
                                isNewFieldRght = false;
                            }
                        }

                        if (isNewFieldRght) {
                            //create new field right and connect it with a step right
                            DocumentStepSubjectRightField oDocumentStepSubjectRightField_New = new DocumentStepSubjectRightField();
                            oDocumentStepSubjectRightField_New.setbWrite(oDocumentStepSubjectRightField_From.getbWrite());
                            oDocumentStepSubjectRightField_New.setsMask_FieldID(oDocumentStepSubjectRightField_From.getsMask_FieldID());
                            oDocumentStepSubjectRightField_New.setDocumentStepSubjectRight(oDocumentStepSubjectRight_To);
                            oDocumentStepSubjectRightFieldDao.saveOrUpdate(oDocumentStepSubjectRightField_New);
                        }

                    }*/

                    //oDocumentStepSubjectRight_To.setDocumentStepSubjectRightFields(aDocumentStepSubjectRightField_To);
                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_To);

                    //found masks that we must delete
                    /* Field-backup - do not delete for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_To : aDocumentStepSubjectRightField_To) {
                        if (!aDocumentStepSubjectRightField_From.stream()
                                .anyMatch(oDocumentStepSubjectRightField_From -> oDocumentStepSubjectRightField_From.getsMask_FieldID()
                                .equals(oDocumentStepSubjectRightField_To.getsMask_FieldID()))) {
                            aDocumentStepSubjectRightField_ToRemove.add(oDocumentStepSubjectRightField_To);

                        }
                    }*/

                    break;
                }
            }

            /* Field-backup - do not delete for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_ToRemove : aDocumentStepSubjectRightField_ToRemove) {
                //delete like that because of error "deleted object would be re-saved by cascade (remove deleted object from associations)"
                oDocumentStepSubjectRightFieldDao.deleteBySqlQuery(oDocumentStepSubjectRightField_ToRemove.getId());

            }*/

        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + ",sKey_GroupPostfix_New=" + sKey_GroupPostfix_New
                    + " )");
            LOG.error("ERROR: ", oException);
            throw oException;
        }
    }

    public List<DocumentStepSubjectRight> cloneDocumentStepSubject(String snID_Process_Activiti,
            String sKey_GroupPostfix, String sKey_GroupPostfix_New, String sKey_Step_Document_To, boolean bReClone)
            throws Exception {

        LOG.info("cloneDocumentStepSubject started sKey_GroupPostfix={}, snID_Process_Activiti={},"
                + " sKey_GroupPostfix_New={}, sKey_Step_Document={}", sKey_GroupPostfix, snID_Process_Activiti,
                sKey_GroupPostfix_New, sKey_Step_Document_To);

        String sKey_Step_Document_From = sKey_Step_Document_To;
        List<DocumentStepSubjectRight> resultList = new ArrayList<>();
        try {
            Set<String> asID_Group_Activiti_New = oSubjectGroupTreeService.getHumanGroupByGroup(sKey_GroupPostfix_New);
            LOG.info("asID_Group_Activiti_New is {}", asID_Group_Activiti_New);
            /* try optimize
            if (sKey_GroupPostfix.startsWith("_default_")) {
                sKey_Step_Document_From = "_";
            }
            List<DocumentStep> aDocumentStep_From = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                    snID_Process_Activiti);
            LOG.info("aDocumentStep={}", aDocumentStep_From);
            List<DocumentStep> aDocumentStep_To = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                    snID_Process_Activiti);
            LOG.info("aDocumentStep={}", aDocumentStep_To);
            final String SKEY_STEP_DOCUMENT_FROM = sKey_Step_Document_From;
            DocumentStep oDocumentStep_From = aDocumentStep_From.stream().filter(o -> SKEY_STEP_DOCUMENT_FROM == null
                    ? o.getnOrder().equals(1) : o.getsKey_Step().equals(SKEY_STEP_DOCUMENT_FROM)).findAny()
                    .orElse(null);
            LOG.info("oDocumentStep_From={}", oDocumentStep_From);
            if (oDocumentStep_From == null) {
                throw new IllegalStateException("There is no active Document Step, process variable sKey_Step_Document="
                        + sKey_Step_Document_From);
            }
            final String SKEY_STEP_DOCUMENT_TO = sKey_Step_Document_To;
            DocumentStep oDocumentStep_To = aDocumentStep_To.stream().filter(o -> SKEY_STEP_DOCUMENT_TO == null
                    ? o.getnOrder().equals(1) : o.getsKey_Step().equals(SKEY_STEP_DOCUMENT_TO)).findAny().orElse(null);
            LOG.info("oDocumentStep_To={}", oDocumentStep_To);
            //LOG.info("Step rights before clonning {}", oDocumentStep_To.aDocumentStepSubjectRight());
            if (oDocumentStep_To == null) {
                throw new IllegalStateException("There is no active Document Step, process variable sKey_Step_Document="
                        + sKey_Step_Document_To);
            }*/
            DocumentStep oDocumentStep_To = getDocumentStep(snID_Process_Activiti, sKey_Step_Document_To);
            if (sKey_GroupPostfix.startsWith("_default_")) {
                sKey_Step_Document_From = "_";
            }
            DocumentStep oDocumentStep_From = getDocumentStep(snID_Process_Activiti, sKey_Step_Document_From);

            List<String> asID_Group_Activiti_New_Selected = new LinkedList<>();
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_From = oDocumentStep_From.aDocumentStepSubjectRight();
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_To = oDocumentStep_To.aDocumentStepSubjectRight();

            DocumentStepSubjectRight oDocumentStepSubjectRight_From = null;
            for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight_From) {
                if (sKey_GroupPostfix.equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                    oDocumentStepSubjectRight_From = oDocumentStepSubjectRight;
                    break;
                }
            }
            if (oDocumentStepSubjectRight_From == null) {
                throw new Exception("Can't find etalonn oDocumentStepSubjectRight_From");
            }
            LOG.info("!!! sKey_GroupPostfix: {} oDocumentStepSubjectRight_From.getsKey_GroupPostfix(): {}",
                    sKey_GroupPostfix, oDocumentStepSubjectRight_From.getsKey_GroupPostfix());

            /* try optimize
            List<DocumentStep> aCheckDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                    snID_Process_Activiti);
            for (DocumentStep oDocumentStep_check : aCheckDocumentStep) {
                if (oDocumentStep_check.getsKey_Step().equals(sKey_Step_Document_To)) {
                    LOG.info("oDocumentStep_check id: {}", oDocumentStep_check.getId());
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight_check = oDocumentStep_check.aDocumentStepSubjectRight();
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight_check : aDocumentStepSubjectRight_check) {
                        LOG.info("oDocumentStepSubjectRight_check is {}", oDocumentStepSubjectRight_check);
                        LOG.info("oDocumentStepSubjectRight_check getDocumentStep id: {} sKey: {}",
                                oDocumentStepSubjectRight_check.getDocumentStep().getId(), oDocumentStepSubjectRight_check.getDocumentStep().getsKey_Step());
                    }
                }
            }*/

            for (String sID_Group_Activiti_New : asID_Group_Activiti_New) {
                if (isNew_DocumentStepSubjectRight(snID_Process_Activiti, oDocumentStep_To.getsKey_Step(),
                        sID_Group_Activiti_New)) {
                    LOG.info("isNew_DocumentStepSubjectRight case.");
                    asID_Group_Activiti_New_Selected.add(sID_Group_Activiti_New);
                } else if (bReClone) {
                    LOG.info("bReClone case.");
                    reCloneRight(aDocumentStepSubjectRight_To, oDocumentStepSubjectRight_From, sID_Group_Activiti_New);
                } else {
                    LOG.info("skip sKey_GroupPostfix_New: {} sKey_GroupPostfix: {}", sID_Group_Activiti_New,
                            oDocumentStep_To.getsKey_Step());
                }
            }

            for (String sID_Group_Activiti_New_Selected : asID_Group_Activiti_New_Selected) {
                LOG.info("sID_Group_Activiti_New_Selected {}", sID_Group_Activiti_New_Selected);
                DocumentStepSubjectRight oDocumentStepSubjectRight_New = new DocumentStepSubjectRight();
                oDocumentStepSubjectRight_New.setsKey_GroupPostfix(sID_Group_Activiti_New_Selected);
                oDocumentStepSubjectRight_New.setbWrite(oDocumentStepSubjectRight_From.getbWrite());
                oDocumentStepSubjectRight_New.setbNeedECP(oDocumentStepSubjectRight_From.getbNeedECP());
                Object sName = oDocumentStepSubjectRight_From.getsName();
                if (sName != null) {
                    oDocumentStepSubjectRight_New.setsName((String) sName);
                }
                /* Field-backup - do not delete List<DocumentStepSubjectRightField> aDocumentStepSubjectRightField_New = new LinkedList<>();

                for (DocumentStepSubjectRightField oDocumentStepSubjectRightField_From : oDocumentStepSubjectRight_From
                        .getDocumentStepSubjectRightFields()) {
                    DocumentStepSubjectRightField oDocumentStepSubjectRightField_New = new DocumentStepSubjectRightField();
                    oDocumentStepSubjectRightField_New.setbWrite(oDocumentStepSubjectRightField_From.getbWrite());
                    oDocumentStepSubjectRightField_New
                            .setsMask_FieldID(oDocumentStepSubjectRightField_From.getsMask_FieldID());
                    oDocumentStepSubjectRightField_New.setDocumentStepSubjectRight(oDocumentStepSubjectRight_New);
                    aDocumentStepSubjectRightField_New.add(oDocumentStepSubjectRightField_New);
                }
                oDocumentStepSubjectRight_New.setDocumentStepSubjectRightFields(aDocumentStepSubjectRightField_New);*/
                oDocumentStepSubjectRight_New.setDocumentStep(oDocumentStep_To);
                LOG.info("right for step: {}", oDocumentStepSubjectRight_New);

                aDocumentStepSubjectRight_To.add(oDocumentStepSubjectRight_New);
                oDocumentStep_To.setaDocumentStepSubjectRight(aDocumentStepSubjectRight_To);
                resultList.add(oDocumentStepSubjectRight_New);
                LOG.info("aDocumentStepSubjectRight_To before saving is {} ", aDocumentStepSubjectRight_To);
                oDocumentStepDao.saveOrUpdate(oDocumentStep_To);

                addRightsToCommonStep(snID_Process_Activiti, sID_Group_Activiti_New_Selected, sKey_Step_Document_To);

            }
            LOG.info("Step rights after clonning {}", oDocumentStep_To.aDocumentStepSubjectRight());

            if (oDocumentStepSubjectRight_From.getsKey_GroupPostfix().startsWith("_default_")) {

                for (String sID_Group_Activiti_New_Selected : asID_Group_Activiti_New_Selected) {
                    LOG.info("sID_Group_Activiti_New_Selected for permition in cloneRights is {}", sID_Group_Activiti_New_Selected);
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep_To.aDocumentStepSubjectRight();

                    DocumentStepSubjectRight oDocumentStepSubjectRight_saved = null;

                    for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                        if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sID_Group_Activiti_New_Selected)) {
                            oDocumentStepSubjectRight_saved = oDocumentStepSubjectRight;
                            LOG.info("sKey_GroupPostfix_New for permition in cloneRights is {}", sID_Group_Activiti_New_Selected);
                            break;
                        }
                    }

                    List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = 
                             //----OLD PERMITION SCHEMA  oDocumentStepSubjectRight_From.getaDocumentSubjectRightPermition();
                    oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight_From.getId());

                    //List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition_new = new ArrayList<>();

                    for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition) {
                        DocumentSubjectRightPermition oDocumentSubjectRightPermition_new = new DocumentSubjectRightPermition();
                        oDocumentSubjectRightPermition_new.setPermitionType(oDocumentSubjectRightPermition.getPermitionType());
                        oDocumentSubjectRightPermition_new.setsID_Group_Activiti(oDocumentSubjectRightPermition.getsID_Group_Activiti());
                        oDocumentSubjectRightPermition_new.setsKeyGroupeSource(oDocumentSubjectRightPermition.getsKeyGroupeSource());
                        //oDocumentSubjectRightPermition_new.setnID_DocumentStepSubjectRight(oDocumentStepSubjectRight_saved.getId());
                        oDocumentSubjectRightPermition_new.setSoValue(oDocumentSubjectRightPermition.getSoValue());
                        oDocumentSubjectRightPermition_new.setoDocumentStepSubjectRight(oDocumentStepSubjectRight_saved);
                        oDocumentSubjectRightPermition_new.setsKeyGroup_Postfix(sID_Group_Activiti_New_Selected);
                        if (oDocumentStepSubjectRight_saved != null) {
                            LOG.info("oDocumentStepSubjectRight_saved id for permition in cloneRights is {}", oDocumentStepSubjectRight_saved.getId());
                            oDocumentSubjectRightPermitionDao.saveOrUpdate(oDocumentSubjectRightPermition_new);
                        }
                        //aDocumentSubjectRightPermition_new.add(oDocumentSubjectRightPermition_new);
                        LOG.info("oDocumentSubjectRightPermition_new id for permition in cloneRights is {}", oDocumentSubjectRightPermition_new.getId());
                    }

                    //----OLD PERMITION SCHEMAif (oDocumentStepSubjectRight_saved != null) {
                        // oDocumentStepSubjectRight_saved.setaDocumentSubjectRightPermition(aDocumentSubjectRightPermition_new);
                        //oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight_saved);
                    //}
                }
            }
            LOG.info("cloneDocumentStepSubject finished!");

        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sKey_GroupPostfix=" + sKey_GroupPostfix + "" + ",sKey_GroupPostfix_New=" + sKey_GroupPostfix_New
                    + "" + ",sKey_Step_Document_To=" + sKey_Step_Document_To + ")");
            LOG.error("ERROR: ", oException);
            throw oException;
        }

        return resultList;
    }

    // TODO: Нужно выпилять из БП
    @Deprecated
    public List<DocumentStepSubjectRight> cloneDocumentStepSubject(String snID_Process_Activiti,
            String sKey_GroupPostfix, String sKey_GroupPostfix_New, String sKey_Step_Document_To) throws Exception {
        return cloneDocumentStepSubject(snID_Process_Activiti, sKey_GroupPostfix, sKey_GroupPostfix_New,
                sKey_Step_Document_To, true); // Todo: set bReClone=false after
        // corecting BA
    }

    public List<DocumentStepSubjectRight> cloneDocumentStepFromTable(String snID_Process_Activiti, String sKey_Group,
            String sID_Field, String sKey_Step, boolean bReClone) throws Exception {
        return cloneDocumentStepFromTable(snID_Process_Activiti, sKey_Group, sID_Field, sKey_Step, bReClone, null);
    }

    public List<DocumentStepSubjectRight> cloneDocumentStepFromTable(String snID_Process_Activiti, String sKey_Group,
            String sID_Field, String sKey_Step, boolean bReClone, String sID_FieldTable) throws Exception {

        LOG.info("started...");
        LOG.info("sKey_Group={}, snID_Process_Activiti={}, sID_Field={}, sKey_Step={}", sKey_Group,
                snID_Process_Activiti, sID_Field, sKey_Step);
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Return = new ArrayList<>();
        try {
            List<String> asLogin = oTaskForm.getValuesFromTableField(snID_Process_Activiti, sID_Field, sID_FieldTable);
            for (String sLogin : asLogin) {
                List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = cloneDocumentStepSubject(
                        snID_Process_Activiti, sKey_Group, sLogin, sKey_Step, bReClone);
                aDocumentStepSubjectRight_Return.addAll(aDocumentStepSubjectRight_Current);
            }
            aDocumentStepSubjectRight_Return.forEach(oDocumentStepSubject -> oDocumentStepSubject.setsID_Field(sID_Field));
            oDocumentStepSubjectRightDao.saveOrUpdate(aDocumentStepSubjectRight_Return);
        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sKey_GroupPostfix=" + sKey_Group + "" + ",sID_Field=" + sID_Field + ""
                    + ",sKey_Step_Document_To=" + sKey_Step + ")");
            LOG.error("ERROR: ", oException);
            throw oException;
        }
        return aDocumentStepSubjectRight_Return;
    }

    // TODO: Нужно выпилять из БП
    @Deprecated
    public List<DocumentStepSubjectRight> cloneDocumentStepFromTable(String snID_Process_Activiti,
            String sKey_GroupPostfix, String sID_Field, String sKey_Step_Document_To) throws Exception {
        return cloneDocumentStepFromTable(snID_Process_Activiti, sKey_GroupPostfix, sID_Field, sKey_Step_Document_To,
                false, null);
    }

    public List<DocumentStepSubjectRight> syncDocumentSubmitedsByField(String snID_Process_Activiti,
            String sKey_Group_Default, String sID_Field, String sID_FieldTable, String sKey_Step, boolean bReClone)
            throws Exception {

        return syncDocumentSubmitedsByField(snID_Process_Activiti, sKey_Group_Default, sID_Field, sID_FieldTable,
                sKey_Step, bReClone, false);
    }

    public List<DocumentStepSubjectRight> syncDocumentSubmitedsByField(String snID_Process_Activiti,
            String sKey_Group_Default, String sID_Field, String sID_FieldTable, String sKey_Step, boolean bReClone,
            boolean bRemoveSign) throws Exception {

        LOG.info("syncDocumentSubmitedsByField started with snID_Process_Activiti={}, sKey_Group_Default={},"
                + " sID_Field={}, sID_FieldTable={}, sKey_Step={}, bReClone={}, bRemoveSign={}", snID_Process_Activiti,
                sKey_Group_Default, sID_Field, sID_FieldTable, sKey_Step, bReClone, bRemoveSign);
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Return = new ArrayList<>();
        try {
            //список логинов у которые должны быть на степе c полем sID_Field
            List<String> asLoginFromField = oTaskForm.getValuesFromTableField(snID_Process_Activiti, sID_Field, sID_FieldTable);
            LOG.info("asLoginFromField={}", asLoginFromField);

            //находим степ на котором находится документ
            DocumentStep oDocumentStep = getDocumentStep(snID_Process_Activiti, sKey_Step);
            LOG.info("oDocumentStep={}", oDocumentStep);

            //вытаскиваем всех, кто фактически находится на степе
            List<DocumentStepSubjectRight> aDocumentStepSubjectRightFromStep = oDocumentStep.aDocumentStepSubjectRight();
            LOG.info("aDocumentStepSubjectRight size={}, aDocumentStepSubjectRight={}",
                    aDocumentStepSubjectRightFromStep.size(), aDocumentStepSubjectRightFromStep);
            //выбираем с полем в рамках, которого происходит синхронизация
            List<DocumentStepSubjectRight> aDocumentStepSubjectWithFieldId = aDocumentStepSubjectRightFromStep.stream()
                    .filter(oDocumentStepSubject
                            -> oDocumentStepSubject.getsID_Field() != null
                    && oDocumentStepSubject.getsID_Field().equals(sID_Field))
                    .collect(Collectors.toList());
            LOG.info("aDocumentStepSubjectWithFieldId={}", aDocumentStepSubjectWithFieldId);
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight_ForRemove = new LinkedList();
            for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectWithFieldId) {
                LOG.info("oDocumentStepSubjectRight={}", oDocumentStepSubjectRight);
                String sCurrentLogin = oDocumentStepSubjectRight.getsKey_GroupPostfix();
                //все DocumentStepSubjectRight чьих логинов нет в asLoginFromField - на удаление
                if (asLoginFromField.contains(sCurrentLogin)) {
                    aDocumentStepSubjectRight_Return.add(oDocumentStepSubjectRight);
                    //убираем логины у которых уже есть права
                    asLoginFromField.remove(sCurrentLogin);
                } else {
                    aDocumentStepSubjectRight_ForRemove.add(oDocumentStepSubjectRight);
                }
            }
            //очистить подпись у DocumentStepSubjectRight, которые уже есть на степе
            if (bRemoveSign) {
                aDocumentStepSubjectRight_Return.forEach(oDocumentStepSubject -> {
                    oDocumentStepSubject.setsDate(null);
                    oDocumentStepSubject.setsDateECP(null);
                    oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubject);
                });
            }
            //удаление тех кого уже нет на степе
            LOG.info("aDocumentStepSubjectRight_ForRemove={}", aDocumentStepSubjectRight_ForRemove);
            for (DocumentStepSubjectRight oDocumentStepSubject : aDocumentStepSubjectRight_ForRemove) {
                removeDocumentStepSubject(snID_Process_Activiti, sKey_Step, oDocumentStepSubject.getsKey_GroupPostfix(), null);
            }
            //клонируем права для оставшихся новых логинов
            for (String sLogin : asLoginFromField) {
                List<DocumentStepSubjectRight> aDocumentStepSubjectRight_New = cloneDocumentStepSubject(
                        snID_Process_Activiti, sKey_Group_Default, sLogin, sKey_Step, bReClone);
                aDocumentStepSubjectRight_Return.addAll(aDocumentStepSubjectRight_New);
            }
            //сетим ид поля в рамках, которого прошла синхронизация
            aDocumentStepSubjectRight_Return.forEach(oDocumentStepSubject -> oDocumentStepSubject.setsID_Field(sID_Field));
            oDocumentStepSubjectRightDao.saveOrUpdate(aDocumentStepSubjectRight_Return);
        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sKey_GroupPostfix=" + sKey_Group_Default + "" + ",sID_Field=" + sID_Field + ""
                    + ",sKey_Step_Document_To=" + sKey_Step + ")");
            LOG.error("ERROR: ", oException);
            throw oException;
        }
        return aDocumentStepSubjectRight_Return;
    }

    public Boolean cancelDocumentSubmit(String snID_Process_Activiti, String sKey_Step, String sKey_Group)
            throws Exception {

        LOG.info("started...");
        LOG.info("snID_Process_Activiti={}, sKey_Step={}, sKey_Group={}", snID_Process_Activiti, sKey_Step, sKey_Group);

        Boolean bCanceled = false;

        try {

            DocumentStep oDocumentStep = getDocumentStep(snID_Process_Activiti, sKey_Step);
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();
            LOG.info("aDocumentStepSubjectRight is {}", aDocumentStepSubjectRight);

            for (int i = 0; i < aDocumentStepSubjectRight.size(); i++) {
                DocumentStepSubjectRight oDocumentStepSubjectRight = aDocumentStepSubjectRight.get(i);
                if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sKey_Group)) {
                    if (oDocumentStepSubjectRight.getsDate() != null && 
                            !oDocumentStepSubjectRight.getoDocumentStepSubjectSignType().getsID().equals("needlessly")) {
                        LOG.info("DocumentStepSubjectRight equals _From with date {}: " + "sKey_Group is: {}",
                                oDocumentStepSubjectRight.getsKey_GroupPostfix());
                        oDocumentStepSubjectRight.setsDate(null);
                        oDocumentStepSubjectRight.setsDateECP(null);
                        oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);
                        bCanceled = true;
                        break;
                    }
                }
            }

        } catch (Exception oException) {
            LOG.error("ERROR:" + oException.getMessage() + " (" + "snID_Process_Activiti=" + snID_Process_Activiti + ""
                    + ",sKey_GroupPostfix=" + sKey_Group + "" + ",sKey_Step_Document=" + sKey_Step + ")");
            LOG.error("ERROR: ", oException);
            throw oException;
        }

        return bCanceled;
    }

    /* Field-backup - do not delete private List<DocumentStepSubjectRightField> mapToFields(JSONObject group, DocumentStepSubjectRight rightForGroup) {
        List<DocumentStepSubjectRightField> resultFields = new ArrayList<>();
        String[] fieldNames = JSONObject.getNames(group);
        LOG.info("fields for right: {}", Arrays.toString(fieldNames));
        if (fieldNames != null) {
            for (String fieldName : fieldNames) {
                if (fieldName == null || fieldName.equals("bWrite")) {
                    continue;
                }
                if (fieldName.contains("Read")) {
                    JSONArray masks = group.optJSONArray(fieldName);
                    LOG.info("Read branch for masks: {}", masks);
                    for (int i = 0; masks.length() > i; i++) {
                        String mask = masks.getString(i);
                        DocumentStepSubjectRightField field = new DocumentStepSubjectRightField();
                        field.setsMask_FieldID(mask);
                        field.setbWrite(false);
                        field.setDocumentStepSubjectRight(rightForGroup);
                        resultFields.add(field);
                    }
                }
                if (fieldName.contains("Write")) {
                    JSONArray masks = group.getJSONArray(fieldName);
                    LOG.info("Write branch for masks: {}", masks);
                    for (int i = 0; masks.length() > i; i++) {
                        String mask = masks.getString(i);
                        DocumentStepSubjectRightField field = new DocumentStepSubjectRightField();
                        field.setsMask_FieldID(mask);
                        field.setbWrite(true);
                        field.setDocumentStepSubjectRight(rightForGroup);
                        resultFields.add(field);
                    }
                }
            }
        }
        return resultFields;
    }*/

    public List<Map<String, Object>> getDocumentStepLogins(String snID_Process_Activiti, Boolean bHistory) throws NotFoundException {// JSONObject

        Map<String, Object> mProcessVariable = new HashMap<>();
        if (bHistory) {
            LOG.info("getDocumentStepLogins history start");
            List<HistoricVariableInstance> aoHistoryVariables = oHistoryService.createHistoricVariableInstanceQuery()
                    .processInstanceId(snID_Process_Activiti)
                    .list();
            for (HistoricVariableInstance oHistoricVariable : aoHistoryVariables) {
                mProcessVariable.put(oHistoricVariable.getVariableName(), oHistoricVariable.getValue());
            }
            LOG.info("mProcessVariable={}", mProcessVariable);
        } else {
            LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
            List<Task> aTaskActive = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active()
                    .list();
            if (aTaskActive.size() < 1 || aTaskActive.get(0) == null) {
                return new ArrayList<>();
                //throw new IllegalArgumentException("Process with ID: " + snID_Process_Activiti + " has no active task.");
            }
            Task oTaskActive = aTaskActive.get(0);
            ProcessInstance oProcessInstance = oRuntimeService.createProcessInstanceQuery()
                    .processInstanceId(snID_Process_Activiti).active().singleResult();
            mProcessVariable = oProcessInstance.getProcessVariables();
            LOG.info("mProcessVariable={}", mProcessVariable);

            List<FormProperty> aProperty = oFormService.getTaskFormData(oTaskActive.getId()).getFormProperties();
            for (FormProperty oProperty : aProperty) {
                mProcessVariable.put(oProperty.getId(), oProperty.getValue());
            }
            LOG.info("mProcessVariable(added)={}", mProcessVariable);
        }
        String sKey_Step_Document = (String) mProcessVariable.get("sKey_Step_Document");
        LOG.info("sKey_Step_Document={}", sKey_Step_Document);

        List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        LOG.info("aDocumentStep={}", aDocumentStep);

        DocumentStep oDocumentStep_Common = aDocumentStep.stream().filter(o -> o.getsKey_Step().equals("_")).findAny()
                .orElse(null);
        LOG.info("oDocumentStep_Common={}", oDocumentStep_Common);
        
        List<Map<String, Object>> amReturn = new LinkedList<>();

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = new LinkedList<>();
        if (oDocumentStep_Common != null) {
            aDocumentStepSubjectRight.addAll(oDocumentStep_Common.aDocumentStepSubjectRight());
        }

        List<DocumentStep> oDocumentStep_ExceptCommon = aDocumentStep.stream()
                .filter(o -> !o.getsKey_Step().equals("_")).collect(Collectors.toList());
        for (DocumentStep oDocumentStep : oDocumentStep_ExceptCommon) {
            aDocumentStepSubjectRight.addAll(oDocumentStep.aDocumentStepSubjectRight());
        }

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
            Map<String, Object> mParamDocumentStepSubjectRight = new HashMap<>();
            mParamDocumentStepSubjectRight.put("sKeyStep", oDocumentStepSubjectRight.getDocumentStep().getsKey_Step());
            mParamDocumentStepSubjectRight.put("sKey_GroupAuthor", oDocumentStepSubjectRight.getsKey_GroupAuthor());
            mParamDocumentStepSubjectRight.put("sDate", oDocumentStepSubjectRight.getsDate() == null ? ""
                    : formatter.print(oDocumentStepSubjectRight.getsDate()));// "2016-05-15
            mParamDocumentStepSubjectRight.put("sDateECP", oDocumentStepSubjectRight.getsDateECP() == null ? ""
                    : formatter.print(oDocumentStepSubjectRight.getsDateECP()));// "2016-05-15
            mParamDocumentStepSubjectRight.put("bNeedECP", oDocumentStepSubjectRight.getbNeedECP() == null ? false
                    : oDocumentStepSubjectRight.getbNeedECP());
            mParamDocumentStepSubjectRight.put("bWrite", oDocumentStepSubjectRight.getbWrite());// false
            mParamDocumentStepSubjectRight.put("bUrgent", oDocumentStepSubjectRight.getbUrgent());// false
            mParamDocumentStepSubjectRight.put("sName",
                    oDocumentStepSubjectRight.getsName() == null ? "" : oDocumentStepSubjectRight.getsName());// "Главный
            // контроллирующий"
            String sID_Group = oDocumentStepSubjectRight.getsKey_GroupPostfix();
            List<Map<String, Object>> amUserProperty = oUsersService.getamUserProperty(sID_Group, false);
            mParamDocumentStepSubjectRight.put("aUser", amUserProperty);
            LOG.info("amUserProperty={}", amUserProperty);
            String sLogin = oDocumentStepSubjectRight.getsLogin();
            LOG.info("sLogin={}", sLogin);
            String sLogin_Referent = "", sFIO_Referent = "";
            if (sLogin != null) {
                User oUser = oIdentityService.createUserQuery().userId(sLogin).singleResult();
                if (oUser != null) {
                    sLogin_Referent = oUser.getId();
                    sFIO_Referent = oUser.getFirstName() + " " + oUser.getLastName();
                }
            }
            mParamDocumentStepSubjectRight.put("sLogin_Referent", sLogin_Referent);
            mParamDocumentStepSubjectRight.put("sFIO_Referent", sFIO_Referent);
            mParamDocumentStepSubjectRight.put("oDocumentStepType", oDocumentStepSubjectRight.getDocumentStep().getoDocumentStepType());
            mParamDocumentStepSubjectRight.put("oDocumentStepSubjectSignType", oDocumentStepSubjectRight.getoDocumentStepSubjectSignType());

            LOG.info("mParamDocumentStepSubjectRight={}", mParamDocumentStepSubjectRight);
            amReturn.add(mParamDocumentStepSubjectRight);
        }
        LOG.info("amReturn={}", amReturn);

        return amReturn;
    }

    public Map<String, Object> getDocumentStepRights(String sLogin, String snID_Process_Activiti) {// JSONObject
        // assume that we can have only one active task per process at the same time
        LOG.info("getDocumentStepRights started sLogin={}, snID_Process_Activiti={}", sLogin, snID_Process_Activiti);
        long startTime = System.nanoTime();

        Map<String, Object> mReturn = new HashMap<>();

        List<DocumentStep> aoDocumentStep_All = oDocumentStepDao
                .findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        LOG.info("aoDocumentStep_All={}", aoDocumentStep_All);

        DocumentStep oDocumentStep_Common = aoDocumentStep_All.stream()
                .filter(o -> o.getsKey_Step().equals("_"))
                .findAny().orElse(null);
        LOG.info("oDocumentStep_Common={}", oDocumentStep_Common);

        HistoricVariableInstance oHistoricVariable = oHistoryService.createHistoricVariableInstanceQuery()
                .processInstanceId(snID_Process_Activiti)
                .variableName("sKey_Step_Document")
                .singleResult();
        String sKey_Step_Document = String.valueOf(oHistoricVariable.getValue());
        LOG.info("sKey_Step_Document={}", sKey_Step_Document);

        DocumentStep oDocumentStep_Active = aoDocumentStep_All.stream()
                .filter(oDocumentStep -> sKey_Step_Document == null ? oDocumentStep.getnOrder().equals(1)
                        : oDocumentStep.getsKey_Step().equals(sKey_Step_Document))
                .findAny().orElse(null);
        LOG.info("oDocumentStep_Active={}", oDocumentStep_Active);

        if (oDocumentStep_Active == null) {
            throw new IllegalStateException(
                    "There is no active Document Step, process variable sKey_Step_Document=" + sKey_Step_Document);
        }

        List<Group> aGroup = oIdentityService.createGroupQuery().groupMember(sLogin).list();
        
        Set<String> asID_Group = new HashSet<>();
        if (aGroup.size() > 1) {
            //in case of refernt submit and author editing wih referent croup on the active current step
            asID_Group.add(sLogin);
        } else {
            if (aGroup != null) {
                aGroup.stream().forEach(group -> asID_Group.add(group.getId()));
            }
            LOG.debug("sLogin={}, asID_Group={}", sLogin, asID_Group);
        }
        long stopTime = System.nanoTime();
        LOG.info("getDocumentStepRights 1st block time execution is: " + String.format("%,12d", (stopTime - startTime)));
        startTime = System.nanoTime();

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Common = new LinkedList<>();
        if (oDocumentStep_Common != null) {
            aDocumentStepSubjectRight_Common = oDocumentStep_Common.aDocumentStepSubjectRight();
        }
        LOG.info("aDocumentStepSubjectRight_Common {}", aDocumentStepSubjectRight_Common);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Active = oDocumentStep_Active
                .aDocumentStepSubjectRight()
                .stream()
                .filter(oDocumentStep -> asID_Group.contains(oDocumentStep.getsKey_GroupPostfix()))
                .collect(Collectors.toList());
        LOG.info("aDocumentStepSubjectRight_Active={}", aDocumentStepSubjectRight_Active);

        List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition = new ArrayList<>();

        for (DocumentStepSubjectRight oDocumentStepSubjectRight_Active : aDocumentStepSubjectRight_Active) {
            if (oDocumentStepSubjectRight_Active.getsKey_GroupPostfix().equals(sLogin) && 
                    oDocumentStepSubjectRight_Active.getsDate() == null) //when several logins on the step - we get rights only for active
            {
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition_finded  = 
                        oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight_Active.getId());
                
                LOG.info("oDocumentStepSubjectRight_Active.getaDocumentSubjectRightPermition {}", aDocumentSubjectRightPermition_finded);
                 aDocumentSubjectRightPermition.addAll(aDocumentSubjectRightPermition_finded);
                 
                //----OLD PERMITION SCHEMA  
                /*LOG.info("oDocumentStepSubjectRight_Active.getaDocumentSubjectRightPermition {}",
                        oDocumentStepSubjectRight_Active.getaDocumentSubjectRightPermition());
                
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition_finded
                        = oDocumentStepSubjectRight_Active.getaDocumentSubjectRightPermition();
                LOG.info("aDocumentSubjectRightPermition_finded size is {}", aDocumentSubjectRightPermition_finded.size());
                
                aDocumentSubjectRightPermition.addAll(aDocumentSubjectRightPermition_finded);*/
                LOG.info("try to find DocumentSubjectRightPermition by id: {}", oDocumentStepSubjectRight_Active.getId());
                break;
            }
        }
        for (DocumentStepSubjectRight oDocumentStepSubjectRight_Common : aDocumentStepSubjectRight_Common) {
            if (oDocumentStepSubjectRight_Common.getsKey_GroupPostfix().equals(sLogin)) {
                List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition_finded
                       //----OLD PERMITION SCHEMA = oDocumentStepSubjectRight_Common.getaDocumentSubjectRightPermition();
                        = oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight_Common.getId());
                
                for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : aDocumentSubjectRightPermition_finded) {
                    if (oDocumentSubjectRightPermition.getsKeyGroupeSource() != null
                            && oDocumentSubjectRightPermition.getsKeyGroupeSource().equals("_default_view")) {
                        aDocumentSubjectRightPermition.add(oDocumentSubjectRightPermition);
                        break;
                    }
                }
            }
        }

        steps:
        {
            for (DocumentStep oDocumentStep : aoDocumentStep_All) {
                LOG.info("oDocumentStep is {} {}", oDocumentStep.getsKey_Step(), oDocumentStep.getId());
                LOG.info("oDocumentStep_Active is {} {}", oDocumentStep_Active.getsKey_Step(), oDocumentStep_Active.getId());

                if (oDocumentStep_Active.getId() > oDocumentStep.getId()) {
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep.aDocumentStepSubjectRight()) {
                        LOG.info("oDocumentStepSubjectRight grouup is {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());
                        if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sLogin)) {
                            for (DocumentSubjectRightPermition oDocumentSubjectRightPermition : 
                                    oDocumentSubjectRightPermitionDao.findAllBy("oDocumentStepSubjectRight.id", oDocumentStepSubjectRight.getId()))
                                    //----OLD PERMITION SCHEMA oDocumentStepSubjectRight.getaDocumentSubjectRightPermition()) {
                                {
                                LOG.info("oDocumentSubjectRightPermition PermitionType {}", oDocumentSubjectRightPermition.getPermitionType());
                                if (oDocumentSubjectRightPermition.getPermitionType() != null
                                        && oDocumentSubjectRightPermition.getPermitionType().equals("SetUrgent")) {
                                    aDocumentSubjectRightPermition.add(oDocumentSubjectRightPermition);
                                    break steps;
                                }
                            }
                        }
                    }
                }
            }
        }
        LOG.info("aDocumentSubjectRightPermition {}", aDocumentSubjectRightPermition);
        mReturn.put("aDocumentSubjectRightPermition", aDocumentSubjectRightPermition);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Merged = new ArrayList<>();

        boolean isCommonStepLogin = true;

        for (DocumentStepSubjectRight oDocumentStepSubjectRight_Active : aDocumentStepSubjectRight_Active) {
            if (asID_Group.contains(oDocumentStepSubjectRight_Active.getsKey_GroupPostfix())) {
                isCommonStepLogin = false;
                aDocumentStepSubjectRight_Merged.add(oDocumentStepSubjectRight_Active);
            }
        }

        if (isCommonStepLogin) {
            for (DocumentStepSubjectRight oDocumentStepSubjectRight_Common : aDocumentStepSubjectRight_Common) {
                if (asID_Group.contains(oDocumentStepSubjectRight_Common.getsKey_GroupPostfix())) {
                    aDocumentStepSubjectRight_Merged.add(oDocumentStepSubjectRight_Common);
                }
            }
        }

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = aDocumentStepSubjectRight_Merged;
        aDocumentStepSubjectRight.addAll(aDocumentStepSubjectRight_Active);
        LOG.info("aDocumentStepSubjectRight={}", aDocumentStepSubjectRight);

        Boolean bWrite = null;
        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
            if (oDocumentStepSubjectRight.getbWrite() != null) {
                if (bWrite == null) {
                    bWrite = false;
                }

                bWrite = bWrite || oDocumentStepSubjectRight.getbWrite();
            }
        }
        mReturn.put("bWrite", bWrite);
        LOG.info("bWrite={}", bWrite);

        List<String> asID_Field_Read = new LinkedList<>();
        List<String> asID_Field_Write = new LinkedList<>();
        
        /* Field-backup - do not delete
        List<Task> aTask = oTaskService.createTaskQuery()
                .processInstanceId(snID_Process_Activiti)
                .active()
                .list();

        List<FormProperty> aFormProperty = new ArrayList<>();
        List<String> aVariable = new ArrayList<>();

        if (!aTask.isEmpty()) {
            String snID_Task = aTask.get(0).getId();
            aFormProperty.addAll(oFormService.getTaskFormData(snID_Task).getFormProperties());

            for (FormProperty oFormProperty : aFormProperty) {
                aVariable.add(oFormProperty.getId());
            }

        } else {
            List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService.createHistoricVariableInstanceQuery().processInstanceId(snID_Process_Activiti).list();

            for (HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance) {
                aVariable.add(oHistoricVariableInstance.getVariableName());
            }
        }

        LOG.info("aFormProperty last size is {}", aFormProperty.size());

        stopTime = System.nanoTime();
        LOG.info("getDocumentStepRights 2nd block time execution is: " + String.format("%,12d", (stopTime - startTime)));
        startTime = System.nanoTime();
        LOG.info("total aDocumentStepSubjectRight size is: " + aDocumentStepSubjectRight.size());

        Map<String, boolean[]> resultMap = new HashMap<>();
        for (String sProperty : aVariable) {
            groupSearch:
            {
                for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                    LOG.info("oDocumentStepSubjectRight.getsKey_GroupPostfix()={}",
                            oDocumentStepSubjectRight.getsKey_GroupPostfix());

                    long loopStartTime = System.nanoTime();

                    for (DocumentStepSubjectRightField oDocumentStepSubjectRightField : oDocumentStepSubjectRight
                            .getDocumentStepSubjectRightFields()) {
                        String sMask = oDocumentStepSubjectRightField.getsMask_FieldID();
                        LOG.info("sMask={}", sMask);
                        LOG.info("total DocumentStepSubjectRightFields size is: "
                                + oDocumentStepSubjectRight.getDocumentStepSubjectRightFields().size());
                        if (sMask != null) {
                            Boolean bNot = false;
                            if (sMask.startsWith("!")) {
                                bNot = true;
                                sMask = sMask.substring(1);
                            }
                            Boolean bEqual = false;
                            Boolean bEndsWith = false;
                            Boolean bStartWith = false;
                            Boolean bAll = "*".equals(sMask);
                            if (!bAll) {
                                if (sMask.startsWith("*")) {
                                    bEndsWith = true;
                                    sMask = sMask.substring(1);
                                }
                                if (sMask.endsWith("*")) {
                                    bStartWith = true;
                                    sMask = sMask.substring(0, sMask.length() - 1);
                                }
                                if (!bStartWith && !bEndsWith && sMask.length() > 0) {
                                    bEqual = true;
                                }
                            }
                            LOG.info("bEndsWith={},bStartWith={},bAll={},bNot={}", bEndsWith, bStartWith, bAll, bNot);
                            long scLoopStartTime = System.nanoTime();

                            String sID = sProperty;
                            Boolean bFound = false;
                            if (bStartWith && bEndsWith) {
                                bFound = sID.contains(sMask);
                            } else if (bStartWith) {
                                bFound = sID.startsWith(sMask);
                            } else if (bEndsWith) {
                                bFound = sID.endsWith(sMask);
                            } else if (bEqual) {
                                bFound = sID.equalsIgnoreCase(sMask);
                            }

                            LOG.info("sID={},bFound={},bAll={}", sID, bFound, bAll);
                            if (bAll || bFound) {
                                Boolean bWriteField = oDocumentStepSubjectRightField.getbWrite();
                                if (bNot) {
                                    resultMap.remove(sID);
                                } else if (bWriteField) {
                                    if (resultMap.containsKey(sID)) {
                                        resultMap.replace(sID, new boolean[]{true, false});
                                    } else {
                                        resultMap.put(sID, new boolean[]{true, false});
                                    }

                                    break groupSearch;

                                } else {
                                    resultMap.put(sID, new boolean[]{false, true});
                                }
                                LOG.info("bWriteField={}", bWriteField);
                            }

                            long scLoopStopTime = System.nanoTime();
                            LOG.info("2st loop time execution in getDocumentStepRights 3th block is: "
                                    + String.format("%,12d", (scLoopStopTime - scLoopStartTime)));
                        }
                    }

                    long loopStopTime = System.nanoTime();
                    LOG.info("1st loop time execution in getDocumentStepRights 3th block is: "
                            + String.format("%,12d", (loopStopTime - loopStartTime)));
                }
            }
        }

        stopTime = System.nanoTime();
        LOG.info("getDocumentStepRights 3th block time execution is: " + String.format("%,12d", (stopTime - startTime)));
        startTime = System.nanoTime();

        LOG.info("asID_Field_Write(before)={}", asID_Field_Write);
        LOG.info("asID_Field_Read(before)={}", asID_Field_Read);
        LOG.info("asID_Field_Write(before) size={}", asID_Field_Write.size());
        LOG.info("asID_Field_Read(before) size={}", asID_Field_Read.size());

        TreeSet<String> asUnique_ID_Field_Write = new TreeSet<>(asID_Field_Write);
        TreeSet<String> asUnique_ID_Field_Read = new TreeSet<>(asID_Field_Read);

        LOG.info("asUnique_ID_Field_Write ={}", asUnique_ID_Field_Write);
        LOG.info("asUnique_ID_Field_Write ={}", asUnique_ID_Field_Read);
        LOG.info("asID_Field_Write size={}", asUnique_ID_Field_Write.size());
        LOG.info("asID_Field_Read size={}", asUnique_ID_Field_Read.size());

        List<String> asNewID_Field_Read = new LinkedList<>();
        List<String> asNewID_Field_Write = new LinkedList<>();

        for (String key : resultMap.keySet()) {
            boolean[] resultArray = resultMap.get(key);
            if (resultArray[0]) {
                asNewID_Field_Write.add(key);
            } else {
                asNewID_Field_Read.add(key);
            }
        }
        
        mReturn.put("asID_Field_Write", asNewID_Field_Write);
        mReturn.put("asID_Field_Read", asNewID_Field_Read);
        */
        LOG.info("asNewID_Field_Write = {}", asID_Field_Write);
        LOG.info("asNewID_Field_Read ={}", asID_Field_Read);

        stopTime = System.nanoTime();
        LOG.info("getDocumentStepRights 4th block time execution is: " + String.format("%,12d", (stopTime - startTime)));

        return mReturn;
    }

    public void syncDocumentGroups(DelegateTask delegateTask, List<DocumentStep> aDocumentStep) {

        Set<String> asGroup = new HashSet<>();
        for (DocumentStep oDocumentStep : aDocumentStep) {
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();
            if (aDocumentStepSubjectRight != null) {

                for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                    asGroup.add(oDocumentStepSubjectRight.getsKey_GroupPostfix());
                    delegateTask.deleteCandidateGroup(oDocumentStepSubjectRight.getsKey_GroupPostfix());
                }
            }
        }
        LOG.debug("asGroup in DocumentInit_iDoc {}", asGroup);

        List<String> asGroup_Old = new ArrayList<>();
        Set<IdentityLink> groupsOld = delegateTask.getCandidates();
        groupsOld.stream().forEach((groupOld) -> {
            asGroup_Old.add(groupOld.getGroupId());
        });
        LOG.info("asGroup_Old before setting: {} delegateTask: {}", asGroup_Old, delegateTask.getId());

        List<DocumentStep> aDocumentStep_All = oDocumentStepDao.findAllBy("snID_Process_Activiti", delegateTask.getProcessInstanceId());

        DocumentStep oDocumentStep_Common = null;

        if (aDocumentStep_All != null) {
            for (DocumentStep oDocumentStep : aDocumentStep_All) {
                if (oDocumentStep.getsKey_Step().equals("_")) {
                    oDocumentStep_Common = oDocumentStep;
                    break;
                }
            }
        }

        if (oDocumentStep_Common != null) {
            for (DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep_Common.aDocumentStepSubjectRight()) {
                LOG.debug("oDocumentStepSubjectRight group in candidate adding is {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());
                if ((!oDocumentStepSubjectRight.getsKey_GroupPostfix().startsWith("_default_"))
                        && (asGroup_Old.isEmpty() || !asGroup_Old.contains(oDocumentStepSubjectRight.getsKey_GroupPostfix()))) {
                    LOG.debug("Group added to candidate is {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());
                    asGroup.add(oDocumentStepSubjectRight.getsKey_GroupPostfix());
                }
            }
        }

        for (String sGroup : asGroup) {
            List<User> aUser = oIdentityService.createUserQuery().memberOfGroup(sGroup).list();
            for (User oUser : aUser) {
                LOG.info("user in usergroup {}", oUser.getId());
            }
        }

        delegateTask.addCandidateGroups(asGroup);

        List<String> asGroup_New = new ArrayList<>();
        Set<IdentityLink> groupsNew = delegateTask.getCandidates();
        groupsNew.stream().forEach((groupNew) -> {
            asGroup_New.add(groupNew.getGroupId());
        });
        LOG.info("asGroup_New after setting: {} delegateTask: {}", asGroup_New, delegateTask.getId());
    }

    // public void checkDocumentInit(DelegateExecution execution) throws
    // IOException, URISyntaxException {//JSONObject
    public List<DocumentStep> checkDocumentInit(DelegateExecution execution, String sKey_GroupPostfix,
            String sKey_GroupPostfix_New) throws IOException, URISyntaxException, Exception {
        // assume that we can have only one active task per process at the same
        // time
        String snID_Process_Activiti = execution.getId();
        List<DocumentStep> aResDocumentStep = new ArrayList<>();
        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        String sID_BP = execution.getProcessDefinitionId();
        LOG.info("sID_BP={}", sID_BP);
        if (sID_BP != null && sID_BP.contains(":")) {
            String[] as = sID_BP.split("\\:");
            sID_BP = as[0];
            LOG.info("FIX(:) sID_BP={}", sID_BP);
        }
        if (sID_BP != null && sID_BP.contains(".")) {
            String[] as = sID_BP.split("\\.");
            sID_BP = as[0];
            LOG.info("FIX(.) sID_BP={}", sID_BP);
        }

        Map<String, Object> mProcessVariable = execution.getVariables();
        String sKey_Step_Document = mProcessVariable.containsKey("sKey_Step_Document")
                ? (String) mProcessVariable.get("sKey_Step_Document") : null;
        if ("".equals(sKey_Step_Document)) {
            sKey_Step_Document = null;
        }
        LOG.info("BEFORE:sKey_Step_Document={}", sKey_Step_Document);

        if (sKey_Step_Document == null) {

            String sPath = "document/" + sID_BP + ".json";
            LOG.info("sPath={}", sPath);
            byte[] aByteDocument = getFileData_Pattern(sPath);
            if (aByteDocument != null && aByteDocument.length > 0) {
                String soJSON = soJSON = Tool.sData(aByteDocument);
                List<DocumentStep> aDocumentStep = setDocumentSteps(snID_Process_Activiti, soJSON);
                if (aDocumentStep != null && aDocumentStep.size() > 1) {
                    sKey_Step_Document = aDocumentStep.get(1).getsKey_Step();
                    LOG.info("AFTER:snID_Process_Activiti={} sKey_Step_Document={}", snID_Process_Activiti, sKey_Step_Document);
                    oRuntimeService.setVariable(snID_Process_Activiti, "sKey_Step_Document", sKey_Step_Document);
                }
            }

            if (sKey_GroupPostfix != null && !sKey_GroupPostfix.trim().equals("") && sKey_GroupPostfix_New != null
                    && !sKey_GroupPostfix_New.trim().equals("")) {
                LOG.info("start user id is {}", sKey_GroupPostfix_New);
                LOG.info("sKey_GroupPostfix is {}", sKey_GroupPostfix);

                List<DocumentStepSubjectRight> aDocumentStepSubjectRight = cloneDocumentStepSubject(
                        snID_Process_Activiti, sKey_GroupPostfix, sKey_GroupPostfix_New, sKey_Step_Document);
            }

        }
        List<DocumentStep> aResultDocumentStep = new ArrayList<>();
        /*try optimize
        aResultDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                snID_Process_Activiti);
        LOG.info("aResultDocumentStep in initDocChecker is {}", aResultDocumentStep.size());
        for (DocumentStep oDocumentStep : aResultDocumentStep) {
            if (oDocumentStep.getsKey_Step().equals(sKey_Step_Document)) {
                LOG.info("founded DocumentStep in initDocChecker is {}", oDocumentStep);
                aResDocumentStep.add(oDocumentStep);
            }
        }
        LOG.info("aResDocumentStep in initDocChecker is {}", aResDocumentStep);
        return aResDocumentStep;
        */
        DocumentStep oDocumentStep = getDocumentStep(snID_Process_Activiti, sKey_Step_Document);
        aResultDocumentStep.add(oDocumentStep);

        return aResultDocumentStep;
    }

    public void notifyAllDocumentStepOwnersViaEmail(String snID_Process_Activiti, String sKey_Step, String sHead, String sContentKey) {
        String sContentValue = extractEmailContent(snID_Process_Activiti, sContentKey);
        sContentValue = formatEmailContent(sContentValue, snID_Process_Activiti, sKey_Step);
        sHead = formatEmailHeader(sHead, snID_Process_Activiti);
        
        List<String> asOwnerID = getDocumentStepRightOwnerIds(snID_Process_Activiti, sKey_Step);
        notifyLoginsViaEmail(asOwnerID, sHead, sContentValue);
    }
    
    public void notifyDocumentStepOwnerViaEmail(String snID_Process_Activiti, String sKey_Step, String sOwnerID, String sHead, String sContentKey) {
        String sContentValue = extractEmailContent(snID_Process_Activiti, sContentKey);
        sContentValue = formatEmailContent(sContentValue, snID_Process_Activiti, sKey_Step);        
        
        List<String> asOwnerID = getDocumentStepRightOwnerIds(snID_Process_Activiti, sKey_Step)
            .stream()
            .filter(s -> s.equalsIgnoreCase(sOwnerID))
            .distinct()
            .collect(Collectors.toList());
        if (asOwnerID.isEmpty()) {
            throw new RuntimeException("'" + sOwnerID + "' has no rights to document: '" + snID_Process_Activiti + "', step: '" + sKey_Step + "'");
        }
        notifyLoginsViaEmail(asOwnerID, sHead, sContentValue);
    }
    
    private String extractEmailContent(String snID_Process_Activiti, String sKey) {
        Map<String, Object> aoProcessVariable = getProcessVariables(snID_Process_Activiti);
        String sContentValue = (String) aoProcessVariable.get(sKey);
        if (Strings.isNullOrEmpty(sContentValue)) {
            throw new RuntimeException("Email content missing, check process variable: " + sKey);
        }
        return sContentValue;
    }
    
    private String formatEmailContent(String sContent, String snID_Process_Activiti, String sKey_Step) {
        //String sID = snID_Process_Activiti + "_" + sKey_Step;
        String sID_Order = generalConfig.getOrderId_ByProcess(Long.valueOf(snID_Process_Activiti));
        String sURL = generalConfig.getsDocumentURL(sID_Order);
        return sContent
                .replace(CustomRegexPattern.TAG_sID_Order, sID_Order)
                .replace("[sID_Order_URL]", sURL);
    }
    
    private String formatEmailHeader(String sHead, String snID_Process_Activiti) {        
        String sID_Order = generalConfig.getOrderId_ByProcess(Long.valueOf(snID_Process_Activiti));        
        return sHead.replace(CustomRegexPattern.TAG_sID_Order, sID_Order);
    }
    
    private void notifyLoginsViaEmail(List<String> asLogin, String sHead, String sContent) {
        LOG.info("email, head: {}, content: {}", sHead, sContent);
        if (asLogin.size() > 0) {
            List<Long> anID_Subject = oSubjectGroupDao
                    .findAllByInValues("sID_Group_Activiti", asLogin)
                    .stream()
                    .map(SubjectGroup::getoSubject)
                    .map(AbstractEntity::getId)
                    .distinct()
                    .collect(Collectors.toList());
            LOG.info("owners count: {}", anID_Subject.size());
            Long nID_EmailType = oSubjectContactTypeDao.getEmailType().getId();
        
            List<String> asEmail = oSubjectService.getSubjectContactValues(anID_Subject, nID_EmailType);
            asEmail.forEach(email -> {
                LOG.info("send email to: {}", email);
                oEmailService.sendEmail(email, sHead, sContent, null);
            });
            LOG.info("owner emails count: {}", asEmail.size());
        }
    }
    
    private List<String> getDocumentStepRightOwnerIds(String snID_Process_Activiti, String sKey_Step) {
        return oDocumentStepDao
                .findAllBy("snID_Process_Activiti", snID_Process_Activiti) // process steps
                .stream()
                .filter(doc -> doc.getsKey_Step().equalsIgnoreCase(sKey_Step)) // required step
                .flatMap(doc -> doc.aDocumentStepSubjectRight().stream()) // step rights
                .map(DocumentStepSubjectRight::getsKey_GroupPostfix) // owners
                .distinct()
                .collect(Collectors.toList());
    }
    
    private Map<String, Object> getProcessVariables(String snID_Process_Activiti) {
        return oHistoryService.createHistoricProcessInstanceQuery()
                .processInstanceId(snID_Process_Activiti.trim())
                .includeProcessVariables()
                .singleResult()
                .getProcessVariables();
    }
    
    // 3.4) setDocumentStep(snID_Process_Activiti, bNext) //проставить номер шаг
    // (bNext=true > +1 иначе -1) в поле таски с id=sKey_Step_Document
    public String setDocumentStep(String snID_Process_Activiti, String sKey_Step) throws Exception {// JSONObject
        // assume that we can have only one active task per process at the same
        // time
        LOG.info("sKey_Step={}, snID_Process_Activiti={}", sKey_Step, snID_Process_Activiti);
        HistoricProcessInstance oProcessInstance = oHistoryService.createHistoricProcessInstanceQuery()
                .processInstanceId(snID_Process_Activiti.trim()).includeProcessVariables().singleResult();
        if (oProcessInstance != null) {
            Map<String, Object> mProcessVariable = oProcessInstance.getProcessVariables();
            String sKey_Step_Document = mProcessVariable.containsKey("sKey_Step_Document")
                    ? (String) mProcessVariable.get("sKey_Step_Document") : null;
            if ("".equals(sKey_Step_Document)) {
                sKey_Step_Document = null;
            }

            if (sKey_Step_Document == null) {
                sKey_Step_Document = (String) oRuntimeService.getVariable(snID_Process_Activiti, "sKey_Step_Document");
            }

            LOG.debug("BEFORE:sKey_Step_Document={}", sKey_Step_Document);

            List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                    snID_Process_Activiti);
            LOG.debug("aDocumentStep={}", aDocumentStep);

            if (sKey_Step != null) {
                sKey_Step_Document = sKey_Step;
            } else if (sKey_Step_Document == null) {
                if (aDocumentStep.size() > 1) {
                    aDocumentStep.get(1);
                } else if (aDocumentStep.size() > 0) {
                    aDocumentStep.get(0);
                } else {
                }
            } else {
                Long nOrder = null;
                for (DocumentStep oDocumentStep : aDocumentStep) {
                    if (nOrder != null) {
                        sKey_Step_Document = oDocumentStep.getsKey_Step();
                        break;
                    }
                    if (nOrder == null && sKey_Step_Document.equals(oDocumentStep.getsKey_Step())) {
                        nOrder = oDocumentStep.getnOrder();
                    }
                }
            }

            LOG.debug("AFTER:sKey_Step_Document={}", sKey_Step_Document);
            oRuntimeService.setVariable(snID_Process_Activiti, "sKey_Step_Document", sKey_Step_Document);
        } else {
            throw new Exception("oProcessInstance is null snID_Process_Activiti = " + snID_Process_Activiti);
        }

        return "";
    }

    public Map<String, Object> isDocumentStepSubmitedAll(String snID_Process, String sKey_Step, boolean bNeedECPCheck) {
        LOG.info("isDocumentStepSubmitedAll: snID_Process {}, sKey_Step {} ...", snID_Process, sKey_Step);
        Map<String, Object> mReturn = new HashMap<>();
        long countSubmited = 0;
        long countNotSubmited = 0;
        DocumentStep oFindedDocumentStep = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process, sKey_Step);

        if (oFindedDocumentStep == null) {
           throw new IllegalArgumentException("DocumentStep not found");
        } else {
            boolean bSubmitedAll = true;
            for (DocumentStepSubjectRight oDocumentStepSubjectRight : oFindedDocumentStep.aDocumentStepSubjectRight()) {
                Boolean bWrite = oDocumentStepSubjectRight.getbWrite();
                if (bWrite != null) {
                    DateTime sDate = oDocumentStepSubjectRight.getsDate();
                    DateTime sDateECP = oDocumentStepSubjectRight.getsDateECP();
                    Boolean bNeedECP = oDocumentStepSubjectRight.getbNeedECP();
                    if (sDate == null) {
                        bSubmitedAll = false;
                        countNotSubmited++;
                    } else {
                        if (bNeedECPCheck && bNeedECP && sDateECP == null) {
                            bSubmitedAll = false;
                            countNotSubmited++;
                        } else {
                            countSubmited++;
                        }
                    }
                }
            }
            //sKey_Step = null проверяем подпись ЭЦП на всех степах
            if (bNeedECPCheck && !oDocumentStepSubjectRightService
                    .getUnsignedRights(snID_Process, null, true).isEmpty()) {
                bSubmitedAll = false;
            }

            mReturn.put("bSubmitedAll", bSubmitedAll);
            mReturn.put("nCountSubmited", countSubmited);
            mReturn.put("nCountNotSubmited", countNotSubmited);
            mReturn.put("nCountSubmitePlan", (countSubmited + countNotSubmited));

            LOG.info("mReturn in isDocumentStepSubmitedAll {}", mReturn);

            return mReturn;
        }
    }

    public List<DocumentSubmitedUnsignedVO> getDocumentSubmitedUnsigned(String sLogin)
            throws JsonProcessingException, RecordNotFoundException, ParseException {

        List<DocumentSubmitedUnsignedVO> aResDocumentSubmitedUnsigned = new ArrayList<>();

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStepSubjectRightDao.findAllBy("sLogin",
                sLogin);
        //LOG.info("aDocumentStepSubjectRight in method getDocumentSubmitedUnsigned = {}", aDocumentStepSubjectRight);
        DocumentStepSubjectRight oFindedDocumentStepSubjectRight;

        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {

            if (oDocumentStepSubjectRight != null) {

                DateTime sDateECP = oDocumentStepSubjectRight.getsDateECP();
                LOG.info("sDateECP = ", oDocumentStepSubjectRight.getsDateECP());
                DateTime sDate = oDocumentStepSubjectRight.getsDate();
                LOG.info("sDate = ", oDocumentStepSubjectRight.getsDate());

                long sDS = oDocumentStepSubjectRight.getsDate().getMillis();
                String setsDateSubmit = Date.convertMilliSecondsToFormattedDate(sDS);

                Boolean bNeedECP = oDocumentStepSubjectRight.getbNeedECP();
                // проверяем, если даты ецп нет, но есть дата подписания - нашли
                // нужный объект, который кладем в VO-обьект-обертку
                if (bNeedECP != null && bNeedECP != false && sDateECP == null) {
                    if (sDate != null) {
                        oFindedDocumentStepSubjectRight = oDocumentStepSubjectRight;
                        //LOG.info("oFindedDocumentStepSubjectRight= {}", oFindedDocumentStepSubjectRight);
                        // Достаем nID_Process_Activiti у найденного
                        // oDocumentStepSubjectRight через DocumentStep
                        String snID_Process_Activiti = oFindedDocumentStepSubjectRight.getDocumentStep()
                                .getSnID_Process_Activiti();
                        LOG.info("snID_Process of oFindedDocumentStepSubjectRight: {}", snID_Process_Activiti);
                        // Получаем sID_Order через generalConfig
                        long nID_Process = Long.valueOf(snID_Process_Activiti);
                        int nID_Server = generalConfig.getSelfServerId();
                        String sID_Order = generalConfig.getOrderId_ByProcess(nID_Server, nID_Process);

                        HistoricProcessInstance oHistoricProcessInstance = oHistoryService
                                .createHistoricProcessInstanceQuery().processInstanceId(snID_Process_Activiti)
                                .singleResult();
                        LOG.info("oHistoricProcessInstance.id={}, snID_Process_Activiti={}", oHistoricProcessInstance.getId(), snID_Process_Activiti);

                        if (oHistoricProcessInstance != null) {
                            ProcessDefinition oProcessDefinition = repositoryService.createProcessDefinitionQuery()
                                    .processDefinitionId(oHistoricProcessInstance.getProcessDefinitionId())
                                    .singleResult();

                            if (oProcessDefinition != null) {
                                String sProcessName = oProcessDefinition.getName();

                                // вытаскиваем дату создания процесса
                                // Date sDateCreateProcess =
                                // oProcessInstance.getStartTime();
                                long sDCP = oHistoricProcessInstance.getStartTime().getTime();
                                String sDateCreateProcess = Date.convertMilliSecondsToFormattedDate(sDCP);
                                LOG.info("sDateCreateProcess ", sDateCreateProcess);
                                // вытаскиваем название бп

                                // String sNameBP = oProcessInstance.getName();
                                // LOG.info("sNameBP {}", sNameBP);
                                // вытаскиваем список активных тасок по процесу
                                List<Task> aTask = oTaskService.createTaskQuery()
                                        .processInstanceId(oHistoricProcessInstance.getId()).active().list();
                                if (aTask.size() < 1 || aTask.get(0) == null) {
                                    continue;
                                }
                                // берем первую
                                Task oTaskCurr = aTask.get(0);
                                LOG.info("oTaskCurr ={} ", oTaskCurr);
                                // вытаскиваем дату создания таски
                                long sDCUT = oTaskCurr.getCreateTime().getTime();
                                String sDateCreateUserTask = Date.convertMilliSecondsToFormattedDate(sDCUT);
                                // Date sDateCreateUserTask =
                                // oTaskCurr.getCreateTime();
                                LOG.info("sDateCreateUserTask = ", oTaskCurr.getCreateTime());
                                // и ее название
                                String sUserTaskName = oTaskCurr.getName();
                                // Создаем обьект=обертку, в который сетим
                                // нужные
                                // полученные поля
                                DocumentSubmitedUnsignedVO oDocumentSubmitedUnsignedVO = new DocumentSubmitedUnsignedVO();

                                oDocumentSubmitedUnsignedVO
                                        .setoDocumentStepSubjectRight(oFindedDocumentStepSubjectRight);
                                oDocumentSubmitedUnsignedVO.setsNameBP(sProcessName);
                                oDocumentSubmitedUnsignedVO.setsUserTaskName(sUserTaskName);
                                oDocumentSubmitedUnsignedVO.setsDateCreateProcess(sDateCreateProcess);
                                oDocumentSubmitedUnsignedVO.setsDateCreateUserTask(sDateCreateUserTask);
                                oDocumentSubmitedUnsignedVO.setsDateSubmit(setsDateSubmit);
                                oDocumentSubmitedUnsignedVO.setsID_Order(sID_Order);

                                aResDocumentSubmitedUnsigned.add(oDocumentSubmitedUnsignedVO);
                                //LOG.info("aResDocumentSubmitedUnsigned = {}", aResDocumentSubmitedUnsigned);
                            } else {
                                LOG.error(String.format("oProcessDefinition [id = '%s']  is null",
                                        oHistoricProcessInstance.getProcessDefinitionId()));

                            }
                        } else {
                            LOG.error(String.format("oHistoricProcessInstance [id = '%s']  is null",
                                    snID_Process_Activiti));
                        }

                    }

                } else {
                    LOG.info("oFindedDocumentStepSubjectRight not found");
                }
            }

        }

        return aResDocumentSubmitedUnsigned;
    }

    public void removeDocumentSteps(String snID_Process_Activiti) {
        LOG.info("removeDocumentSteps start with snID_Process_Activiti={}", snID_Process_Activiti);
        List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);

        if (aDocumentStep != null) {
            oDocumentStepDao.delete(aDocumentStep);
            LOG.info("aDocumentStep deleted...");
        }
    }
    
    /**
     * Получение активного степа документаю
     *
     * @param snID_Process_Activiti ид процесса-документа
     * @return активный степ
     */
    public String getActiveStepName(String snID_Process_Activiti) {
        LOG.info("getActiveStepName started with snID_Process_Activiti={}", snID_Process_Activiti);
        List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService.createHistoricVariableInstanceQuery()
                .processInstanceId(snID_Process_Activiti)
                .list();
        String sKey_Step_Active = null;
        for (HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance) {
            if (oHistoricVariableInstance.getVariableName().startsWith("sKey_Step")) {
                sKey_Step_Active = String.valueOf(oHistoricVariableInstance.getValue());
                LOG.info("sKey_Step_Active {}", oHistoricVariableInstance.getValue());
            }
        }
        LOG.info("getActiveStepName finished with sKey_Step_Active={}", sKey_Step_Active);
        return sKey_Step_Active;
    }

    /**
     * Валидация права подписи. Проверяем права, которые есть на степе,
     * позволяют ли они выполнить подпись.
     *
     * @param snID_Process_Activiti - ид процесса
     * @param sKey_Step - название степа
     * @param sID_Group_Activiti - персонализированная группа
     */
    public void validateSubmitRights(String snID_Process_Activiti, String sKey_Step, String sID_Group_Activiti) {
        LOG.info("Validation of the submit rights start with snID_Process={}, sID_Group={}, sKey_Step={}",
                snID_Process_Activiti, sID_Group_Activiti, sKey_Step);
        DocumentStep oStep = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
        List<DocumentStepSubjectRight> aoStepRight = oStep.aDocumentStepSubjectRight();
        List<DocumentStepSubjectRight> aoStepRight_forSubmit = aoStepRight.stream()
                .filter(oStepRight -> oStepRight.getbWrite().equals(true)
                        && oStepRight.getsDate() == null
                        && oStepRight.getsKey_GroupPostfix().equals(sID_Group_Activiti))
                .collect(Collectors.toList());
        if (aoStepRight_forSubmit.isEmpty()) {
            throw new DocumentAccessException(DocumentAccessException.DOCUMENT_MODIFIED);
        }
        LOG.info("Submit rights validation succsess");
    }

    /**
     * Валидация прав на степе. Проверяем наличие права на степе так же смотрим права с дефолтного степа.
     *
     * @param snID_Process_Activiti - ид процесса
     * @param sKey_Step - название степа
     * @param sID_Group_Activiti - персонализированная группа
     */
    public void validateStepRights(String snID_Process_Activiti, String sKey_Step, String sID_Group_Activiti) {
        LOG.info("Validation of the step rights start with snID_Process={}, sID_Group={}, sKey_Step={}",
                snID_Process_Activiti, sID_Group_Activiti, sKey_Step);
        DocumentStep oStep_Active = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step);
        DocumentStep oStep_Default = oDocumentStepDaoNew.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, "_");
        List<DocumentStepSubjectRight> aoRightToCheck = new ArrayList<>();
        aoRightToCheck.addAll(oStep_Active.aDocumentStepSubjectRight());
        aoRightToCheck.addAll(oStep_Default.aDocumentStepSubjectRight());
        List<DocumentStepSubjectRight> aoStepRight = aoRightToCheck.stream()
                .filter(oStepRight -> oStepRight.getsKey_GroupPostfix().equals(sID_Group_Activiti))
                .collect(Collectors.toList());
        if (aoStepRight.isEmpty()) {
            throw new DocumentAccessException(DocumentAccessException.ACCESS_DENIED);
        }
        LOG.info("Step rights validation success");
    }

    /**
     * Проверка на содержание логина на других степах с заданным правом подписи
     *
     * @param snID_Process_Activiti - ид процесса
     * @param sLogin - группа для проверки
     * @param bWrite - право подписи (may be null)
     * @return присутствует или нет
     */
    public Boolean isLoginConteinsOnDocumentSteps(String snID_Process_Activiti, String sLogin, Boolean bWrite) {
        LOG.info("isLoginConteinsOnDocumentSteps is snID_Process_Activiti: {}, sLogin: {}", snID_Process_Activiti, sLogin);
        List<DocumentStep> aDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);

        for (DocumentStep oDocumentStep : aDocumentStep) {
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();

            for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                LOG.info("oDocumentStepSubjectRight is {}", oDocumentStepSubjectRight);
                if (oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sLogin)) {
                    if (bWrite == null && oDocumentStepSubjectRight.getbWrite() == null) {
                        return true;
                    } else if (oDocumentStepSubjectRight.getbWrite() != null && oDocumentStepSubjectRight.getbWrite().equals(bWrite)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    /**
     * Сервис по статусам
     * Если bSetTaskName == true, ищем последнюю закрытую ют, парсим по двоеточию статус и меняем его на sEventName.
     * Если bSetHistoryEvent == true, то вносим событие в историю.
     * 
     * @param snID_Process_Activiti
     * @param sEventName
     * @param bSetHistoryEvent
     * @param bSetTaskName
     * @throws CRCInvalidException
     * @throws RecordNotFoundException 
     */
    public void setDocumentEvent(String snID_Process_Activiti, String sEventName, boolean bSetHistoryEvent, boolean bSetTaskName) throws CRCInvalidException, RecordNotFoundException {
        String sID_Order = generalConfig.getOrderId_ByProcess(Long.valueOf(snID_Process_Activiti));
        Long nID_Task = oActionTaskService.getTaskIDbyProcess(Long.valueOf(snID_Process_Activiti), sID_Order, Boolean.FALSE);
        Task oTask = oTaskService.createTaskQuery().taskId(String.valueOf(nID_Task)).singleResult();
        if (bSetTaskName) {
            String sTaskName = oTask.getName();
            LOG.info("sTaskName: {}", sTaskName);
            String sNewName = " ";
            if (sTaskName.contains(" :: ")) {
                Pattern patternDate = Pattern.compile("(.+?) :: (.+)");
                Matcher matcherDate = patternDate.matcher(sTaskName);
                while (matcherDate.find()) {
                    sNewName = matcherDate.group(2);
                }
            } else {
                sNewName = sTaskName;
            }            
            String sDelimeter = " :: ";
            String sUserName = sEventName + sDelimeter + sNewName;
            LOG.info("sUserName: {}", sUserName);
            oTask.setName(sUserName);
            oTaskService.saveTask(oTask);            
        }
        if (bSetHistoryEvent) {
            Map<String, String> mParam = new HashMap<>();
            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
            mParam.put("sID_Process", snID_Process_Activiti);
            mParam.put("sID_Order", sID_Order);
            mParam.put("sName", sEventName.toLowerCase());
            boolean bProcessClosed = oTask == null;
            String sUserTaskName = bProcessClosed ? "закрита" : oTask.getName();
            mParam.put("sUserTaskName", sUserTaskName);
            mParam.put("nID_HistoryEventType", "16");
            try {
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
            } catch (Exception ex) {
                LOG.info("Error in setDocumentEvent during bSetHistoryEvent: {}", ex);
            }
        }
    }
}