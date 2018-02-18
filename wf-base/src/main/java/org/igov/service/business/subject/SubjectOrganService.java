/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.subject;

import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import org.igov.service.business.util.SubjectUtils;
import org.igov.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.igov.service.business.subject.SubjectGroupTreeService.ORGAN;

/**
 *
 * @author iDoc-2
 */
@Service
public class SubjectOrganService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectOrganService.class);

    @Autowired private SubjectOrganDao oSubjectOrganDao;
    @Autowired private SubjectDao oSubjectDao;
    @Autowired private SubjectGroupDao oSubjectGroupDao;
    @Autowired private SubjectGroupService oSubjectGroupService;
    @Autowired private SubjectContactDao oSubjectContactDao;
    @Autowired private SubjectHumanPositionCustomService oSubjectHumanPositionCustomService;
    @Autowired private SubjectService oSubjectService;
    @Autowired private ServerEntitySyncService oServerEntitySyncService;
    @Autowired private SubjectStatusDao oSubjectStatusDao;

    public SubjectGroup setSubjectOrgan(String sID_Group_Activiti, String sName, String sID_Group_Activiti_Parent,
                                        String sOKPO, String sFormPrivacy, String sEmail, Boolean bCreate, Boolean isSync,
                                        String sStatusName) {
        if (sID_Group_Activiti == null && sID_Group_Activiti_Parent == null) {
            throw new RuntimeException("No name company");
        }
        if (Boolean.TRUE.equals(bCreate) && sID_Group_Activiti_Parent != null && sID_Group_Activiti != null) {
            throw new RuntimeException("should be auto generated");
        }
        if (Boolean.FALSE.equals(bCreate) && sID_Group_Activiti == null) {
            throw new RuntimeException("No company/depart selected, use 'sID_Group_Activiti' param");
        }

        sID_Group_Activiti = generatesID(sName, sID_Group_Activiti, sID_Group_Activiti_Parent);

        SubjectGroup oSubjectGroup = oSubjectGroupService.getOrCreateSubjectGroup(sID_Group_Activiti);
        Subject oSubject = oSubjectService.getOrCreateSubject(oSubjectGroup);
        SubjectOrgan oSubjectOrgan = getOrCreateSubjectOrgan(oSubject);
        SubjectHumanPositionCustom oPosition = oSubjectHumanPositionCustomService.getGroupDepartment();

        boolean isCreation = oSubjectGroup.getId() == null;

        oSubjectGroup.setName(sName);
        oSubjectGroup.setsChain("");
        oSubjectGroup.setsSubjectGroup_Company("");
        oSubjectGroup.setoSubjectHumanPositionCustom(oPosition);

        oSubject.setsID(sID_Group_Activiti);
        oSubject.setsLabel(sName);
        if (sStatusName != null && !"".equals(sStatusName.trim())) {
            SubjectStatus oSubjectStatus = oSubjectStatusDao.findByExpected("sName", sStatusName);
            oSubject.setoSubjectStatus(oSubjectStatus);
        }
        oSubjectDao.saveOrUpdate(oSubject);
        if (isCreation) {
            SubjectUtils.checkID(oSubject);
        }
        LOG.debug("set Subject, id: {}, sName: {}", oSubject.getId(), sName);

        oSubjectOrgan.setName(sName);
        oSubjectOrgan.setsNameFull(sName);
        oSubjectOrgan.setsOKPO(sOKPO);
        oSubjectOrgan.setsFormPrivacy(sFormPrivacy);
        oSubjectOrganDao.saveOrUpdate(oSubjectOrgan);
        if (isCreation) {
            SubjectUtils.checkID(oSubjectOrgan);
        }
        LOG.debug("set SubjectOrgan, id: {}", oSubjectOrgan.getId());
        LOG.info("setSubjectOrgan oSubjectOrgan id is {}", oSubjectOrgan.getId());
        if (sEmail != null) {
            SubjectContact oSubjectContact = oSubjectService.getOrCreateSubjectEmailContact(oSubject);
            oSubjectContact.setsValue(sEmail);
            oSubjectContact.setsDate();
            oSubjectContactDao.saveOrUpdate(oSubjectContact);
            if (isCreation) {
                SubjectUtils.checkID(oSubjectContact);
            }
            oSubject.getaSubjectAccountContact().add(oSubjectContact);
            LOG.debug("set SubjectContact (email), id: {}, value: {}", oSubjectContact.getId(), sEmail);
            LOG.info("setSubjectOrgan oSubjectGroup id is {}", oSubjectContact.getId());
        }

        oSubjectGroup.setoSubject(oSubject);
        LOG.info("setSubjectOrgan oSubject id is {}", oSubject.getId());
        oSubjectGroupDao.saveOrUpdate(oSubjectGroup);
        if (isCreation) {
            SubjectUtils.checkID(oSubjectGroup);
        }
        LOG.debug("set SubjectGroup, id: {}, sID: {}", oSubjectGroup.getId(), oSubjectGroup.getsID_Group_Activiti());

        if (sID_Group_Activiti_Parent != null) {
            SubjectGroup oSubjectGroupParent = oSubjectGroupService.getSubjectGroup(sID_Group_Activiti_Parent);
            oSubjectService.setParentDepart(oSubjectGroup, oSubjectGroupParent, ORGAN);
        } else {
            oSubjectGroup.setsChain(sID_Group_Activiti);
            oSubjectGroup.setsSubjectGroup_Company(sName);
            oSubjectGroupDao.saveOrUpdate(oSubjectGroup);
        }
        LOG.info("setSubjectOrgan oSubjectGroup id is {}", oSubjectGroup.getId());

        String sAction = null;
        
        if(isCreation){
            sAction =  oServerEntitySyncService.INSERT_ACTION;
        }else{
            sAction = oServerEntitySyncService.UPDATE_ACTION;
        }
        
        if(isSync == null || isSync == false){
            oServerEntitySyncService.addRecordToServerEntitySync(oSubjectGroup.getsID_Group_Activiti(),  sAction, "SubjectOrgan");
              
            new Thread(new Runnable() {
                public void run() {
                    oServerEntitySyncService.runServerEntitySync("SubjectOrgan", oSubjectGroup.getsID_Group_Activiti());
                }
            }).start();
        }
        
        return oSubjectGroup;
    }

    private String generatesID(String sName, String sID_Group_Activiti, String sID_Group_Activiti_Parent) {
        boolean bCompany = sID_Group_Activiti_Parent == null && sID_Group_Activiti != null;
        boolean bDepartCreation = sID_Group_Activiti_Parent != null && sID_Group_Activiti == null;
        boolean bDepartEdition = sID_Group_Activiti_Parent != null && sID_Group_Activiti != null;

        if (bDepartCreation) {
            SubjectGroup oSubjectGroupParent = oSubjectGroupService.getSubjectGroup(sID_Group_Activiti_Parent);
            String sPrefix = oSubjectGroupParent.getsChain();
            if (!sPrefix.endsWith("_")) {
                sPrefix = sPrefix.concat("_");
            }
            String sTranslitedName = Tool.sTextTranslit(sName.replaceAll(" ", "_"));
            return sPrefix + sTranslitedName;
        }
        return sID_Group_Activiti;
    }

    private SubjectOrgan getOrCreateSubjectOrgan(Subject oSubject) {
        SubjectOrgan oSubjectOrgan = oSubjectOrganDao.getSubjectOrgan(oSubject);
        if (oSubjectOrgan == null) {
            oSubjectOrgan = new SubjectOrgan();
            oSubjectOrgan.setoSubject(oSubject);
        }
        return oSubjectOrgan;
    }

    public boolean isOrgan(SubjectGroup oSubjectGroup) {
        Subject oSubject = oSubjectGroup.getoSubject();
        SubjectOrgan oSubjectOrgan = oSubjectOrganDao.getSubjectOrgan(oSubject);
        return oSubjectOrgan != null;
    }

    public SubjectOrgan getSubjectOrgan(String sOKPO) {
        LOG.info(String.format("find SubjectOrgan entity by sOKPO=%s", sOKPO));
        return oSubjectOrganDao.getSubjectOrgan(sOKPO);
    }

    public SubjectOrgan setSubjectOrgan(String sOKPO, String sNameFull) {
        LOG.info(String.format("create SubjectOrgan entity by sOKPO=%s, sNameFull=%s", sOKPO, sNameFull));
        SubjectOrgan oSubjectOrgan = new SubjectOrgan();
        oSubjectOrgan.setsOKPO(sOKPO);
        oSubjectOrgan.setsNameFull(sNameFull);
        Subject oSubject = new Subject();
        oSubject.setsLabel(sNameFull);
        oSubjectOrgan.setoSubject(oSubject);
        return oSubjectOrganDao.saveOrUpdateSubjectOrgan(oSubjectOrgan);
    }

    public Map<String, Object> getChildren(String sOKPO) {
        return oSubjectGroupService.getChildren(oSubjectOrganDao.findByExpected("sOKPO", sOKPO));
    }

}
