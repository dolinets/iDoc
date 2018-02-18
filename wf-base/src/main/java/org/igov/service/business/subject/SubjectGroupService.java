/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.subject;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import javassist.NotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.igov.service.business.subject.SubjectGroupTreeService.HUMAN;
import static org.igov.service.business.subject.SubjectGroupTreeService.ORGAN;

/**
 * Сервис получения организационной иерархии
 *
 * @author inna
 */
@Component("subjectGroupService")
@Service
public class SubjectGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectGroupService.class);
    @Autowired
    private IdentityService identityService;

    @Autowired
    private SubjectHumanDao oSubjectHumanDao;
    
    @Autowired
    private SubjectOrganDao oSubjectOrganDao;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;

    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;

    public SubjectGroup getSubjectGroup(String sID_Group_Activiti) {
        return oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
    }

    public SubjectGroup getSubjectGroup(Subject oSubject) {
        return oSubjectGroupDao.findByExpected("oSubject", oSubject);
    }

    public SubjectGroupResultTree getaSubjectGroup(String sID_Group_Activiti, String sFind) {
        LOG.info("getaSubjectGroup started..");
        long startTime_0 = System.currentTimeMillis();
        List<SubjectGroup> aResultTree = new ArrayList<>();
        SubjectGroupResultTree result = new SubjectGroupResultTree();

        Set<String> asID_Group_Activiti = new HashSet(Arrays.asList(sID_Group_Activiti.split(",")));
        List<SubjectGroup> aSubjectGroup_Company = new ArrayList<>();

        for (String sID_Group_Activiti_Splited : asID_Group_Activiti) {
            try {
                aSubjectGroup_Company.add(oSubjectGroupTreeService.getCompany(sID_Group_Activiti_Splited));
            } catch (NotFoundException ex) {
                LOG.info("Company for {} not found", sID_Group_Activiti_Splited);
            }
        }

        //ищем всех и людей и органов
        List<SubjectGroup> aSubjectGroup = oSubjectGroupDao.findAllByLikeNameOrLogin(sFind.trim());
        List<Long> anSubjectGroup = aSubjectGroup.stream()
                .map(SubjectGroup::getoSubject)
                .filter(Objects::nonNull)
                .map(Subject::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        LOG.info("find aSubjectGroup by  {} size is {} ", sFind, aSubjectGroup.size());

        // получить только отфильтрованный по компаниям список
        List<SubjectGroup> aSubjectGroupFilterCompany = Lists.newArrayList(Collections2.filter(aSubjectGroup, new Predicate<SubjectGroup>() {
            @Override
            public boolean apply(SubjectGroup subjectGroup) {
                return asID_Group_Activiti.contains(subjectGroup.getsChain());
            }
        }));

        long stopTime_1 = System.currentTimeMillis();
        long elapsedTime_1 = stopTime_1 - startTime_0;
        LOG.info("find getaSubjectGroup elapsedTime_1  {}", elapsedTime_1);

        long startTime_2 = System.currentTimeMillis();
        // получить только отфильтрованный по людям список
        List<SubjectHuman> aSubjectHuman = new ArrayList<>();
        if (!anSubjectGroup.isEmpty()) {
            aSubjectHuman.addAll(oSubjectHumanDao.findAllByInValues("oSubject.id", anSubjectGroup));
        }
        LOG.info("find Human by  {} size is {} subject is ", sFind, aSubjectHuman.size(), aSubjectHuman);
        List<SubjectGroup> aSubjectGroupFilterHuman = new ArrayList();
        if (!aSubjectHuman.isEmpty()) {
            List<Long> anID_Subject_SubjectHuman = aSubjectHuman.stream()
                    .map(SubjectHuman::getoSubject)
                    .filter(Objects::nonNull)
                    .map(Subject::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            LOG.info("anID_SubjectHuman.size: " + anID_Subject_SubjectHuman.size());
            // получить только отфильтрованный по компаниям список
            aSubjectGroupFilterHuman = Lists.newArrayList(Collections2.filter(aSubjectGroupFilterCompany, new Predicate<SubjectGroup>() {
                @Override
                public boolean apply(SubjectGroup subjectGroup) {
                    Subject oSubject = subjectGroup.getoSubject();
                    if (oSubject == null) {
                        return false;
                    }
                    Long nID = oSubject.getId();
                    return nID != null && anID_Subject_SubjectHuman.contains(nID);
                }
            }));
        }

        long stopTime_2 = System.currentTimeMillis();
        long elapsedTime_2 = stopTime_2 - startTime_2;
        LOG.info("find getaSubjectGroup elapsedTime_1 {}", elapsedTime_2);

        long startTime_3 = System.currentTimeMillis();

        for (SubjectGroup oSubjectGroup : aSubjectGroupFilterHuman) {
            //oSubjectGroup.setsSubjectGroup_Company(getCompanyName(asID_Group_Activiti, oSubjectGroup));

            for (SubjectGroup oSubjectGroup_Company : aSubjectGroup_Company) {
                if (oSubjectGroup_Company.getsID_Group_Activiti().equals(oSubjectGroup.getsChain())) {
                    oSubjectGroup.setsSubjectGroup_Company(oSubjectGroup_Company.getName());
                }
            }

            oSubjectGroup.setaUser(getSubjectUserByActivitiGroup(oSubjectGroup.getsID_Group_Activiti()));
        }
        
        LOG.info("aSubjectGroupFilterHuman sFind size is {}", aSubjectGroupFilterHuman.size());
        //aResultTree.addAll(aSubjectGroupFilterHuman);
        aResultTree.addAll(oSubjectGroupTreeService.filtrChildResultByUser_New(sFind, aSubjectGroupFilterHuman));
        result.setaSubjectGroupTree(aResultTree);

        long stopTime_3 = System.currentTimeMillis();
        long elapsedTime_3 = stopTime_3 - startTime_3;
        LOG.info("find getaSubjectGroup elapsedTime_3 {}", elapsedTime_3);

        return result;
    }

    public List<SubjectUser> getSubjectUserByActivitiGroup(String sID_Group) {
        if (sID_Group == null) {
            List<User> aoUser = identityService.createUserQuery().list();
            return aoUser
                       .stream()
                       .map(SubjectUser.BuilderHelper::buildByActivitiUser)
                       .collect(Collectors.toList());
        }

        List<User> aoUser = identityService.createUserQuery().memberOfGroup(sID_Group).list();
        return aoUser
                   .stream()
                   .filter(u -> u.getId().contains(sID_Group) || u.getId().equalsIgnoreCase(sID_Group))
                   .map(SubjectUser.BuilderHelper::buildByActivitiUser)
                   .collect(Collectors.toList());
    }

    private String getCompanyName(Set<String> asID_Group_Activiti, SubjectGroup oSubjectGroup) {

        List<SubjectGroup> aSubjectGroupCompany = new ArrayList<>();
        SubjectGroup oSubjectGroupCompany = null;
        for (String sID_Group_Activiti_Company : asID_Group_Activiti) {
            try {
                oSubjectGroupCompany = oSubjectGroupTreeService.getCompany(sID_Group_Activiti_Company);
            } catch (NotFoundException ex) {
                LOG.info("NotFoundException with getCompany: " + ex.getMessage());
            }
            aSubjectGroupCompany.add(oSubjectGroupCompany);
        }

        SubjectGroup oSubjectGroupCompanyName = null;
        for (SubjectGroup oSubjectGroupNameCompany : aSubjectGroupCompany) {
            if (oSubjectGroupNameCompany.getsID_Group_Activiti().equals(oSubjectGroup.getsChain())) {
                oSubjectGroupCompanyName = oSubjectGroupNameCompany;
            }
        }

        String sCompanyName = "";
        if (oSubjectGroupCompanyName != null) {
            sCompanyName = oSubjectGroupCompanyName.getName();
        }

        return sCompanyName;
    }
    
    public String getSubjectType(String sID_Group_Activiti) {
        try {
            Optional<SubjectGroup> oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti);
            if (oSubjectGroup.isPresent()) {
                Subject oSubject = oSubjectGroup.get().getoSubject();


                if(oSubject != null){
                    LOG.info("oSubjectGroup in getSubjectType is " + oSubject.getId());
                    Optional<SubjectHuman> oSubjectHuman = oSubjectHumanDao.findBy("oSubject", oSubject);
                    LOG.info("sID_Group_Activiti: {} oSubjectHuman isPresent: {}", sID_Group_Activiti, oSubjectHuman.isPresent());

                    if (oSubjectHuman.isPresent()) {
                        return HUMAN;
                    } else {
                        Optional<SubjectOrgan> oSubjectOrgan = oSubjectOrganDao.findBy("oSubject", oSubject);
                        LOG.info("sID_Group_Activiti: {} oSubjectOrgan isPresent: {}", sID_Group_Activiti, oSubjectOrgan.isPresent());
                        if (oSubjectOrgan.isPresent()) {
                            return ORGAN;
                        } else {
                            /*throw new RuntimeException("Can't find any SubjectHuman or SubjectOrgan for sID_Group_Activiti = "
                                    + sID_Group_Activiti + " Subject = " + oSubject.getId());
                            */
                            return null;
                        }
                    }
                }else{
                    return null;
                }
            } else {
                LOG.info("Can't find any SubjectGroup for sID_Group_Activiti = " + sID_Group_Activiti);
                return "";
            }
        } catch (Exception oException) {
            LOG.error("ERROR: ", oException);
            throw oException;
        }
    }

    public SubjectGroup getOrCreateSubjectGroup(String sID_Group_Activiti) {
        Optional<SubjectGroup> oSubjectGroupWrapper = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti);
        if (!oSubjectGroupWrapper.isPresent()) {
            SubjectGroup oSubjectGroup = new SubjectGroup();
            oSubjectGroup.setsID_Group_Activiti(sID_Group_Activiti);
            return oSubjectGroup;
        }
        return oSubjectGroupWrapper.get();
    }

    public List<SubjectGroup> findSubjectGroupInCompany(String sID_Group_Activiti, String sNameOrLogin, String sSubjectType) {
        String sChain = getSubjectGroup(sID_Group_Activiti).getsChain();
        List<SubjectGroup> aoSubjectGroup;
        if (HUMAN.equalsIgnoreCase(sSubjectType)) {
            aoSubjectGroup = oSubjectGroupDao.findHumansInCompanyByLikeNameOrLogin(sChain, sNameOrLogin);
        } else {
            aoSubjectGroup = oSubjectGroupDao.findDepartsInCompanyByLikeName(sChain, sNameOrLogin);
        }
        return aoSubjectGroup;
    }

    public SubjectGroup getCompany(SubjectGroup oSubjectGroup) {
        return getSubjectGroup(oSubjectGroup.getsChain());
    }

    /**
     * get employees in depart and bottom departs
     */
    public Map<String, Object> getChildren(SubjectOrgan oSubjectOrgan) {
        Map<String, Object> oResultMap = new HashMap<>();
        List<SubjectHuman> aoSubjectHuman = new ArrayList<>();
        List<SubjectOrgan> aoSubjectOrgan = new ArrayList<>();

        SubjectGroup oSubjectGroup = getCompany(getSubjectGroup(oSubjectOrgan.getoSubject()));

        List<Subject> aoSubject_Human = oSubjectGroupDao
                .findHumansInCompany(oSubjectGroup.getsID_Group_Activiti())
                .stream()
                .map(SubjectGroup::getoSubject)
                .collect(Collectors.toList());
        if (aoSubject_Human.size() > 0) {
            aoSubjectHuman = oSubjectHumanDao.findAllByInValues("oSubject", aoSubject_Human);
        }
        aoSubjectHuman.forEach(sh -> sh.setaSubjectHumanRole(null));

        List<Subject> aoSubject_Organ = oSubjectGroupDao
                .findDepartsInCompany(oSubjectGroup.getsID_Group_Activiti())
                .stream()
                .filter(sg -> !sg.getsID_Group_Activiti().equals(oSubjectGroup.getsID_Group_Activiti()))
                .map(SubjectGroup::getoSubject)
                .collect(Collectors.toList());
        if (aoSubject_Organ.size() > 0) {
            aoSubjectOrgan = oSubjectOrganDao.findAllByInValues("oSubject", aoSubject_Organ);
        }

        Map<String, Object> aoChildMap = new HashMap<>();
        aoChildMap.put("aoChildOrgan", aoSubjectOrgan);
        aoChildMap.put("aoChildHuman", aoSubjectHuman);

        oResultMap.put("oParentSubject", oSubjectOrgan);
        oResultMap.put("oChildSubject", aoChildMap);

        return oResultMap;
    }
}
