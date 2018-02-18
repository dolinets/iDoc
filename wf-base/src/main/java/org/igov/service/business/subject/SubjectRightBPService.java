package org.igov.service.business.subject;

import javassist.NotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.repository.ProcessDefinition;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.model.subject.SubjectRightBP;
import org.igov.model.subject.SubjectRightBPDao;
import org.igov.service.business.subject.criteria.HierarchyCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubjectRightBPService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectRightBPService.class);

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RepositoryService oRepositoryService;

    @Autowired
    private SubjectRightBPDao subjectRightBPDao;

    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;

    @Autowired
    private SubjectGroupDao subjectGroupDao;

    public List<Map<String, String>> getBPs_ForExport(String sLogin) {

        List<Map<String, String>> aResultMap = new ArrayList<>();

        List<Group> aGroup = identityService.createGroupQuery().groupMember(sLogin).list();

        List<String> asID_Group = new ArrayList<>();

        if (aGroup != null) {
            aGroup.stream().forEach(group -> asID_Group.add(group.getId()));
        }

        List<SubjectRightBP> aSubjectRightBP = subjectRightBPDao.findAllByInValues("asID_Group_Export", asID_Group);

        if (aSubjectRightBP != null) {
            for (SubjectRightBP oSubjectRightBP : aSubjectRightBP) {
                List<ProcessDefinition> aProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                        .processDefinitionKeyLike(oSubjectRightBP.getsID_BP()).active().latestVersion().list();
                Map<String, String> mSubjectRightBP = new HashMap<>();
                mSubjectRightBP.put("sID_BP", oSubjectRightBP.getsID_BP());
                mSubjectRightBP.put("sNote", oSubjectRightBP.getsNote());
                if (!aProcessDefinition.isEmpty()) {
                    mSubjectRightBP.put("sName_BP", aProcessDefinition.get(0).getName());
                } else {
                    mSubjectRightBP.put("sName_BP", "");
                }
                aResultMap.add(mSubjectRightBP);
            }
        }

        return aResultMap;
    }

    public List<SubjectRightBPVO> getBPs_ForReferent(String sLogin, String sID_Group_Referent, Long nID_SubjectHumanPositionCustom_Referent) {
        List<SubjectRightBPVO> aResSubjectRightBPVO = new ArrayList<>();
        if (sLogin != null) {
            try {
                aResSubjectRightBPVO.addAll(getBPs_ForReferent_bysLogin(sLogin));
            } catch (NotFoundException e) {
                LOG.error("getBPs_ForReferent_bysLogin, error: {}", e);
            }
        }
        if (sID_Group_Referent != null) {
            aResSubjectRightBPVO.addAll(getBPs_ForReferent_bysID_Group_Referent(sID_Group_Referent));
        }
        if (nID_SubjectHumanPositionCustom_Referent != null) {
            aResSubjectRightBPVO.addAll(getBPs_ForReferent_bynID_SubjectHumanPositionCustom_Referent(nID_SubjectHumanPositionCustom_Referent));
        }
        return aResSubjectRightBPVO;
    }

    public List<Map<String, String>> getAllBPs() {
        List<Map<String, String>> amListOfBPs = new ArrayList<>();
        List<ProcessDefinition> aoProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                .active().latestVersion().list();
        for (ProcessDefinition oProcessDefinition : aoProcessDefinition) {
            Map<String, String> mBP = new HashMap<>();
            mBP.put("sID", oProcessDefinition.getKey());
            mBP.put("sName", oProcessDefinition.getName());
            amListOfBPs.add(mBP);
        }

        return amListOfBPs;
    }

    public List<SubjectRightBPVO> getBPs_ForReferent_bysLogin(String sLogin) throws NotFoundException {
        List<Group> aGroup = identityService.createGroupQuery().groupMember(sLogin).list();
        List<String> asID_Group = new ArrayList<>();
        if (aGroup != null) {
            aGroup.stream().forEach(group -> asID_Group.add(group.getId()));
        }

        try {
            SubjectGroup oSubjectGroup = oSubjectGroupTreeService.getCompany(sLogin);
            asID_Group.add(oSubjectGroup.getsID_Group_Activiti());
        } catch (Exception ex) {
            LOG.error("Can't get Company by Login: " + sLogin, ex);
        }

        LOG.info("!!!In the method getSubjectRightBPs sLogin={}, asID_Group={}", sLogin, asID_Group);

        List<SubjectRightBP> aSubjectRightBP = subjectRightBPDao.findAllByInValues("sID_Group_Referent", asID_Group);
        LOG.info("In the method getSubjectRightBPs aSubjectRightBP {}", aSubjectRightBP);

        return getBPs_ForReferent_byaSubjectRightBP(aSubjectRightBP);
    }

    public List<SubjectRightBPVO> getBPs_ForReferent_bysID_Group_Referent(String sID) {
        List<String> asID_Group = oSubjectGroupTreeService.getSubjectGroupsTreeUp(sID, SubjectGroupTreeService.ORGAN)
                .stream()
                .map(SubjectGroup::getsID_Group_Activiti)
                .collect(Collectors.toList());
        asID_Group.add(sID);

        List<SubjectRightBP> aSubjectRightBP = subjectRightBPDao.findAllByInValues("sID_Group_Referent", asID_Group);
        return getBPs_ForReferent_byaSubjectRightBP(aSubjectRightBP);
    }

    public List<SubjectRightBPVO> getBPs_ForReferent_bynID_SubjectHumanPositionCustom_Referent(Long nID) {
        List<SubjectRightBP> aSubjectRightBP = subjectRightBPDao.findAllBy("nID_SubjectHumanPositionCustom_Referent", nID);

        List<String> asID_Group = subjectGroupDao.findAllBy("oSubjectHumanPositionCustom.id", nID)
                .stream()
                .flatMap(sg -> oSubjectGroupTreeService.getHierarchy(HierarchyCriteria.departsUp(sg.getsID_Group_Activiti()).setbIncludeRoot(true)).stream())
                .map(SubjectGroup::getsID_Group_Activiti)
                .distinct()
                .collect(Collectors.toList());

        aSubjectRightBP.addAll(subjectRightBPDao.findAllByInValues("sID_Group_Referent", asID_Group));

        return getBPs_ForReferent_byaSubjectRightBP(aSubjectRightBP);
    }

    private List<SubjectRightBPVO> getBPs_ForReferent_byaSubjectRightBP(List<SubjectRightBP> aSubjectRightBP) {
        List<SubjectRightBPVO> aResSubjectRightBPVO = new ArrayList<>();
        for (SubjectRightBP oSubjectRightBP : aSubjectRightBP) {

            if (oSubjectRightBP != null) {
                String sID_BP = oSubjectRightBP.getsID_BP();
                LOG.info("In the method getSubjectRightBPs oFindedSubjectRightBP {}", oSubjectRightBP.getsID_BP());

                List<ProcessDefinition> aProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                        .processDefinitionKeyLike(sID_BP).active().latestVersion().list();

                if (!aProcessDefinition.isEmpty()) {
                    String sName_BP = aProcessDefinition.get(0).getName();
                    SubjectRightBPVO oSubjectRightBP_VO = new SubjectRightBPVO();
                    oSubjectRightBP_VO.setoSubjectRightBP(oSubjectRightBP);
                    oSubjectRightBP_VO.setsName_BP(sName_BP);

                    aResSubjectRightBPVO.add(oSubjectRightBP_VO);
                }
            }
            LOG.info("In the method getSubjectRightBPs oSubjectRightBP is null");
        }
        return aResSubjectRightBPVO;
    }

    /**
     * Создать права на создание БП. Можно добавлять как все критерии сразу так и комбинациями: ид БП - ид группы,
     * ид БП - ид должности.
     *
     * @param sID_BP ид бизнес процесса
     * @param sID_Group_Referent ид группы
     * @param nID_SubjectHumanPosition ид должности
     */
    public void setBP(String sID_BP, String sID_Group_Referent, Long nID_SubjectHumanPosition) {
        SubjectRightBP oSubjectRightBP = new SubjectRightBP();
        oSubjectRightBP.setsID_BP(sID_BP);
        if (sID_Group_Referent != null && !"".equals(sID_Group_Referent.trim())) {
            oSubjectRightBP.setsID_Group_Referent(sID_Group_Referent);
        }
        if (nID_SubjectHumanPosition != null) {
            oSubjectRightBP.setnID_SubjectHumanPositionCustom_Referent(nID_SubjectHumanPosition);
        }
        oSubjectRightBP.setsID_Place_UA("");
        LOG.info("Created SubjectRightBP={}", oSubjectRightBP);
        subjectRightBPDao.saveOrUpdate(oSubjectRightBP);
    }

    /**
     * Удалить права на создание БП. Можно удалять как по всем критериям сразу так и по комбинации: ид БП -
     * ид группы, ид БП - ид должности.
     *
     * @param sID_BP ид процесса
     * @param sID_Group_Referent ид группы
     * @param nID_SubjectHumanPosition ид должности
     */
    public void removeBP(String sID_BP, String sID_Group_Referent, Long nID_SubjectHumanPosition) {
        List<SubjectRightBP> aoSubjectRightBP = subjectRightBPDao.findBy(sID_BP, sID_Group_Referent, nID_SubjectHumanPosition);
        LOG.info("finded {} SubjectRightBP for removing", aoSubjectRightBP.size());
        subjectRightBPDao.delete(aoSubjectRightBP);
    }

}
