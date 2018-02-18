package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javassist.NotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.igov.service.business.access.AccessService;

import org.igov.service.business.process.ActionProcessServcie;
import org.igov.service.business.subject.SubjectRightBPService;
import org.igov.service.business.subject.SubjectRightBPVO;
import org.igov.service.exception.CommonServiceException;
import org.igov.service.exception.ExceptionMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author idenysenko
 */
@Controller
@Api(tags = {"ProcessCommonController — организация процессов"})
@RequestMapping(value = "/process")
public class ProcessCommonController implements ExceptionMessage{

    private static final Logger LOG = LoggerFactory.getLogger(ProcessCommonController.class);

    @Autowired
    private ActionProcessServcie oActionProcessServcie;
    @Autowired
    private SubjectRightBPService oSubjectRightBPService;
    @Autowired
    private AccessService oAccessService;

    @ApiOperation(value = "Получение ProcessDefinition'ов которые sLogin может создавать, или в которых участник",
            notes = "На вход так же принимает массив типов вкладок. Возможные значения для saFilterStatus: Task,"
                    + "Document, All"
                    + "\n Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/process/getAllBpForLogin?sLogin=OGOK_010691KVS&saFilterStatus=DocumentOpenedUnassignedUnprocessed \n")
    @RequestMapping(value = "/getAllBpForLogin", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getAllBpForLogin(
            @ApiParam(value = "Персонализированная группа", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,
            @ApiParam(value = "Персонализированная группа - референт", required = false) @RequestParam(value = "sLoginReferent", required = false) String sLoginReferent,
            @ApiParam(value = "Тип закладки", required = false) @RequestParam(value = "saFilterStatus", required = false) String saFilterStatus,
            @ApiParam(value = "Включительно с правами на создание", required = false) @RequestParam(value = "bIncludeRights", required = false, defaultValue = "true") Boolean bIncludeRights,
            @ApiParam(value = "Фильтр (true - доки/ false - таски/ null - все)", required = false) @RequestParam(value = "bFilterDoc", required = false) Boolean bFilterDoc,
            HttpServletRequest oRequest) throws NotFoundException {

        LOG.info("getAllBpForLogin started...");
        //sLogin = oAccessService.getSessionLogin(sLogin, sLoginReferent, oRequest);
        LOG.info("getAllBpForLogin sLogin is {}", sLogin);

        List<Map<String, String>> mAllBPs = new ArrayList<>();
        //процессы в которых учавствует sLogin
        mAllBPs.addAll(oActionProcessServcie.getBPsForParticipant(sLogin, saFilterStatus));
        LOG.info("mAllBPs Participant {}", mAllBPs);
        if (bIncludeRights) {
            //процессы которые sLogin может стартовать
            List<SubjectRightBPVO> aoSubjectRightBPVO = oSubjectRightBPService.getBPs_ForReferent_bysLogin(sLogin);
            for (SubjectRightBPVO oSubjectRightBPVO : aoSubjectRightBPVO) {
                Map<String, String> mBpID_Name = new HashMap<>();
                mBpID_Name.put("sID", oSubjectRightBPVO.getoSubjectRightBP().getsID_BP());
                mBpID_Name.put("sName", oSubjectRightBPVO.getsName_BP());
                mAllBPs.add(mBpID_Name);
            }
        }
        //делим на документы и не документы
        if (bFilterDoc != null) {
            if (bFilterDoc) {
                mAllBPs = mAllBPs.stream()
                        .filter(map -> map.get("sID").startsWith("_doc"))
                        .collect(Collectors.toList());
            } else {
                mAllBPs = mAllBPs.stream()
                        .filter(map -> !map.get("sID").startsWith("_doc"))
                        .collect(Collectors.toList());
            }
        }
        LOG.info("mAllBPs Referent {}", mAllBPs);

        return mAllBPs;
    }

    @ApiOperation(value = "Получение полей процесса\n", notes = "Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/getBPFields?sProcessDefinitionKey=_doc_btsol_vertical_sz \n")
    @RequestMapping(value = "/getBPFields", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getBPFields(
            @ApiParam(value = "Ид процесса без версионности", required = true) @RequestParam(value = "sProcessDefinitionKey", required = true) String sProcessDefinitionKey,
            @ApiParam(value = "Флаг все поля/доступные для поиска", required = false) @RequestParam(value = "bAllTypeFields", required = false) Boolean bAllTypeFields
    ) throws CommonServiceException {

        return oActionProcessServcie.getBPFields(sProcessDefinitionKey, bAllTypeFields);
    }
}
