package org.igov.service.business.action.task.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.task.IdentityLink;

import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectDao;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.business.subject.SubjectHumanService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author idenysenko
 */
@Component("SyncProcessSubject")
public class SyncProcessSubject implements TaskListener {

    @Autowired
    TaskService oTaskService;
    @Autowired
    private ProcessSubjectDao oProcessSubjectDao;
    @Autowired
    private SubjectHumanService oSubjectHumanService;
    @Autowired
    private GeneralConfig generalConfig;
    @Autowired
    HttpRequester oHttpRequester;
    @Autowired
    private RepositoryService oRepositoryService;
    @Autowired
    ProcessLinkService oProcessLinkService;

    private final static Logger LOG = LoggerFactory.getLogger(SyncProcessSubject.class);

    @Override
    public void notify(DelegateTask oDelegateTask) {
        LOG.info("SyncProcessSubject was started...");
        LOG.info("Task id {}", oDelegateTask.getId());
        LOG.info("snID_Process_Activiti id {}", oDelegateTask.getProcessInstanceId());
        String snID_Process_Activiti = oDelegateTask.getProcessInstanceId();
        String snID_Task_Activiti = oDelegateTask.getId();
        Set<IdentityLink> aoCandidates = oDelegateTask.getCandidates();
        LOG.info("SyncProcessSubject: выбраные кандидаты aoCandidates={}", aoCandidates);
        LOG.info("snID_ProcessSubjectTask_Activiti in SyncProcessSubject is {}", oDelegateTask.getVariable("snID_ProcessSubjectTask"));
        //Login = GroupId
        List<String> asLogin = new ArrayList<>();

        for (IdentityLink oCandidateLink : aoCandidates) {
            String sCandidateGroupId = oCandidateLink.getUserId();
            if (sCandidateGroupId != null) {
                asLogin.add(sCandidateGroupId);
            }
        }

        if (asLogin.isEmpty() || asLogin.size() > 1) {

            throw new RuntimeException("Task has several candidates or no one.");
        } else {

            String sLogin = asLogin.get(0);
            Long nID_ProcessSubjectTask = (Long) oDelegateTask.getVariable("snID_ProcessSubjectTask");
            LOG.info("sLogin: {}", sLogin);
            if (nID_ProcessSubjectTask == null) {
                throw new RuntimeException("snID_ProcessSubjectTask is null");
            } else {

                List<ProcessSubject> aProcessSubject = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", nID_ProcessSubjectTask);

                ProcessSubject oProcessSubject = null;
                //this is delegating
                for (ProcessSubject oaProcessSubject : aProcessSubject) {
                    LOG.info("oaProcessSubject login {}", oaProcessSubject.getsLogin());
                    if (oaProcessSubject.getsLogin().equals(sLogin) && oaProcessSubject.getSnID_Task_Activiti() == null) {

                        if (oProcessSubject == null) {
                            LOG.info("oaProcessSubject id is {}", oaProcessSubject.getId());
                            oProcessSubject = oaProcessSubject;
                            //throw new RuntimeException("Error in SyncProcessSubject. Several candidates to update");
                        }
                    }
                }

                //this is editing
                if (oProcessSubject == null) {
                    LOG.info("oProcessSubject is null");
                    List<ProcessSubject> aProcessSubject_EqualsLogins_Candidate = new ArrayList<>();

                    for (ProcessSubject oaProcessSubject : aProcessSubject) {
                        if (oaProcessSubject.getsLogin().equals(sLogin)
                                && oaProcessSubject.getSnID_Process_Activiti().equals(snID_Process_Activiti)) {
                            aProcessSubject_EqualsLogins_Candidate.add(oaProcessSubject);
                        }
                    }

                    LOG.info("aProcessSubject_EqualsLogins_Candidate {}", aProcessSubject_EqualsLogins_Candidate);

                    if (aProcessSubject_EqualsLogins_Candidate.size() > 1) {

                        Comparator<ProcessSubject> cmp = new Comparator<ProcessSubject>() {
                            @Override
                            public int compare(ProcessSubject ProcessSubject1, ProcessSubject ProcessSubject2) {
                                return Long.valueOf(ProcessSubject1.getSnID_Task_Activiti())
                                        .compareTo(Long.valueOf(ProcessSubject2.getSnID_Task_Activiti()));
                            }
                        };

                        oProcessSubject = Collections.min(aProcessSubject_EqualsLogins_Candidate, cmp);
                        LOG.info("oProcessSubject.getSnID_Task_Activiti min {}", oProcessSubject.getSnID_Task_Activiti());
                        LOG.info("oProcessSubject.getId {}", oProcessSubject.getId());
                    } else {
                        if (!aProcessSubject_EqualsLogins_Candidate.isEmpty()) {
                            oProcessSubject = aProcessSubject_EqualsLogins_Candidate.get(0);
                        }
                    }
                }

                if (oProcessSubject == null) {

                }

                if (oProcessSubject == null) {
                    LOG.info("Couldn't find ProcessSubject with assained login");
                } else {
                    LOG.info("oProcessSubject id {}", oProcessSubject.getId());
                    oProcessSubject.setSnID_Task_Activiti(snID_Task_Activiti);
                    try {
                        oProcessLinkService.syncProcessLinksShort(snID_Process_Activiti, aProcessSubject, oDelegateTask);
                    } catch (Exception oException) {
                        LOG.error("syncProcessLinksShort error - {}", oException);
                        throw new RuntimeException(oException.getMessage());
                    }

                    oProcessSubjectDao.saveOrUpdate(oProcessSubject);
                }
            }
        }
    }
}
