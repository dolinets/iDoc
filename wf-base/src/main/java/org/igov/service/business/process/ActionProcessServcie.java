package org.igov.service.business.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.TaskInfo;
import org.igov.model.action.vo.TaskFilterVO;

import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.controller.ExceptionCommonController;
import org.igov.service.exception.CommonServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author idenysenko
 */
@Service
public class ActionProcessServcie {
    
    @Autowired
    private ActionTaskService oActionTaskService;
    @Autowired
    private RepositoryService oRepositoryService;
    
    private static final Logger LOG = LoggerFactory.getLogger(ActionProcessServcie.class);
    //типы полей, которые используем как критерий - можем выполнить поиск по полю или нет
    private static final List<String> FIELDS_TYPE_DISABLED_FOR_SEARCH = Arrays.asList("markers", "file", "label",
            "queueData", "invisible", "table", "fileHTML", "textArea", "enum");
    
    /**
     * Получение названий процессов в которых учавствовал sLogin. На вход так же принимает массив тип фильтра.
     * Возможные значения для saFilterStatus: DocumentOpenedUnassignedUnprocessed, DocumentOpenedUnassignedProcessed,
     * DocumentOpenedUnassignedWithoutECP, DocumentClosed, OpenedCreatorDocument, OpenedUnassigned, OpenedAssigned,
     * Task, Document
     * 
     * @param sLogin логин, для которого ищем БП
     * @param saFilterStatus тип вкладки, если null - для доков и заданий
     * @return мапу Name_ID
     */
    public List<Map<String, String>> getBPsForParticipant(String sLogin, String saFilterStatus) {
        LOG.info("getBPs_For_Participant start with sLogin={}, saFilterStatus={}", sLogin, saFilterStatus);
        //если параметр не задан, выполняется поиск по всем таска (и доки и задания)
        if (saFilterStatus == null || "".equals(saFilterStatus.trim())) {
            saFilterStatus = "Opened";
        }
        List<Map<String, String>> amBP_Result = new ArrayList<>();

        List<String> asFilterStatus = Arrays.asList(saFilterStatus.split(","));
        LOG.info("asFilterStatus={}", asFilterStatus);
        //лист всех тасок из которых будем выбирать ProcessDefinitionKey
        List<TaskInfo> aoAllTasks = new ArrayList<>();
        asFilterStatus.forEach(sStatus
                -> aoAllTasks.addAll(oActionTaskService.searchTasks(new TaskFilterVO(sLogin, sStatus, false, false)))
        );
        LOG.info("Task count for definition searching {}", aoAllTasks.size());
        //дефинишины для которых нужно найти неймы
        Set<String> asProcessDefinitionKey = aoAllTasks.stream()
                .map(oTask -> 
                    oTask.getProcessDefinitionId() == null ? null : oTask.getProcessDefinitionId().split(":")[0])
                .collect(Collectors.toSet());
        LOG.info("asProcessDefinitionKey={}", asProcessDefinitionKey);
        //для каждого ProcessDefinitionKey находим нейм и кладем в мапу
        String sProcessDefinitionName;
        for (String sProcessDefinitionKey : asProcessDefinitionKey) {
            List<ProcessDefinition> aoProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(sProcessDefinitionKey)
                    .latestVersion()
                    .list();
            if (!aoProcessDefinition.isEmpty()) {
                Map<String, String> mBpID_Name = new HashMap<>();
                sProcessDefinitionName = aoProcessDefinition.get(0).getName();
                mBpID_Name.put("sID", sProcessDefinitionKey);
                mBpID_Name.put("sName", sProcessDefinitionName);
                amBP_Result.add(mBpID_Name);
            }
        }

        return amBP_Result;
    }
    
    /**
     * Найти поля для заданного ProcessDefinitionKey. 
     * 
     * @param sProcessDefinitionKey ид названия процесса без версионности
     * @param bAllTypeFields флаг все поля/доступные для поиска
     * @return мапу ID_Name с найдеными полями
     * @throws CommonServiceException не найдена запись в "public"."act_re_procdef" с таким sProcessDefinitionKey
     */
    public List<Map<String, String>> getBPFields(String sProcessDefinitionKey, Boolean bAllTypeFields) 
            throws CommonServiceException {
        LOG.info("getBPFields start for {} with bAllTypeFields={}", sProcessDefinitionKey, bAllTypeFields);
        if (bAllTypeFields == null) {
            bAllTypeFields = false;
        }
        List<ProcessDefinition> aoProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(sProcessDefinitionKey)
                .latestVersion()
                .list();
        List<Map<String, String>> mFields_Result = new ArrayList<>();

        if (!aoProcessDefinition.isEmpty()) {
            //получили последнюю версию процесса
            String sProcessDefinitionId = aoProcessDefinition.get(0).getId();
            LOG.info("Current sProcessDefinitionId={}", sProcessDefinitionId);
            //элементы БП
            Collection<FlowElement> oElements = oRepositoryService.getBpmnModel(sProcessDefinitionId)
                    .getMainProcess().getFlowElements();
            Set<String> asAllreadyAddedField = new HashSet<>();
            for (FlowElement flowElement : oElements) {
                //поля содержатся только у юзер тасок
                if (flowElement instanceof UserTask) {
                    UserTask userTask = (UserTask) flowElement;
                    for (org.activiti.bpmn.model.FormProperty oProperty : userTask.getFormProperties()) {
                        Map<String, String> mField = new HashMap<>();
                        mField.put("sID", oProperty.getId());
                        mField.put("sName", oProperty.getName().split(";")[0]);
                        mField.put("sType", oProperty.getType());
                        if (bAllTypeFields && !asAllreadyAddedField.contains(oProperty.getId())) {
                            asAllreadyAddedField.add(oProperty.getId());
                            mFields_Result.add(mField);
                        } else {
                            //отдаем поля, по которым клиент может сделать поиск
                            if (isFieldAbleToSearch(oProperty) && !asAllreadyAddedField.contains(oProperty.getId())) {
                                asAllreadyAddedField.add(oProperty.getId());
                                mFields_Result.add(mField);
                            }
                        }                      
                    }
                }
            }
        } else {
            //не найдена запись в "public"."act_re_procdef" с таким sProcessDefinitionKey
            throw new CommonServiceException(ExceptionCommonController.SYSTEM_ERROR_CODE,
                    "Can't find ProcessDefinition object.");
        }

        return mFields_Result;
    }
    
    private boolean isFieldAbleToSearch(org.activiti.bpmn.model.FormProperty oProperty) {
        boolean result = false;
        String sName = oProperty.getName();
        String sType = oProperty.getType();
        if (!sName.contains("bVisible=false") && !FIELDS_TYPE_DISABLED_FOR_SEARCH.contains(sType)) {
            result = true;
        }
        return result;
    }
}
