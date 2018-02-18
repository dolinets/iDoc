package org.igov.service.business.process.processLink;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.igov.model.document.*;
import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectDao;
import org.igov.model.process.processLink.*;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.SubjectHumanDao;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.document.DocumentStepSubjectRightService;
import org.igov.service.business.launch.LaunchService;
import org.igov.service.business.subject.SubjectHumanService;
import org.igov.service.business.util.Date;
import org.igov.service.controller.ExceptionCommonController;
import org.igov.service.exception.CommonServiceException;
import org.igov.util.JSON.JsonRestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author idenysenko
 */
@Component("processLinkService")
public class ProcessLinkService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLinkService.class);

    @Autowired private HistoryService oHistoryService;
    @Autowired private TaskService oTaskService;
    @Autowired private ActionTaskService oActionTaskService;
    @Autowired private ProcessLinkDao oProcessLinkDao;
    @Autowired private ProcessLinkTypeDao oProcessLinkTypeDao;
    @Autowired private ProcessLinkSubTypeDao oProcessLinkSubTypeDao;
    @Autowired private DocumentStepService oDocumentStepService;
    @Autowired private ServerDao oServerDao;
    @Autowired private GeneralConfig generalConfig;
    @Autowired private DocumentStepTypeDao oDocumentStepTypeDao;
    @Autowired private SubjectHumanDao oSubjectHumanDao;
    @Autowired private ProcessSubjectDao oProcessSubjectDao;
    @Autowired private HttpRequester oHttpRequester;
    @Autowired private RepositoryService oRepositoryService;
    @Autowired private SubjectHumanService oSubjectHumanService;
    @Autowired private DocumentStepDao oDocumentStepDao;
    @Autowired private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;
    @Autowired private LaunchService oLaunchService;
    @Autowired private DocumentStepSubjectRightService oDocumentStepSubjectRightService;

    /**
     * Получить все ProcessLink по параметрам.
     *
     * @param sID_Group_Activiti персонализированная группа (логин)
     * @param sType тип закладки
     * @param sSubType тип под-закладки
     * @return лист ProcessLink из бд
     */
    public List<ProcessLink> getProcessLinks(String sID_Group_Activiti, String sType, String sSubType) {

        return oProcessLinkDao.getProcessLinks(sID_Group_Activiti, sType, sSubType);
    }

    /**
     * Получить все ProcessLink по персонализированной группе и ид процесса.
     *
     * @param sID_Group_Activiti персонализированная группа (логин)
     * @param snID_Process_Activiti ид процесса
     * @return лист ProcessLink из бд
     */
    public List<ProcessLink>  getProcessLinksByProcessAndGroup(String snID_Process_Activiti, String sID_Group_Activiti) {

        return oProcessLinkDao.getProcessLinksByProcessAndGroup(snID_Process_Activiti, sID_Group_Activiti);
    }

    /**
     * Создание/редактирование ProcessLink. Если приходит ид пытаемся отредактировать, если нет создаем.
     *
     * @param nID_server ид исходного сервера
     * @param sType тип закладки
     * @param sSubType тип под-закладки
     * @param sID_Group_Activiti группа авторизованного сотрудника
     * @param sLogin логин авторизованного сотрудника
     * @param sTaskName название таски
     * @param sProcessName название процесса
     * @param sProcessDateCreate дата создания процесса
     * @param sProcessDateModify дата редактирования процесса
     * @param sTaskDateCreate дата создания таски
     * @param snID_Process_Activiti ид процесса
     * @param snID_Task ид таски
     * @param nID_DocumentStepType тип степа на котором находится таска
     * @param sTaskDateModify дата редактирования таски
     * @param bUrgent метка срочности
     * @return обьект, который создали или отредактировали
     * @throws CommonServiceException если пришли не все обязательные параметры
     */
    public ProcessLink setProcessLink(Long nID_server, String sType, String sSubType,
            String sID_Group_Activiti, String sLogin, String sTaskName, String sProcessName, String sProcessDateCreate,
            String sProcessDateModify, String sTaskDateCreate, String sTaskDateModify, String snID_Process_Activiti,
            String snID_Task, Long nID_DocumentStepType, Boolean bUrgent) throws CommonServiceException {

        LOG.info("setProcessLink started with: nID_server={}, sType={}, sSubType={},"
                + "sGroup={}, sLogin={}, sTaskName={}, sProcessName={}, sProcessDateCreate={}, sProcessDateModify={},"
                + "sTaskDateCreate={}, sTaskDateModify={}, snID_Process_Activiti={}, snID_Task={}, nID_DocumentStepType={}, bUrgent={}",
                nID_server, sType, sSubType, sID_Group_Activiti, sLogin, sTaskName, sProcessName, sProcessDateCreate,
                sProcessDateModify, sTaskDateCreate, sTaskDateModify, snID_Process_Activiti, snID_Task, nID_DocumentStepType, bUrgent);

        if (nID_server == null || sType == null || sSubType == null || sID_Group_Activiti == null || sTaskName == null
                || sProcessName == null || sProcessDateCreate == null || sTaskDateCreate == null
                || snID_Process_Activiti == null || snID_Task == null) {

            throw new CommonServiceException(ExceptionCommonController.SYSTEM_ERROR_CODE,
                    new RuntimeException("One of the required parameter is null!"));
        }

        Server oServer = oServerDao.findByIdExpected(nID_server);
        LOG.info("oServer={}", oServer);
        ProcessLink_Type oProcessLinkType = oProcessLinkTypeDao.findByExpected("sName", sType);
        LOG.info("oProcessLinkType={}", oProcessLinkType);
        ProcessLink_SubType ProcessLinkSubType = oProcessLinkSubTypeDao.findByExpected("sName", sSubType);
        LOG.info("ProcessLinkSubType={}", ProcessLinkSubType);
        //для задач nID_DocumentStepType = null
        DocumentStepType oDocumentStepType = null;
        if (nID_DocumentStepType != null) {
            oDocumentStepType = oDocumentStepTypeDao.findById(nID_DocumentStepType).get();
            LOG.info("oDocumentStepType={}", oDocumentStepType);
        }

        ProcessLink oProcessLink = oProcessLinkDao.findProcessLink(snID_Process_Activiti, sID_Group_Activiti, oServer.getId());
        if (oProcessLink == null) {
            oProcessLink = new ProcessLink();
        }
        //дата изменения процесса внешней таски
        DateTime dtProcessDateModify_WhichIsAlreadyInDB = oProcessLink.getsProcessDateModify();
        //проверка для апдейта уже существующей сущности ProcessLink, если sProcessDateModify больше даты, которую
        // передали в сервис считаем, что в таком случае апдейтить сущность не нужно,
        // dtProcessDateModify_WhichIsAlreadyInDB == null считаем, что создается новая сущность
        if ((dtProcessDateModify_WhichIsAlreadyInDB != null && dtProcessDateModify_WhichIsAlreadyInDB.isBefore(DateTime.parse(sProcessDateModify)))
                || dtProcessDateModify_WhichIsAlreadyInDB == null) {
            oProcessLink.setoServer(oServer);
            oProcessLink.setoProcessLinkType(oProcessLinkType);
            oProcessLink.setoProcessLinkSubType(ProcessLinkSubType);
            oProcessLink.setsID_Group_Activiti(sID_Group_Activiti);
            oProcessLink.setsLogin(sLogin);
            oProcessLink.setsTaskName(sTaskName);
            oProcessLink.setsProcessName(sProcessName);
            oProcessLink.setsProcessDateCreate(DateTime.parse(sProcessDateCreate));
            oProcessLink.setsProcessDateModify(sProcessDateModify == null ? null : DateTime.parse(sProcessDateModify));
            oProcessLink.setsTaskDateCreate(DateTime.parse(sTaskDateCreate));
            oProcessLink.setsTaskDateModify(sTaskDateModify == null ? null : DateTime.parse(sTaskDateModify));
            oProcessLink.setSnID_Process_Activiti(snID_Process_Activiti);
            oProcessLink.setSnID_Task(snID_Task);
            oProcessLink.setoDocumentStepType(oDocumentStepType);
            oProcessLink.setbUrgent(bUrgent);
        }

        LOG.info("oProcessLink={}", oProcessLink);
        oProcessLinkDao.saveOrUpdate(oProcessLink);

        return oProcessLink;
    }

    /**
     * Синхронизировать ProcessLink. Проверить на участие в процессе внешних SubjectHuman (sRemoteHostAndContext !=
     * null) если таковы присутсвуют, то создать обьект ProcessLink для передоставления через него доступа с другого
     * сервера.
     *
     * @param snID_Process ид процесса
     * @param sLoginPrincipal логин инициатора запуска
     */
    public void syncProcessLinks(String snID_Process, String sLoginPrincipal) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        LOG.info("syncProcessLinks started with snID_Process={}", snID_Process);
        if (snID_Process == null || snID_Process.equals("null")) {
            throw new IllegalArgumentException("Can't sync ProcessLinks - snID_Process is null!");

        } else {
            //Персонализированные группы, для которых нужно проверить у SubjectHuman - sRemoteHostAndContext
            Set<String> asID_Group_Activiti = new HashSet<>();
            DocumentStep oDocumentStep_Active = null;
            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = null;
            //Достаем название активного степпа из переменных процесса, чтобы найти степ (из него тип степа) и
            //райты (из них всех сабджект хьюманов)
            String sKey_Step_Active = oDocumentStepService.getActiveStepName(snID_Process);

            LOG.info("sKey_Step_Active={}", sKey_Step_Active);

            if (sKey_Step_Active != null) {
                oDocumentStep_Active = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process, sKey_Step_Active);
                aDocumentStepSubjectRight = oDocumentStep_Active.aDocumentStepSubjectRight();

                for(DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight){
                    asID_Group_Activiti.add(oDocumentStepSubjectRight.getsKey_GroupPostfix());
                }
                asID_Group_Activiti.addAll(oDocumentStepSubjectRightDao.findDocumentParticipant(snID_Process));
                //Если sKey_Step_Active == null, то считаем, что это не документ, а задача
            } else {
                List<ProcessSubject> aoProcessSubject = oProcessSubjectDao.findAllBy("snID_Process_Activiti", snID_Process);
                LOG.info("aoProcessSubject.size={}", aoProcessSubject.size());
                asID_Group_Activiti = aoProcessSubject.stream()
                        .map(ProcessSubject::getsLogin)
                        .collect(Collectors.toSet());
            }
            long block1 = System.currentTimeMillis();
            LOG.info("Getting logins {}", block1 - start);

            LOG.info("Logins to check asID_Group_Activiti={}", asID_Group_Activiti);

            List<SubjectHuman> aoExternalSubjectHuman = oSubjectHumanDao
                    .getExternalSubjectHumanByIdGroupActiviti(asID_Group_Activiti, generalConfig.getSelfHost());
            LOG.info("aoExternalSubjectHuman.size={}", aoExternalSubjectHuman.size());

            if (!aoExternalSubjectHuman.isEmpty()) {
                SimpleDateFormat oFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                //находим параметры, для setProcessLink
                HistoricProcessInstance oProcess = oHistoryService.createHistoricProcessInstanceQuery()
                        .processInstanceId(snID_Process)
                        .singleResult();
                String sProcessDateCreate = oFormatter.format(oProcess.getStartTime());

                TaskInfo oTask_Current;
                List<Task> aTask = oTaskService.createTaskQuery().processInstanceId(snID_Process).active().list();
                if (!aTask.isEmpty()) {
                    oTask_Current = aTask.get(0);
                } else {
                    List<HistoricTaskInstance> aoHistoryTask = oHistoryService.createHistoricTaskInstanceQuery()
                            .processInstanceId(snID_Process)
                            .finished()
                            .list();
                    oTask_Current = oActionTaskService.filterHistoryTasksByEndTime(aoHistoryTask).get(0);
                }

                String sTaskDateCreate = oFormatter.format(oTask_Current.getCreateTime());

                String sType = oTask_Current.getProcessDefinitionId().startsWith("_doc_") ? "Document" : "Task";
                LOG.info("sType={}", sType);

                String sProcessName = oRepositoryService
                            .getProcessDefinition(oTask_Current.getProcessDefinitionId())
                            .getName();
                LOG.info("sProcessName={}", sProcessName);

                Long nID_Server_Self = Long.valueOf(generalConfig.getSelfServerId());
                LOG.info("nID_Server_Self={}", nID_Server_Self);

                for (SubjectHuman oSubjectHuman : aoExternalSubjectHuman) {
                    String sID_Group_Activiti = oSubjectHumanService.getSubjectGroup(oSubjectHuman).getsID_Group_Activiti();
                    Server oServer = oSubjectHuman.getoServer();
                    LOG.info("sID_Group_Activiti={}, oServer={}", sID_Group_Activiti, oServer);

                    String sSubType = String.valueOf(oActionTaskService
                            .findTaskTab(oTask_Current.getId(), sID_Group_Activiti, null)
                            .get("sDocumentStatus"));
                    LOG.info("sSubType={}", sSubType);

                    String sURI = oServer.getsURL() + "/wf/service/process/processlink/setProcessLink";
                    LOG.info("sURI={}", sURI);

                    ProcessLinkVO oProcessLinkVO = new ProcessLinkVO();
                    oProcessLinkVO.setnID_Server(nID_Server_Self); //sSourceServerHostContext - по которому ищем сервер
                    oProcessLinkVO.setsType(sType); //sType - документ или задание
                    oProcessLinkVO.setsSubType(sSubType); //sSubType - вкладка
                    oProcessLinkVO.setsID_Group_Activiti(sID_Group_Activiti); // sID_Group_Activiti - персонализированная группа
                    oProcessLinkVO.setsLogin(sID_Group_Activiti); //sLogin - еще не используется
                    oProcessLinkVO.setsTaskName(oTask_Current.getName()); //sTaskName  - имя таски
                    oProcessLinkVO.setsProcessName(sProcessName); //sProcessName - имя процесса
                    oProcessLinkVO.setsProcessDateCreate(sProcessDateCreate); //sProcessDateCreate - дата старта процесса
                    oProcessLinkVO.setsProcessDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sProcessDateModify - дата модификации процесса
                    oProcessLinkVO.setsTaskDateCreate(sTaskDateCreate); //sTaskDateCreate - дата создания таски
                    oProcessLinkVO.setsTaskDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sTaskDateModify - дата модификации таски
                    oProcessLinkVO.setSnID_Process_Activiti(snID_Process); //snID_Process_Activiti - ид процесса
                    oProcessLinkVO.setSnID_Task(oTask_Current.getId()); //snID_Task - ид таски
                    oProcessLinkVO.setnID_DocumentStepType(oDocumentStep_Active == null
                            ? null : oDocumentStep_Active.getoDocumentStepType().getId()); //nID_DocumentStepType - тип степа документа

                    if(aDocumentStepSubjectRight != null){
                        Optional<DocumentStepSubjectRight> oDocumentStepSubjectRight_matcher = aDocumentStepSubjectRight.stream()
                            .filter(oDocumentStepSubjectRight -> oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sID_Group_Activiti))
                            .findFirst();

                       oDocumentStepSubjectRight_matcher.ifPresent(oDocumentStepSubjectRight ->
                                oProcessLinkVO.setbUrgent(oDocumentStepSubjectRight.getbUrgent()));
                    }
                    LOG.info("oProcessLinkVO={}", oProcessLinkVO);
                    //создание ProcessLink на внешнем сервере через LaunchService для дальнешего протоколирования вызова
                    Class[] aClass = {String.class, Map.class, String.class, String.class};
                    Object[] aoObject = {sURI, new HashMap(), JsonRestUtils.toJson(oProcessLinkVO), "application/json;charset=utf-8"};
                    oLaunchService.start(sLoginPrincipal, "postInside", aClass, aoObject, HttpRequester.class, oServer);
                }
            }
        }
        LOG.info("syncProcessLinks time {}", System.currentTimeMillis() - start);
    }

    /**
     * Синхронизировать ProcessLink по логину, выполняется проверка является ли этот сотрудник внешним.
     *
     * @param snID_Process_Activiti ид процесса
     * @param sID_Group_Activiti - персонализированная группа логин
     * @param sLoginPrincipal логин инициатора запуска
     */
    public void syncProcessLinksByLogin(String snID_Process_Activiti, String sID_Group_Activiti, String sLoginPrincipal) throws JsonProcessingException {
        LOG.info("syncProcessLinksByLogin start... {}, {}, {}", snID_Process_Activiti, sID_Group_Activiti, sLoginPrincipal);
        Set<String> asLogin = new HashSet<>();
        asLogin.add(sID_Group_Activiti);
        List<SubjectHuman> aoSubjectHuman = oSubjectHumanService.getExternalSubjectHumanByIdGroupActiviti(asLogin);
        if (!aoSubjectHuman.isEmpty()) {
            LOG.info("Founded external sID_Group_Activiti!");
            //Ищем права на документ
            List<DocumentStepSubjectRight> aoRight = oDocumentStepSubjectRightService.getRightsByProcessAndGroup(snID_Process_Activiti, sID_Group_Activiti);
            //у группы больше не осталось прав, если это внешний сотрудник нужно у него удалить доступ к документу
            if (aoRight.isEmpty()) {
                LOG.info("Need to delete external participant.");
                removeExternalProcessLinks(snID_Process_Activiti, sID_Group_Activiti, sLoginPrincipal);
            } else {
                LOG.info("Just start syncProcessLink");
                //в остальных случаях синхронизируем, возможно внешний сотрудник был подписантом а остался на просмотре
                DocumentStep oDocumentStep_Active = null;
                //Достаем название активного степпа из переменных процесса, чтобы найти степ (из него тип степа) и
                //райты (из них всех сабджект хьюманов)
                String sKey_Step_Active = oDocumentStepService.getActiveStepName(snID_Process_Activiti);
                LOG.info("sKey_Step_Active={}", sKey_Step_Active);

                if (sKey_Step_Active != null) {
                    oDocumentStep_Active = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process_Activiti, sKey_Step_Active);
                }
                SimpleDateFormat oFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                //находим параметры, для setProcessLink
                HistoricProcessInstance oProcess = oHistoryService.createHistoricProcessInstanceQuery()
                        .processInstanceId(snID_Process_Activiti)
                        .singleResult();
                String sProcessDateCreate = oFormatter.format(oProcess.getStartTime());

                TaskInfo oTask_Current;
                List<Task> aTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
                if (!aTask.isEmpty()) {
                    oTask_Current = aTask.get(0);
                } else {
                    List<HistoricTaskInstance> aoHistoryTask = oHistoryService.createHistoricTaskInstanceQuery()
                            .processInstanceId(snID_Process_Activiti)
                            .finished()
                            .list();
                    oTask_Current = oActionTaskService.filterHistoryTasksByEndTime(aoHistoryTask).get(0);
                }
                String sTaskDateCreate = oFormatter.format(oTask_Current.getCreateTime());

                String sType = oTask_Current.getProcessDefinitionId().startsWith("_doc_") ? "Document" : "Task";
                LOG.info("sType={}", sType);

                String sProcessName = oRepositoryService
                        .getProcessDefinition(oTask_Current.getProcessDefinitionId())
                        .getName();
                LOG.info("sProcessName={}", sProcessName);

                Long nID_Server_Self = Long.valueOf(generalConfig.getSelfServerId());
                LOG.info("nID_Server_Self={}", nID_Server_Self);

                SubjectHuman oSubjectHuman = oSubjectHumanService.getSubjectHuman(sID_Group_Activiti);
                Server oServer = oSubjectHuman.getoServer();
                LOG.info("sID_Group_Activiti={}, oServer={}", sID_Group_Activiti, oServer);

                String sSubType = String.valueOf(oActionTaskService
                        .findTaskTab(oTask_Current.getId(), sID_Group_Activiti, null)
                        .get("sDocumentStatus"));
                LOG.info("sSubType={}", sSubType);

                String sURI = oServer.getsURL() + "/wf/service/process/processlink/setProcessLink";
                LOG.info("sURI={}", sURI);

                ProcessLinkVO oProcessLinkVO = new ProcessLinkVO();
                oProcessLinkVO.setnID_Server(nID_Server_Self); //sSourceServerHostContext - по которому ищем сервер
                oProcessLinkVO.setsType(sType); //sType - документ или задание
                oProcessLinkVO.setsSubType(sSubType); //sSubType - вкладка
                oProcessLinkVO.setsID_Group_Activiti(sID_Group_Activiti); // sID_Group_Activiti - персонализированная группа
                oProcessLinkVO.setsLogin(sID_Group_Activiti); //sLogin - еще не используется
                oProcessLinkVO.setsTaskName(oTask_Current.getName()); //sTaskName  - имя таски
                oProcessLinkVO.setsProcessName(sProcessName); //sProcessName - имя процесса
                oProcessLinkVO.setsProcessDateCreate(sProcessDateCreate); //sProcessDateCreate - дата старта процесса
                oProcessLinkVO.setsProcessDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sProcessDateModify - дата модификации процесса
                oProcessLinkVO.setsTaskDateCreate(sTaskDateCreate); //sTaskDateCreate - дата создания таски
                oProcessLinkVO.setsTaskDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sTaskDateModify - дата модификации таски
                oProcessLinkVO.setSnID_Process_Activiti(snID_Process_Activiti); //snID_Process_Activiti - ид процесса
                oProcessLinkVO.setSnID_Task(oTask_Current.getId()); //snID_Task - ид таски
                oProcessLinkVO.setnID_DocumentStepType(oDocumentStep_Active == null
                        ? null : oDocumentStep_Active.getoDocumentStepType().getId()); //nID_DocumentStepType - тип степа документа
                LOG.info("oProcessLinkVO={}", oProcessLinkVO);
                //создание ProcessLink на внешнем сервере через LaunchService для дальнешего протоколирования вызова
                Class[] aClass = {String.class, Map.class, String.class, String.class};
                Object[] aoObject = {sURI, new HashMap(), JsonRestUtils.toJson(oProcessLinkVO), "application/json;charset=utf-8"};
                oLaunchService.start(sLoginPrincipal, "postInside", aClass, aoObject, HttpRequester.class, oServer);
                LOG.info("syncProcessLinksByLogin completed.");
            }
        }
        LOG.info("syncProcessLinksByLogin {} not external participant.", sID_Group_Activiti);
    }

    /**
     * Синхронизировать ProcessLink, используется только в SyncProcessSubject. Проверить на участие в процессе внешних
     * SubjectHuman (sRemoteHostAndContext != null) если таковы присутсвуют, то создать обьект ProcessLink для
     * передоставления через него доступа с другого сервера.
     *
     * @param snID_Process_Activiti ид процесса
     * @param aProcessSubject массив участников задания
     * @param oDelegateTask обьект активити
     * @throws JsonProcessingException ошибка сериализации
     */
    public void syncProcessLinksShort(String snID_Process_Activiti, List<ProcessSubject> aProcessSubject,
            DelegateTask oDelegateTask) throws JsonProcessingException {

        LOG.info("syncProcessLinksShort started...");
        Set<String> asID_Group_Activiti = new HashSet<>();
        LOG.info("aoProcessSubject.size={}", aProcessSubject.size());
        Map<String, String> mProcessSubjectLogin_LoginRole = new HashMap<>();
        aProcessSubject.forEach((oProcessSubject) -> {
            String sLogin = oProcessSubject.getsLogin();
            asID_Group_Activiti.add(sLogin);
            mProcessSubjectLogin_LoginRole.put(sLogin, oProcessSubject.getsLoginRole());
        });
        LOG.info("Logins to check asID_Group_Activiti={}", asID_Group_Activiti);

        List<SubjectHuman> aoExternalSubjectHuman = oSubjectHumanDao
                .getExternalSubjectHumanByIdGroupActiviti(asID_Group_Activiti, generalConfig.getSelfHost());
        LOG.info("aoExternalSubjectHuman.size={}", aoExternalSubjectHuman.size());

        if (!aoExternalSubjectHuman.isEmpty()) {

            String sType = oDelegateTask.getProcessDefinitionId().startsWith("_doc_") ? "Document" : "Task";
            LOG.info("sType={}", sType);

            String sProcessName = oRepositoryService
                    .getProcessDefinition(oDelegateTask.getProcessDefinitionId())
                    .getName();
            LOG.info("sProcessName={}", sProcessName);

            Long nID_Server_Self = Long.valueOf(generalConfig.getSelfServerId());
            LOG.info("nID_Server_Self={}", nID_Server_Self);

            for (SubjectHuman oSubjectHuman : aoExternalSubjectHuman) {
                String sID_Group_Activiti = oSubjectHumanService.getSubjectGroup(oSubjectHuman).getsID_Group_Activiti();
                Server oServer = oSubjectHuman.getoServer();
                LOG.info("sID_Group_Activiti={}, oServer={}", sID_Group_Activiti, oServer);
                String sSubType = mProcessSubjectLogin_LoginRole.get(sID_Group_Activiti).equals("Controller")
                        ? "Control"
                        : "Execution";
                LOG.info("sSubType={}", sSubType);
                String sLoginPrincipal = oDelegateTask.getVariable("sLoginAuthor", String.class);
                LOG.info("sLoginPrincipal={}", sLoginPrincipal);
                String sURI = oServer.getsURL() + "/wf/service/process/processlink/setProcessLink";
                LOG.info("sURI={}", sURI);

                ProcessLinkVO oProcessLinkVO = new ProcessLinkVO();
                oProcessLinkVO.setnID_Server(nID_Server_Self); //sSourceServerHostContext - по которому ищем сервер
                oProcessLinkVO.setsType(sType); //sType - документ или задание
                oProcessLinkVO.setsSubType(sSubType); //sSubType - вкладка
                oProcessLinkVO.setsID_Group_Activiti(sID_Group_Activiti); // sID_Group_Activiti - персонализированная группа
                oProcessLinkVO.setsLogin(sID_Group_Activiti); //sLogin - еще не используется
                oProcessLinkVO.setsTaskName(oDelegateTask.getName()); //sTaskName  - имя таски
                oProcessLinkVO.setsProcessName(sProcessName); //sProcessName - имя процесса
                oProcessLinkVO.setsProcessDateCreate(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sProcessDateCreate - дата старта процесса
                oProcessLinkVO.setsProcessDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sProcessDateModify - дата модификации процесса
                oProcessLinkVO.setsTaskDateCreate(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sTaskDateCreate - дата создания таски
                oProcessLinkVO.setsTaskDateModify(Date.getToday("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //sTaskDateModify - дата модификации таски
                oProcessLinkVO.setSnID_Process_Activiti(snID_Process_Activiti); //snID_Process_Activiti - ид процесса
                oProcessLinkVO.setSnID_Task(oDelegateTask.getId()); //snID_Task - ид таски
                oProcessLinkVO.setnID_DocumentStepType(null); //nID_DocumentStepType - тип степа документа
                LOG.info("oProcessLinkVO={}", oProcessLinkVO);
                //создание ProcessLink на внешнем сервере через LaunchService для дальнешего протоколирования вызова
                Class[] aClass = {String.class, Map.class, String.class, String.class};
                Object[] aoObject = {sURI, new HashMap(), JsonRestUtils.toJson(oProcessLinkVO), "application/json;charset=utf-8"};
                oLaunchService.start(sLoginPrincipal, "postInside", aClass, aoObject, HttpRequester.class, oServer);
            }
        }
    }

    /**
     * Удалить ProcessLink с внешнего сервера.
     *  @param snID_Process_Activiti ид процесса
     * @param sID_Group_Activiti персонализированная группа (логин)
     * @param sLoginPrincipal кто вызвал сервис
     */
    public void removeExternalProcessLinks(String snID_Process_Activiti, String sID_Group_Activiti, String sLoginPrincipal) {
        LOG.info("removeExternalProcessLinks started for {}, {}", snID_Process_Activiti, sID_Group_Activiti);
        List<SubjectHuman> aoSubjectHuman_External = new ArrayList<>();
        //по логину выбираем только того, которого нужно удалить
        if (sID_Group_Activiti != null) {
            aoSubjectHuman_External.add(oSubjectHumanService.getSubjectHuman(sID_Group_Activiti));
        }
        if (sID_Group_Activiti == null) {
            aoSubjectHuman_External.addAll(oSubjectHumanService.getExternalSubjectHumanFromDocument(snID_Process_Activiti));
        }
        //собираем сервера на которые нужно отправить запрос на удаление ProcessLink с snID_Process_Activiti
        Set<Server> aoServer = aoSubjectHuman_External.stream()
                .map(SubjectHuman::getoServer)
                .collect(Collectors.toSet());
        LOG.info("aoServer count {}", aoServer.size());
        Map<String, String> mParam = new HashMap<>();
        mParam.put("snID_Process_Activiti", snID_Process_Activiti);
        if (sID_Group_Activiti != null) {
            mParam.put("sID_Group_Activiti", sID_Group_Activiti);
        }
        for (Server oServer : aoServer) {
            String sURI = oServer.getsURL() + "/wf/service/process/processlink/removeProcessLinks";
            //установка статуса ProcessLink'а на внешнем сервере через LaunchService для дальнешего протоколирования вызова
            Class[] aClass = {String.class, Map.class};
            Object[] aoObject = {sURI, mParam};
            oLaunchService.start(sLoginPrincipal, "getInside", aClass, aoObject, HttpRequester.class, oServer);
        }
    }

    /**
     * Установаить статус ProcessLink по ид процесса (запрос на внешний сервер).
     *
     * @param snID_Process_Activiti ид процесса
     * @param sStatus статус, кторый будет проставлен в БД
     * @param sLogin логин того тко вызвал сервис
     */
    public void setExternalProcessLinkStatus(String snID_Process_Activiti, String sStatus, String sLogin) {
        LOG.info("removeExternalProcessLinks started for snID_Process_Activiti={}", snID_Process_Activiti);
        List<SubjectHuman> aoSubjectHuman_External = oSubjectHumanService.getExternalSubjectHumanFromDocument(snID_Process_Activiti);
        //собираем сервера на которые нужно отправить запрос
        Set<Server> aoServer = aoSubjectHuman_External.stream()
                .map(SubjectHuman::getoServer)
                .collect(Collectors.toSet());
        LOG.info("aoServer count {}", aoServer.size());
        Map<String, String> mParam = new HashMap<>();
        mParam.put("snID_Process_Activiti", snID_Process_Activiti);
        mParam.put("sStatus", sStatus);
        mParam.put("snID_Process_Activiti", snID_Process_Activiti);
        for (Server oServer : aoServer) {
            String sURI = oServer.getsURL() + "/wf/service/process/processlink/setProcessLinkStatus";
            //установка статуса ProcessLink'а на внешнем сервере через LaunchService для дальнешего протоколирования вызова
            Class[] aClass = {String.class, Map.class};
            Object[] aoObject = {sURI, mParam};
            oLaunchService.start(sLogin, "getInside", aClass, aoObject, HttpRequester.class, oServer);
        }
    }

}
