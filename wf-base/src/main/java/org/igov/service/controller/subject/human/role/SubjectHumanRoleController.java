package org.igov.service.controller.subject.human.role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.service.business.subject.SubjectHumanRoleService;
import org.igov.service.business.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

@Controller
@Api(value = "Правовые группы сотрудников", tags = {"SubjectHumanRole"})
@RequestMapping(value = "/subject")
public class SubjectHumanRoleController {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanRoleController.class);

    @Autowired
    private SubjectHumanRoleService oSubjectHumanRoleService;

    @ApiOperation(value = "Предоставление сабджекту роли", response = String.class, notes = "Предоставление сабджекту роли")
    @RequestMapping(value = "/setSubjectHumanRole", method = RequestMethod.GET)
    @ResponseBody
    public String setSubjectHumanRole(
            @ApiParam(required = true, value = "nID_SubjectHuman") @RequestParam Long nID_SubjectHuman,
            @ApiParam(required = true, value = "nID_SubjectHumanRole") @RequestParam Long nID_SubjectHumanRole) {
        return oSubjectHumanRoleService.setSubjectHumanRole(nID_SubjectHuman, nID_SubjectHumanRole);
    }
    
    @ApiOperation(value = "Проверка, является ли текущий логин администратором",
            response = Boolean.class,
            notes = "##### Пример:"
                + "alpha.test.region.igov.org.ua/wf/service/subject/isAdmin")
    @ApiResponse(code = 200, message = "true,false")
    @RequestMapping(value = "/isAdmin", method = RequestMethod.GET)
    @ResponseBody
    public Boolean isAdmin(@RequestParam(required = false) String sID_Group_Activiti, HttpSession session) {
        boolean hasSessionLogin = hasAttribute(session, "sLogin");
        boolean hasLogin = CommonUtils.bIs(sID_Group_Activiti);
        if (!hasSessionLogin && !hasLogin) {
            throw new RuntimeException("Session has no currently logged-in user. Specify value explicitly");
        }
        
        if (hasSessionLogin) {
            sID_Group_Activiti = (String) session.getAttribute("sLogin");
            LOG.debug("Using session 'sLogin' = {}", sID_Group_Activiti);
        }
    
        return oSubjectHumanRoleService.isAdmin(sID_Group_Activiti);
    }
    
    private boolean hasAttribute(HttpSession session, String sAttributeName) {
        Enumeration<String> asAttributeName = session.getAttributeNames();
        String s;
        while (asAttributeName.hasMoreElements()) {
            s = asAttributeName.nextElement();
            if (sAttributeName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

}
