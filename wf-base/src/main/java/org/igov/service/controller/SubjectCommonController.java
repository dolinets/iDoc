package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.model.subject.wrapper.SubjectContactWrapper;
import org.igov.service.business.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Api(tags = {"SubjectCommonController — Организационная иерархия"})
@RequestMapping(value = "/subject")
public class SubjectCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectCommonController.class);

    @Autowired
    private SubjectService oSubjectService;
    @Autowired
    private SubjectContactDao oSubjectContactDao;
    @Autowired
    private SubjectHumanDao oSubjectHumanDao;
    
    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectHumanDao subjectHumanDao;
    
    @Autowired
    private SubjectOrganDao subjectOrganDao;

    @ApiOperation(value = "Получение сабджекта по логину", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectByLoginAccount?sLogin=kermit \n"
            + "Ответ: HTTP STATUS 200\n\n"
    )
    @RequestMapping(value = "/getSubjectByLoginAccount", method = RequestMethod.GET)
    @ResponseBody
    public Subject getSubjectByLoginAccount(@ApiParam(value = "логин", required = true) @RequestParam(value = "sLogin") String sLogin)
            throws Exception {
        return oSubjectService.getSubjectByLoginAccount(sLogin);
    }
    
    @ApiOperation(value = "Получение e-mail по логину", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getEmailByLogin?sLogin=MJU_Dnipro_KrRigArea_DVS \n"
    )
    @RequestMapping(value = "/getEmailByLogin", method = RequestMethod.GET)
    @ResponseBody
    public String getEmailByLogin(@ApiParam(value = "логин", required = true) @RequestParam(value = "sLogin") String sLogin)
            throws Exception {
        return oSubjectService.getEmailByLogin(sLogin);
    }
    
    @ApiOperation(value = "Получение контактов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectContacts?snID_Process_Activiti=34610001&sID_Field=sTableViewed&sSubjectType=Human&nID_SubjectContactType=1 \n"
    )
    @RequestMapping(value = "/getSubjectContacts", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getSubjectContacts(
        @ApiParam(value = "ИД процесса", required = true) @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
        @ApiParam(value = "ид поля - из которого мы вытянем или тейблу, или селек, или стрингу", required = true) @RequestParam(value = "sID_Field", required = true) String sID_Field,
        @ApiParam(value = "ид поля таблицы", required = true) @RequestParam(value = "sID_FieldTable", required = true) String sID_FieldTable,
        @ApiParam(value = "фильтр по типу Subject'a", required = false) @RequestParam(value = "sSubjectType", required = false) String sSubjectType,
        @ApiParam(value = "тип контакта", required = true) @RequestParam(value = "sSubjectContactType", required = true) String sSubjectContactType
    ) throws Exception {
            
        return oSubjectService.getSubjectContacts(snID_Process_Activiti, sID_Field, sID_FieldTable, sSubjectType, sSubjectContactType);
    }
    
    @ApiOperation(value = "Получение контактов всех иерархии по заданным параметрам", responseContainer = "List", response = String.class)
    @ApiResponse(code = 200, message = "Значения контактов")
    @RequestMapping(value = "/getSubjectContactValues", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getSubjectContactValues(
            @ApiParam(value = "Строковый ID сотрудника, отдела, компании", required = true) @RequestParam String sID_Group_Activiti,
            @ApiParam(value = "Тип контакта", required = true) @RequestParam String sSubjectContactType,
            @ApiParam(value = "Тип ", allowableValues = "Human,Organ,All", defaultValue = "All")
            @RequestParam(required = false, defaultValue = "All") String sSubjectType,
            @ApiParam(value = "Глубина подчинения выборки, '-1' = вся иерархия", defaultValue = "1")
            @RequestParam(required = false, defaultValue = "1") Integer nLevel
    ) {
        return oSubjectService.getSubjectContacts(sID_Group_Activiti, sSubjectType, sSubjectContactType, nLevel);
    }

    @ApiOperation(value = "Получение контактов сотрудника или организации", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectContact?sID_Group_Activiti=NSTU_13917_KTM"
    )
    @ApiResponse(code = 200, responseContainer = "List", message = "[\n"
            + "  {\n"
            + "    \"subjectContactType\": {\n"
            + "      \"sName_EN\": \"Phone\",\n"
            + "      \"sName_UA\": \"Телефон\",\n"
            + "      \"sName_RU\": \"Телефон\",\n"
            + "      \"nID\": 0\n"
            + "    },\n"
            + "    \"sValue\": \"380 (63) 120-76-43\",\n"
            + "    \"sDate\": null,\n"
            + "    \"nID\": 578\n"
            + "  }\n"
            + "]")
    @RequestMapping("/getSubjectContact")
    @ResponseBody
    public List<SubjectContact> getSubjectContact(
            @ApiParam(value = "Строковый ID сотрудника или организации", required = true) @RequestParam String sID_Group_Activiti) {
        return oSubjectService.getaSubjectContact(sID_Group_Activiti);
    }

    @ApiOperation(value = "/setSubjectContact",
            consumes = "application/json;charset=UTF-8",
            notes = "Добавление контактов сотрудника или организации")
    @RequestMapping(value = "/setSubjectContact", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void setSubjectContact(
            @ApiParam(required = true, value = "строковый ID сотрудника или организации") @RequestParam String sID_Group_Activiti,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam(required = true, value = "массив контактов в формате [{\"sType\":\"\",\"sValue\":\"\"},{...}]") @RequestBody List<SubjectContactWrapper> aContactWrapper
    ) {
        oSubjectService.setSubjectContacts(sID_Group_Activiti, aContactWrapper, isSync);
    }

    @ApiOperation(value = "/deleteSubjectContact",
            consumes = "application/json;charset=UTF-8",
            notes = "Удаление контактов сотрудника или организации. В ответ: список значений ")
    @RequestMapping(value = "/deleteSubjectContact", method = RequestMethod.POST)
    @ResponseBody
    public List<String> deleteSubjectContact(
            @ApiParam(required = true, value = "строковый ID сотрудника или организации") @RequestParam String sID_Group_Activiti,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam(required = true, value = "массив контактов в формате [{\"sType\":\"\",\"sValue\":\"\"},{...}]") @RequestBody List<SubjectContactWrapper> aContactWrapper
    ) {
        return oSubjectService.deleteSubjectContacts(sID_Group_Activiti, aContactWrapper, isSync);
    }
    
    /**
     * получение субъекта, если таков найден, или добавление субъекта в
     * противном случае
     */
    @ApiOperation(value = "Получение субъекта", notes = "##### SubjectController - Субъекты  и смежные сущности. Получение субъекта #####\n\n"
            + "HTTP Context: http://server:port/wf/service/subject/syncSubject\n\n\n"
            + "Если субъект найден, или добавление субъекта в противном случае\n\n"
            + "От клиента ожидается ОДИН и только ОДИН параметр из нижеперечисленных\n\n"
            + "- nID - ИД-номер субъекта\n"
            + "- sINN - строка-ИНН (субъект - человек)\n"
            + "- sOKPO - строка-ОКПО (субъек - организация)\n"
            + "- nID_Subject - ID авторизированого субъекта (добавляется в запрос автоматически после аутентификации пользователя)\n\n\n"
            + "Примеры:\n\n"
            + "https://alpha.test.igov.org.ua/wf/service/subject/syncSubject?sINN=34125265377\n\n"
            + "https://alpha.test.igov.org.ua/wf/service/subject/syncSubject?sOKPO=123\n\n"
            + "https://alpha.test.igov.org.ua/wf/service/subject/syncSubject?nID=1\n\n"
            + "Response\n"
            + "\n```json\n"
            + "{\n"
            + "    \"nID\":150,\n"
            + "    \"sID\":\"34125265377\",\n"
            + "    \"sLabel\":null,\n"
            + "    \"sLabelShort\":null\n"
            + "}\n"
            + "\n```\n")
    @RequestMapping(value = "/syncSubject", method = RequestMethod.GET, headers = {"Accept=application/json"})
    public @ResponseBody
    Subject syncSubject(
            @ApiParam(value = "ИД-номер субъекта", required = false) @RequestParam(value = "nID", required = false) Long nID,
            @ApiParam(value = "строка-ИНН (субъект - человек)", required = false) @RequestParam(required = false) String sINN,
            @ApiParam(value = "номер-ИД типа идентификации субьекта-человека (по умолчанию 0)", required = false)
            @RequestParam(required = false, defaultValue = "0") int nID_SubjectHumanIdType,
            @ApiParam(value = "строка-код, параметр-идентификатора субьекта (без префикса типа)", required = false)
            @RequestParam(required = false) String sCode_Subject,
            @ApiParam(value = "строка-ОКПО (субъек - организация)", required = false) @RequestParam(required = false) String sOKPO,
            HttpServletResponse httpResponse,
            HttpServletRequest oRequest) {

        LOG.info("--- syncSubject ---");
        LOG.info("syncSubject started...");
        HttpSession oSession = oRequest.getSession(true);
        oSession.setAttribute("sINN", sINN);
        
        LOG.info("syncSubject sINN is {}", oSession.getAttribute("sINN"));
        LOG.info("syncSubject oSession ID is {}", oSession.getId());
        Subject subject;
        if (nID != null) {
            subject = subjectDao.getSubject(nID);
        } else if (StringUtils.isNotEmpty(sINN)) {
            SubjectHuman oSubjectHuman = subjectHumanDao.getSubjectHuman(sINN);
            if (oSubjectHuman == null) {
                oSubjectHuman = subjectHumanDao.saveSubjectHuman(sINN);
            }
            subject = oSubjectHuman.getoSubject();
        } else if (StringUtils.isNotEmpty(sCode_Subject)) {
            SubjectHumanIdType subjectHumanIdType = SubjectHumanIdType.fromId(nID_SubjectHumanIdType);

            SubjectHuman oSubjectHuman = subjectHumanDao.getSubjectHuman(subjectHumanIdType, sCode_Subject);
            if (oSubjectHuman == null) {
                oSubjectHuman = subjectHumanDao.saveSubjectHuman(subjectHumanIdType, sCode_Subject);
            }
            subject = oSubjectHuman.getoSubject();
        } else if (StringUtils.isNotEmpty(sOKPO)) {
            SubjectOrgan subjectOrgan = subjectOrganDao.getSubjectOrgan(sOKPO);
            if (subjectOrgan == null) {
                subjectOrgan = subjectOrganDao.setSubjectOrgan(sOKPO);
            }
            subject = subjectOrgan.getoSubject();
        } else {
            throw new ActivitiObjectNotFoundException(
                    "RequestParam not found! You should add nID or  sINN or sINN, "
                    + "or (nID_SubjectHumanIdType + sCode_Subject) or sOKPO param!", Subject.class);
        }
        if (subject == null) {
            throw new ActivitiObjectNotFoundException(
                    String.format("Subject not found and not created! nID = %s sINN = %s, nID_SubjectHumanIdType = %s, "
                            + "sCode_Subject = %s sOKPO = %s", nID, sINN, nID_SubjectHumanIdType, sCode_Subject, sOKPO),
                    Subject.class);
        }
        httpResponse.setHeader("Content-Type", "application/json;charset=UTF-8");
        return subject;
    }
    
    @ApiOperation(value = "Удалить контакты по ид контакта или ид сабджекта",
            consumes = "application/json;charset=UTF-8",
            notes = "Пример: https://alpha.test.region.igov.org.ua/wf/service/subject/deleteSubjectContactBy?nID_Subject=641")
    @RequestMapping(value = "/deleteSubjectContactBy", method = RequestMethod.GET)
    @ResponseBody
    public void deleteSubjectContact(
            @ApiParam("ид контакта") @RequestParam(required = false) Long nID_SubjectContact,
            @ApiParam("ид сабджекта") @RequestParam(required = false) Long nID_Subject
    ) {
        List<SubjectContact> aoSubjectContact = new ArrayList<>();
        if (nID_SubjectContact != null) {
            aoSubjectContact.add(oSubjectContactDao.findById(nID_SubjectContact).orNull());
        }
        if (nID_Subject != null){
            aoSubjectContact.addAll(oSubjectContactDao.findAllBy("subject.id", nID_Subject));
        }
        aoSubjectContact = aoSubjectContact.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!aoSubjectContact.isEmpty()) {
            //находим SubjectHuman, которые ссылаются на aoSubjectContact и обнуляем ссылку
            List<SubjectHuman> aoSubjectHuman = oSubjectHumanDao.findAllByInValues("defaultEmail", aoSubjectContact);
            aoSubjectHuman.forEach(oSubjectHuman -> oSubjectHuman.setDefaultEmail(null));
            oSubjectHumanDao.saveOrUpdate(aoSubjectHuman);
            aoSubjectHuman = oSubjectHumanDao.findAllByInValues("defaultPhone", aoSubjectContact);
            aoSubjectHuman.forEach(oSubjectHuman -> oSubjectHuman.setDefaultPhone(null));
            oSubjectHumanDao.saveOrUpdate(aoSubjectHuman);
            //после того как обнулили ссылки удаляем контакты
            oSubjectContactDao.delete(aoSubjectContact);
        }
    }
}
