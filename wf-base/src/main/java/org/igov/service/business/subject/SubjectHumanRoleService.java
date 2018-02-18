package org.igov.service.business.subject;

import com.google.common.base.Optional;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.SubjectHumanDao;
import org.igov.model.subject.SubjectHumanRole;
import org.igov.model.subject.SubjectHumanRoleDao;
import org.igov.service.business.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectHumanRoleService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanRoleService.class);

    @Autowired
    private SubjectHumanDao oSubjectHumanDao;
    @Autowired
    private SubjectHumanRoleDao oSubjectHumanRoleDao;
    @Autowired
    private SubjectHumanService oSubjectHumanService;

    public String setSubjectHumanRole(Long nID_SubjectHuman, Long nID_SubjectHumanRole) {

        try {
            String res = "empty";
            Optional<SubjectHuman> oSubjectHuman = oSubjectHumanDao.findById(nID_SubjectHuman);
            //            System.out.println("oSubjectHuman");
            Optional<SubjectHumanRole> oSubjectHumanRole = oSubjectHumanRoleDao.findById(nID_SubjectHumanRole);
            //            System.out.println("oSubjectHumanRole");
            if (oSubjectHuman != null && oSubjectHumanRole != null) {

                //                System.out.println("SubjectHuman & SubjectHumanRole not null");
                //            String res = oSubjectHumanRole.toString();
                //            for (SubjectHumanRole oSubjectHumanRole : aSubjectHumanRole) {
                //               res = res + " " + oSubjectHumanRole.getName();
                //            }
                //            System.out.println("oSubjectHumanRole.toString(): " + res);
                List<SubjectHumanRole> aCurrentSubjectHumanRole = oSubjectHuman.get().getaSubjectHumanRole();
                if (aCurrentSubjectHumanRole.isEmpty()) {
                    aCurrentSubjectHumanRole.add(oSubjectHumanRole.get());
                } else {
                    for (SubjectHumanRole subjectHumanRole : aCurrentSubjectHumanRole) {
                        boolean bSubjectHumanRole = false;
                        if (subjectHumanRole.getName().equals(oSubjectHumanRole.get().getName())) {
                            bSubjectHumanRole = true;
                        }
                        if (bSubjectHumanRole == false) {
                            aCurrentSubjectHumanRole.add(oSubjectHumanRole.get());
                        }

                    }
                }
                oSubjectHuman.get().setaSubjectHumanRole(aCurrentSubjectHumanRole);
                oSubjectHumanDao.saveOrUpdate(oSubjectHuman.get());
                //                System.out.println("subjectHumanDao.saveOrUpdate(oSubjectHuman.get())");
                //                System.out.println("oSubjectHuman.getaSubjectHumanRole().toString(): " + oSubjectHuman.getaSubjectHumanRole().toString());
                if (!oSubjectHuman.get().getaSubjectHumanRole().isEmpty()) {
                    res = "";
                    for (SubjectHumanRole oSubjectHumanRoleElem : oSubjectHuman.get().getaSubjectHumanRole()) {
                        res = res + " " + oSubjectHumanRoleElem.getName();
                    }
                }
            } else {
                //                System.out.println("SubjectHuman: " + oSubjectHuman.toString());
                //                System.out.println("SubjectHumanRole: " + oSubjectHumanRole.toString());
            }
            System.out.println("res: " + res);
            return res;
        } catch (Exception ex) {
            return CommonUtils.getStringStackTrace(ex);
        }
    }
    
    public Boolean isAdmin(String sLogin) {
        return isAdmin(oSubjectHumanService.getSubjectHuman(sLogin));
    }
    
    public Boolean isAdmin(SubjectHuman subjectHuman) {
        List<SubjectHumanRole> aoSubjectHumanRole = subjectHuman.getaSubjectHumanRole();
        if (aoSubjectHumanRole.isEmpty()) {
            return false;
        }
        String sAdminRole = oSubjectHumanRoleDao.getAdminRole().get().getName();
        return aoSubjectHumanRole.stream()
                       .map(SubjectHumanRole::getName)
                       .anyMatch(sAdminRole::equalsIgnoreCase);
    }
    
}
