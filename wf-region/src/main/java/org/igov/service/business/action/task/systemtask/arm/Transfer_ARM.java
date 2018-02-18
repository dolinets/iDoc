package org.igov.service.business.action.task.systemtask.arm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.igov.service.business.relation.RelationService;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.igov.io.GeneralConfig;
import org.igov.model.arm.DboTkModel;
import org.igov.model.arm.ValidationARM;
import org.igov.model.relation.ObjectGroup;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.action.task.systemtask.mail.Abstract_MailTaskCustom;
import org.igov.service.business.arm.ArmService;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.igov.service.business.subject.SubjectGroupTreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Elena Листинер, предназначен для создания новой заявки в АРМ
 *
 */
@Component("Transfer_ARM")
public class Transfer_ARM extends Abstract_MailTaskCustom implements JavaDelegate {

    private final static Logger LOG = LoggerFactory.getLogger(Transfer_ARM.class);

    private Expression soData;

    @Autowired
    private ArmService armService;

    private Expression sLoginAuthor;

    @Autowired
    private TaskForm taskForm;

    @Autowired
    private RelationService oRelationService;

    @Autowired
    private GeneralConfig generalConfig;
    
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;
    
    @Autowired
    private UsersService oUsersService;
    
    @Autowired
    private EmailProcessSubjectService oEmailProcessSubjectService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        try {
            // получаю из екзекьюшена sID_order
            String sID_order = generalConfig.getOrderId_ByProcess(Long.valueOf(execution.getProcessInstanceId()));
            LOG.info("sID_order: " + sID_order);

            //nID_order без 5-
            /*Long nID_order = generalConfig.getProtectedNumber_ByProcess(execution.getProcessInstanceId());
            LOG.info("nID_order: " + nID_order);*/
            // получаю из екзекьюшена soData
            String soData_Value = this.soData.getExpressionText();
            LOG.info("soData_Value before: " + soData_Value);
            String soData_Value_Result = replaceTags(soData_Value, execution);
            LOG.info("soData_Value after: " + soData_Value_Result);

            // из мапы получаем по ключу значения и укладываем все это в
            // модель и туда же укладываем по ключу Out_number значение nID_order
            DboTkModel dataForTransferToArm = ValidationARM.fillModel(soData_Value_Result);

            Integer maxNum = armService.getMaxValue();
            dataForTransferToArm.setNumber_441(maxNum + 1);
            dataForTransferToArm.setOut_number(sID_order);

            //костыльно сетим в Longterm null, чтоб приоритет заявки был "текущий"
            dataForTransferToArm.setLongterm(null);
            dataForTransferToArm.setDep_number("XXX");
            
            String sCompany = " ";
            
            boolean isAllowToUpdate = true;

            try {
                //название предприятия
                String sAuthorLogin = getStringFromFieldExpression(sLoginAuthor, execution);
                ObjectGroup oObjectGroup = oRelationService.getCompany(sAuthorLogin);                
                if (oObjectGroup == null){
                    //sCompany = oSubjectGroupTreeService.getCompany(sAuthorLogin).getName();
                    isAllowToUpdate = false;
                    //sendEmail(sID_order);
                }else {
                    sCompany = oObjectGroup.getsName();
                }
                dataForTransferToArm.setIndustry(sCompany);
            } catch (Exception e) {
                LOG.info("Exception in company: {}", e);
                throw new RuntimeException("Обрана Вами компанія не може приймати участь в тендері!");
            }

            /**
             * ***************************************добавление подгрупп и кодов****************************************
             */
            String snID_Process_Activiti = execution.getProcessInstanceId();
            String sID_sTable_Goods = "sTable_Goods";
            //id поля наименований подгрупп
            String sID_Name_Relation = "sName_Goods";
            //id поля кода работ
            String sID_Relation = "sID_Private_Source_Goods";
            String sCode = " ", sGroup = " ";

            //подгруппа
            List<String> aGroup = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Goods, sID_Name_Relation);
            LOG.info("aGroup: {}", aGroup);
            sGroup = String.join(" | ", aGroup);
            LOG.info("sGroup: {}", sGroup);
            if (sGroup.length() > 200) {
                sGroup = sGroup.substring(0, 191) + "\"->...\"";
                //throw new RuntimeException("Field \"Group\" too long");
            }

            dataForTransferToArm.setGruppa(sGroup);

            //код подгруппы
            List<String> aCode = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Goods, sID_Relation);
            LOG.info("aCode: {}", aCode);
            sCode = String.join(" | ", aCode);
            LOG.info("sCode: {}", sCode);

            if (sCode.length() > 50) {
                sCode = sCode.substring(0, 41) + "\"->...\"";
                //throw new RuntimeException("Field \"Kod\" too long");
            }

            dataForTransferToArm.setKod(sCode);
            /**
             * ************************************************************************************************************
             */

            Date dDateEZ = dataForTransferToArm.getDataEZ();
            Date dDateIn = dataForTransferToArm.getData_in();
            java.sql.Date sqlDate = new java.sql.Date(ValidationARM.Date_EZ(dDateIn, dDateEZ).getTime());

            dataForTransferToArm.setDataEZ(sqlDate);

            //приложение
            String prilog = ValidationARM.getPrilog(dataForTransferToArm.getPrilog(), oAttachmetService);
            LOG.info("prilog: {}", prilog);
            dataForTransferToArm.setPrilog(ValidationARM.getValidSizePrilog(prilog));
            LOG.info("dataForTransferToArm: {}", dataForTransferToArm);

            //создание записи в арме
            List<DboTkModel> listOfModels = armService.getDboTkByOutNumber(sID_order);
            LOG.info("listOfModels: {}", listOfModels);
            if ((listOfModels == null || listOfModels.isEmpty()) && isAllowToUpdate) {
                LOG.info("Enable to transfer: {}", isAllowToUpdate);
                armService.createDboTk(dataForTransferToArm);
            }
        } catch (Exception e) {
            LOG.info("Exception in Transfer_ARM: {}", e);
            throw e;
        }
    }
    
   /* private void sendEmail(String sID_order){
        List<String> aAdmins = oUsersService.getUsersLoginByGroup("Admin");
        LOG.info("array of Admins: {}", aAdmins);
        LOG.info("number of Admins: {}", aAdmins.size());
        
        List<String> mAdmins = oUsersService.getUsersLoginByGroup("admin");
        LOG.info("array of admins: {}", mAdmins);
        LOG.info("number of admins: {}", mAdmins.size());
    }*/
}
