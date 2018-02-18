package org.igov.service.business.subject;

import com.google.common.base.Optional;
import org.igov.io.GeneralConfig;
import org.igov.model.document.DocumentStepSubjectRightDao;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.*;
import org.igov.model.subject.vo.SubjectHumanVO;
import org.igov.model.subject.vo.SubjectHumanVO_Compact;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import org.igov.service.business.subject.criteria.HierarchyCriteria;
import org.igov.service.business.util.SubjectUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.igov.service.business.subject.SubjectGroupTreeService.ORGAN;

/**
 *
 * @author idenysenko
 */
@Service
public class SubjectHumanService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanService.class);
    
    @Autowired private SubjectHumanDao oSubjectHumanDao;
    @Autowired private GeneralConfig generalConfig;
    @Autowired private SubjectGroupDao oSubjectGroupDao;
    @Autowired private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;
    @Autowired private SubjectDao oSubjectDao;
    @Autowired private SubjectContactDao oSubjectContactDao;
    @Autowired private SubjectContactTypeDao oSubjectContactTypeDao;
    @Autowired private SubjectGroupTreeService oSubjectGroupTreeService;
    @Autowired private SubjectHumanPositionCustomDao subjectHumanPositionCustomDao;
    @Autowired private SubjectGroupTreeDao oSubjectGroupTreeDao;
    @Autowired private ServerDao oServerDao;
    @Autowired private SubjectStatusDao oSubjectStatusDao;
    @Autowired private UsersService oUsersService;
    @Autowired private SubjectService oSubjectService;
    @Autowired private SubjectGroupService oSubjectGroupService;
    @Autowired private ServerEntitySyncService oServerEntitySyncService;
    @Autowired private SubjectOrganService oSubjectOrganService;

    public boolean isHuman(SubjectGroup oSubjectGroup) {
        Subject oSubject = oSubjectGroup.getoSubject();
        SubjectHuman oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);
        return oSubjectHuman != null;
    }
    public SubjectGroup getSubjectGroup(SubjectHuman oSubjectHuman) {
        return oSubjectGroupDao
                .findByExpected("oSubject.id", oSubjectHuman.getoSubject().getId());
    }
    
     /**
     * Получить всех внешних SubjectHuman из документа.
     * 
     * @param snID_Process_Activiti ид процесса
     * @return лист SubjectHuman
     */
    public List<SubjectHuman> getExternalSubjectHumanFromDocument(String snID_Process_Activiti) {
        Set<String> asID_Group_Activiti = oDocumentStepSubjectRightDao.findDocumentParticipant(snID_Process_Activiti);

        return oSubjectHumanDao.getExternalSubjectHumanByIdGroupActiviti(asID_Group_Activiti, generalConfig.getSelfHost());
    }

    public SubjectHuman getSubjectHuman(String sID_Group_Activiti) {
        SubjectGroup oSubjectGroup = oSubjectGroupService.getSubjectGroup(sID_Group_Activiti);
        Subject oSubject = oSubjectGroup.getoSubject();
        return oSubjectHumanDao.getSubjectHuman(oSubject);
    }

    public SubjectHumanVO getSubjectHumanVO(String sID_Group_Activiti, Boolean  bIncludeSubjectGroupChilds) {
        //по дефолту считаем, что дети не нужны
        if (bIncludeSubjectGroupChilds == null) {
            bIncludeSubjectGroupChilds = false;
        }
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        if (bIncludeSubjectGroupChilds) {
            oSubjectGroup.setaSubjectGroup(oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.employeesOf(sID_Group_Activiti)));
        }
        SubjectHumanVO oSubjectHumanVO = new SubjectHumanVO();

        Subject oSubject = oSubjectGroup.getoSubject();
        oSubject.setaSubjectAccountContact(oSubjectContactDao.findContacts(oSubject));
        oSubjectHumanVO.setoSubjectGroup(oSubjectGroup);

        SubjectHuman oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);
        oSubjectHuman.setoSubject(null); // already exist in response
        SubjectContact defaultPhone = oSubjectHuman.getDefaultPhone();
        SubjectContact defaultEmail = oSubjectHuman.getDefaultEmail();
        oSubjectHuman.getaSubjectHumanRole().size();
        oSubjectHumanVO.setoSubjectHuman(oSubjectHuman);

        SubjectGroup oSubjectGroupTopHead = oSubjectGroupTreeService
                .getHierarchy(HierarchyCriteria.bossOf(oSubjectGroup.getsID_Group_Activiti()))
                .stream()
                .findAny()
                .orElse(null);
        oSubjectHumanVO.setoSubjectGroupHead(oSubjectGroupTopHead);

        List<SubjectGroup> aoSubjectGroupTreeUp = oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.departsUp(sID_Group_Activiti));
        oSubjectHumanVO.setaSubjectGroupTreeUp(aoSubjectGroupTreeUp);

        oSubjectHumanVO.setbHead(isHead(oSubjectGroup));
        try {
            oSubjectHumanVO.setmUserGroupMember(oUsersService.getFioUserGroupMember(sID_Group_Activiti));
        } catch (Exception e) {
            LOG.warn("getFioUserGroupMember, sID: {}, error: {}", sID_Group_Activiti, e);
        }

        return oSubjectHumanVO;
    }

    private boolean isHead(SubjectGroup oSubjectGroup) {
        boolean hasEmployees = oSubjectGroupTreeDao.getaSubjectGroupTreeChildren(oSubjectGroup).size() > 0;
        if (!hasEmployees) {
            //
            SubjectGroup oDepart = oSubjectGroupTreeService
                    .getHierarchy(HierarchyCriteria.parentDepartOf(oSubjectGroup.getsID_Group_Activiti()))
                    .stream()
                    .findAny()
                    .orElse(null);
            if (oDepart != null) {
                List<SubjectGroup> aoEmployeeInDepart = oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.employeesOf(oDepart.getsID_Group_Activiti()));
                return aoEmployeeInDepart.size() == 1;
            }
            return false;
        }
        return true;
    }
    
    public SubjectHumanVO_Compact getSubjectHumanVO_Compact(String sID_Group_Activiti) {
        //по дефолту считаем, что дети не нужны
        /*if (bIncludeSubjectGroupChilds == null) {
            bIncludeSubjectGroupChilds = false;
        }*/
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        /*if (bIncludeSubjectGroupChilds) {
            oSubjectGroup.setaSubjectGroup(oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.employeesOf(sID_Group_Activiti)));
        }*/
        SubjectHumanVO_Compact oSubjectHumanVO = new SubjectHumanVO_Compact();

        Subject oSubject = oSubjectGroup.getoSubject();
        //oSubject.setaSubjectAccountContact(oSubjectContactDao.findContacts(oSubject));
        //oSubjectHumanVO.setoSubjectGroup(oSubjectGroup);

        SubjectHuman oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);
        oSubjectHuman.setoSubject(null); // already exist in response
        //SubjectContact defaultPhone = oSubjectHuman.getDefaultPhone();
        //SubjectContact defaultEmail = oSubjectHuman.getDefaultEmail();
        //oSubjectHuman.getaSubjectHumanRole().size();
        oSubjectHumanVO.setoSubjectHuman(oSubjectHuman);

        /*SubjectGroup oSubjectGroupTopHead = oSubjectGroupTreeService
                .getHierarchy(HierarchyCriteria.bossOf(oSubjectGroup.getsID_Group_Activiti()))
                .stream()
                .findAny()
                .orElse(null);
        oSubjectHumanVO.setoSubjectGroupHead(oSubjectGroupTopHead);*/

        /*List<SubjectGroup> aoSubjectGroupTreeUp = oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.departsUp(sID_Group_Activiti));
        oSubjectHumanVO.setaSubjectGroupTreeUp(aoSubjectGroupTreeUp);*/

        /*oSubjectHumanVO.setbHead(oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Parent", oSubjectGroup).size() > 0);
        try {
            oSubjectHumanVO.setmUserGroupMember(oUsersService.getFioUserGroupMember(sID_Group_Activiti));
        } catch (Exception e) {
            LOG.warn("getFioUserGroupMember, sID: {}, error: {}", sID_Group_Activiti, e);
        }*/

        return oSubjectHumanVO;
    }    

    public SubjectGroup createSubjectHuman(String sLoginNew, String sPassword,
            String sFamily, String sName, String sSurname, String sPosition, String sStatus, String sDateBirth, String sEmail, String sPhone,
            String sID_Group_Activiti_Organ, Boolean isHead, Long nID_Server, Boolean isSync, String sLogin, String sLoginReferent) {
        
        //добавление чейна к логину PLATFORM-589
        String sChain = null;
        if(sLogin != null){
            SubjectGroup oSubjectGroupForChain = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
            sChain = oSubjectGroupForChain.getsChain();
            if (!sLoginNew.startsWith(sChain)) {
                if (sLoginNew.toLowerCase().startsWith(sChain.toLowerCase())) {
                    int nLength = sChain.length();
                    sLoginNew = sChain + sLoginNew.substring(nLength);
                } else {
                    sLoginNew = sChain + sLoginNew;
                }
            }
        }
                
        Optional<SubjectGroup> oSubjectGroupWrapper = oSubjectGroupDao.findBy("sID_Group_Activiti", sLoginNew);
        if (oSubjectGroupWrapper.isPresent()) {
            throw new RuntimeException("Такий логін вже існує");
        }
        SubjectGroup oSubjectGroup = setSubjectHuman(sLoginNew, sPassword, sFamily, sName, sSurname, sPosition, sStatus, sDateBirth, sEmail, 
                sPhone, sID_Group_Activiti_Organ, isHead, nID_Server, true);
        
        if(isSync == null || isSync == false){
             oServerEntitySyncService.addRecordToServerEntitySync(oSubjectGroup.getsID_Group_Activiti(), oServerEntitySyncService.INSERT_ACTION, "SubjectHuman");
              
        new Thread(new Runnable() {
            public void run() {
                    oServerEntitySyncService.runServerEntitySync("SubjectHuman", oSubjectGroup.getsID_Group_Activiti());
                }
            }).start();
        }
                
        return oSubjectGroup;
    }

    public SubjectGroup updateSubjectHuman(String sLogin, String sPassword,
            String sFamily, String sName, String sSurname, String sPosition, String sStatus, String sDateBirth, String sEmail, String sPhone,
            String sID_Group_Activiti_Organ, Boolean isHead, Long nID_Server, Boolean isSync) {
        Optional<SubjectGroup> oSubjectGroupWrapper = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin);
        if (!oSubjectGroupWrapper.isPresent()) {
            throw new RuntimeException("Такого логіну не існує");
        }
        
        SubjectGroup oSubjectGroup = setSubjectHuman(sLogin, sPassword, sFamily, sName, sSurname, sPosition, 
                sStatus, sDateBirth, sEmail, sPhone, sID_Group_Activiti_Organ, isHead, nID_Server, false);
        if(isSync == null || isSync == false){
             oServerEntitySyncService.addRecordToServerEntitySync(oSubjectGroup.getsID_Group_Activiti(), oServerEntitySyncService.UPDATE_ACTION, "SubjectHuman");
              
          new Thread(new Runnable() {
              public void run() {
                  oServerEntitySyncService.runServerEntitySync("SubjectHuman", oSubjectGroup.getsID_Group_Activiti());
                  }
              }).start();
          }
        
        //oServerEntitySyncService.startSyncFromService("SubjectHuman", oSubjectGroup.getsID_Group_Activiti(), oServerEntitySyncService.UPDATE_ACTION, isSync);
        return oSubjectGroup;
    }

    public SubjectGroup setSubjectHuman(String sLogin, String sPassword,
            String sFamily, String sName, String sSurname, String sPosition, String sStatus, String sDateBirth, String sEmail, String sPhone,
            String sID_Group_Activiti_Organ, Boolean isHead, Long nID_Server, Boolean isNewSubject) {

        SubjectGroup oSubjectGroupOrgan = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti_Organ);

        SubjectGroup oSubjectGroup = oSubjectGroupService.getOrCreateSubjectGroup(sLogin);
        Subject oSubject = oSubjectService.getOrCreateSubject(oSubjectGroup);
        SubjectHuman oSubjectHuman = getOrCreateSubjectHuman(oSubject);
        
        boolean isCreation = oSubjectGroup.getId() == null;

        String sFio = getSubjectLabel(sFamily, sName, sSurname);

        oSubjectGroup.setName(sFio);
        oSubjectGroup.setsChain(sLogin);
        if (sPosition != null) {
            SubjectHumanPositionCustom oPosition = subjectHumanPositionCustomDao.findByExpected("name", sPosition);
            oSubjectGroup.setoSubjectHumanPositionCustom(oPosition);
        }

        oSubject.setsLabel(sFio);
        if (sStatus != null) {
            Optional<SubjectStatus> oSubjectStatusWrapper = oSubjectStatusDao.findBy("sName", sStatus);
            if (oSubjectStatusWrapper.isPresent()) {
                oSubject.setoSubjectStatus(oSubjectStatusWrapper.get());
            }
        } else {
            oSubject.setoSubjectStatus(oSubjectStatusDao.getWorkStatus());
        }
        oSubjectDao.saveOrUpdate(oSubject);
        LOG.info("setSubjectHuman oSubject id is {}", oSubject.getId());
        
        if (isCreation) {
            SubjectUtils.checkID(oSubject);
        }
        LOG.debug("set Subject, id: {}", oSubject.getId());

        oSubjectHuman.setsFamily(sFamily);
        oSubjectHuman.setName(sName);
        oSubjectHuman.setsSurname(sSurname);
        if (sDateBirth != null) {
            oSubjectHuman.setsDateBirth(LocalDateTime.parse(sDateBirth, DateTimeFormat.forPattern("dd.MM.yyyy")).toDateTime());
        }
        if (nID_Server != null) {
            Server oServer = oServerDao.findByIdExpected(nID_Server);
            oSubjectHuman.setoServer(oServer);
        } else if(isNewSubject) {
            Server oServer = oServerDao.findByIdExpected(Long.valueOf(generalConfig.getSelfServerId()));
            oSubjectHuman.setoServer(oServer);
        }
        
        if (sEmail != null) {
            SubjectContact oEmail = getHumanEmail(oSubjectHuman);
            oEmail.setsValue(sEmail);
            oSubjectContactDao.saveOrUpdate(oEmail);
            LOG.info("setSubjectHuman oEmail id is {}", oEmail.getId());
        }
        if (sPhone != null) {
            SubjectContact oPhone = getHumanPhone(oSubjectHuman);
            oPhone.setsValue(sPhone);
            oSubjectContactDao.saveOrUpdate(oPhone);
            LOG.info("setSubjectHuman oPhone id is {}", oPhone.getId());
            
        }
        oSubjectHumanDao.saveOrUpdate(oSubjectHuman);
        if (isCreation) {
            SubjectUtils.checkID(oSubjectHuman);
        }
        LOG.debug("set SubjectHuman, id: {}", oSubjectHuman.getId());

        oSubjectGroup.setoSubject(oSubject);
        if (sPosition != null) {
            SubjectHumanPositionCustom oPosition = subjectHumanPositionCustomDao.findByExpected("name", sPosition);
            oSubjectGroup.setoSubjectHumanPositionCustom(oPosition);
        }
        oSubjectGroupDao.saveOrUpdate(oSubjectGroup);
        if (isCreation) {
            SubjectUtils.checkID(oSubjectGroup);
        }
        LOG.debug("set SubjectGroup, id: {}, sID: {}", oSubjectGroup.getId(), oSubjectGroup.getsID_Group_Activiti());

        oSubjectService.setParentDepart(oSubjectGroup, oSubjectGroupOrgan, ORGAN);
        if (isHead != null) {
            setHead(sLogin, sID_Group_Activiti_Organ, isHead);
        }

        oUsersService.setHuman(sLogin, sPassword, sFamily, sName, sSurname, sEmail);
        
        LOG.info("setSubjectHuman oSubjectHuman id is {}", oSubjectHuman.getId());

        return oSubjectGroup;
    }

    private SubjectHuman getOrCreateSubjectHuman(Subject oSubject) {
        SubjectHuman oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);
        if (oSubjectHuman == null) {
            oSubjectHuman = new SubjectHuman();
            oSubjectHuman.setoSubject(oSubject);
        }
        return oSubjectHuman;
    }

    private SubjectContact getHumanEmail(SubjectHuman subjectHuman) {
        SubjectContact oSubjectContact = subjectHuman.getDefaultEmail();
        if (oSubjectContact == null) {
            oSubjectContact = createSubjectContact(subjectHuman.getoSubject(), oSubjectContactTypeDao.getEmailType());
            subjectHuman.setDefaultEmail(oSubjectContact);
        }
        return oSubjectContact;
    }

    private SubjectContact getHumanPhone(SubjectHuman subjectHuman) {
        SubjectContact oSubjectContact = subjectHuman.getDefaultPhone();
        if (oSubjectContact == null) {
            oSubjectContact = createSubjectContact(subjectHuman.getoSubject(), oSubjectContactTypeDao.getPhoneType());
            subjectHuman.setDefaultPhone(oSubjectContact);
        }
        return oSubjectContact;
    }

    private SubjectContact createSubjectContact(Subject oSubject, SubjectContactType oSubjectContactType) {
        SubjectContact oSubjectContact = new SubjectContact();
        oSubjectContact.setSubjectContactType(oSubjectContactType);
        oSubjectContact.setSubject(oSubject);
        oSubjectContact.setsDate();
        return oSubjectContact;
    }

    private void setHead(String sID_Group_Activiti, String sID_Group_Activiti_Organ, boolean isHead) {
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        SubjectGroup oSubjectGroupOrgan = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti_Organ);
        SubjectGroup oSubjectGroupHeadPrev = oSubjectGroupTreeService.getHeadInDepart(oSubjectGroupOrgan);

        LOG.info("handle the head in depart: {}, human: {}, isHead: {}", oSubjectGroupOrgan.getName(), oSubjectGroup.getName(), isHead);

        List<SubjectGroup> aoEmployeeInDepart = oSubjectGroupTreeService
                .getHierarchy(HierarchyCriteria.employeesOf(oSubjectGroupOrgan.getsID_Group_Activiti()));

        if (aoEmployeeInDepart.size() < 2) {
            LOG.info("depart '{}' has 0/1 employees, set human '{}' as head intentionally");
            setBottomDepartHeadsToDepartHead(oSubjectGroupOrgan, oSubjectGroup);
            setTopDepartHeadToDepartHead(oSubjectGroupOrgan, oSubjectGroup);
        } else {
            if (isHead) {
                LOG.info("setting new head '{}' in depart '{}'", oSubjectGroup.getName(), oSubjectGroupOrgan.getName());
                if (oSubjectGroupHeadPrev != null) {
                    LOG.info("depart '{}' previous head: {}", oSubjectGroupOrgan.getName(), oSubjectGroupHeadPrev.getName());
                    if (!oSubjectGroupHeadPrev.getsID_Group_Activiti().equals(oSubjectGroup.getsID_Group_Activiti())) {
                        oSubjectGroupTreeDao.deleteChildrenOf(oSubjectGroup);

                        // set prev children to human (bottom depart head too)
                        oSubjectGroupTreeDao.getaSubjectGroupTreeChildren(oSubjectGroupHeadPrev)
                                .stream()
                                .filter(sgt -> !sgt.getoSubjectGroup_Child().equals(oSubjectGroup)) // if human in depart
                                .forEach(sgt -> {
                                    sgt.setoSubjectGroup_Parent(oSubjectGroup);
                                    oSubjectGroupTreeDao.saveOrUpdate(sgt);
                                });
                        // set prev head to human
                        oSubjectGroupTreeDao.deleteParentsOf(oSubjectGroupHeadPrev, oSubjectGroupOrgan);

                        // remove prev head from human
                        List<SubjectGroupTree> aoSubjectGroupHeadPrevTree = oSubjectGroupTreeDao
                                .getaSubjectGroupTreeParent(oSubjectGroup)
                                .stream()
                                .filter(sgt -> sgt.getoSubjectGroup_Parent().getsID_Group_Activiti().equals(oSubjectGroupHeadPrev.getsID_Group_Activiti()))
                                .collect(Collectors.toList());
                        oSubjectGroupTreeDao.delete(aoSubjectGroupHeadPrevTree);

                        SubjectGroupTree oSubjectGroupTreeSaved = oSubjectGroupTreeService
                                .saveOrUpdate(oSubjectGroupHeadPrev, oSubjectGroup);
                        SubjectUtils.checkID(oSubjectGroupTreeSaved);
                    }
                } else {
                    LOG.info("depart '{}' has no current head");
                    // set employees in depart to human
                    Set<Long> anID = aoEmployeeInDepart
                            .stream()
                            .map(SubjectGroup::getId)
                            .collect(Collectors.toSet());
                    anID.remove(oSubjectGroup.getId());
                    List<SubjectGroupTree> aoSubjectGroupEmployeeSaved = oSubjectGroupTreeDao
                            .getaSubjectGroupTreeChildren(oSubjectGroupOrgan)
                            .stream()
                            .map(SubjectGroupTree::getoSubjectGroup_Child)
                            .filter(sg -> anID.contains(sg.getId()))
                            .map(sg -> oSubjectGroupTreeService.saveOrUpdate(sg, oSubjectGroup))
                            .collect(Collectors.toList());
                    aoSubjectGroupEmployeeSaved.forEach(SubjectUtils::checkID);

                    setBottomDepartHeadsToDepartHead(oSubjectGroupOrgan, oSubjectGroup);
                }

                setTopDepartHeadToDepartHead(oSubjectGroupOrgan, oSubjectGroup);
            } else {
                LOG.info("remove head (or add employee) '{}' from depart '{}'", oSubjectGroup.getName(), oSubjectGroupOrgan.getName());

                oSubjectGroupTreeDao.deleteChildrenOf(oSubjectGroup);
                // remove top
                oSubjectGroupTreeDao.deleteParentsOf(oSubjectGroup, oSubjectGroupOrgan);

                Set<Long> anSubjectGroupTreeParent = oSubjectGroupTreeDao.getaSubjectGroupTreeParent(oSubjectGroup)
                        .stream()
                        .map(SubjectGroupTree::getoSubjectGroup_Parent)
                        .filter(this::isHuman)
                        .map(SubjectGroup::getId)
                        .collect(Collectors.toSet());

                if (oSubjectGroupHeadPrev != null && !anSubjectGroupTreeParent.contains(oSubjectGroupHeadPrev.getId())) {
                    SubjectGroupTree oSubjectGroupTreeSaved = oSubjectGroupTreeService
                            .saveOrUpdate(oSubjectGroup, oSubjectGroupHeadPrev);
                    SubjectUtils.checkID(oSubjectGroupTreeSaved);
                }
            }
        }
    }

    private void setTopDepartHeadToDepartHead(SubjectGroup oDepart, SubjectGroup oEmployee) {
        SubjectGroup oSubjectGroupHeadPrev = oSubjectGroupTreeService.getHeadInDepart(oDepart);
        SubjectGroup oSubjectGroupHeadTop = oSubjectGroupTreeService.getTopBoss(oDepart, 1);
        // set top boss
        if (oSubjectGroupHeadTop != null && oSubjectGroupHeadTop != oSubjectGroupHeadPrev) {
            if (!oSubjectGroupTreeDao.exists(oEmployee, oSubjectGroupHeadTop)) {
                SubjectGroupTree oSubjectGroupTreeSaved = oSubjectGroupTreeService
                        .saveOrUpdate(oEmployee, oSubjectGroupHeadTop);
                SubjectUtils.checkID(oSubjectGroupTreeSaved);
            }
        }
    }

    private void setBottomDepartHeadsToDepartHead(SubjectGroup oDepart, SubjectGroup oDepartHead) {
        List<SubjectGroupTree> aoSubjectGroupTreeSaved = oSubjectGroupTreeService
                .getHierarchy(HierarchyCriteria.bottomDeparts(oDepart.getsID_Group_Activiti()))
                .stream()
                .map(sg -> oSubjectGroupTreeService.getHeadInDepart(sg))
                .filter(Objects::nonNull)
                .map(oHead_BottomDepart -> oSubjectGroupTreeService.saveOrUpdate(oHead_BottomDepart, oDepartHead))
                .collect(Collectors.toList());
        aoSubjectGroupTreeSaved.forEach(SubjectUtils::checkID);
    }

    private static String getSubjectLabel(String sFamily, String sName, String sSurname) {
        return String.format("%s %s %s", sFamily, sName, sSurname);
    }

    public List<SubjectHuman> getExternalSubjectHumanByIdGroupActiviti(Set<String> asID_Group_Activiti) {
        return oSubjectHumanDao.getExternalSubjectHumanByIdGroupActiviti(asID_Group_Activiti, generalConfig.getSelfHost());
    }
}
