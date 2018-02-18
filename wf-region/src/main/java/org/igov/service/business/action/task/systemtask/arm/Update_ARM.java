package org.igov.service.business.action.task.systemtask.arm;

import java.util.Date;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.igov.io.GeneralConfig;
import org.igov.model.arm.DboTkModel;
import org.igov.model.arm.ValidationARM;
import org.igov.model.relation.ObjectGroup;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import com.google.common.base.Optional;
import java.math.BigDecimal;
import java.util.Calendar;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.action.task.systemtask.mail.Abstract_MailTaskCustom;
import org.igov.service.business.arm.ArmService;
import org.igov.service.business.relation.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Elena Предназначен для работы с исполнителями и апдейта существующих
 * заявок
 *
 */
@Component("Update_ARM")
public class Update_ARM extends Abstract_MailTaskCustom implements JavaDelegate {

    private final static Logger LOG = LoggerFactory.getLogger(Update_ARM.class);

    private Expression soData;

    @Autowired
    private ArmService armService;

    // имя исполнителя , который выполняет заявку
    private Expression name_isExecute;

    //тип статуса документа 
    private Expression sStatusType;

    //тип ответа исполнителя
    private Expression sStatusTypeExecutor;

    @Autowired
    private GeneralConfig ogeneralConfig;

    @Autowired
    private TaskForm taskForm;

    @Autowired
    private RelationService oRelationService;

    @Autowired
    private SubjectGroupDao SubjectGroupDao;

    @Override
    public void execute(DelegateExecution execution) throws Exception {

        try {
            // получаю из екзекьюшена soData
            String soData_Value = this.soData.getExpressionText();
            LOG.info("soData_Value before: " + soData_Value);

            String soData_Value_Result = replaceTags(soData_Value, execution);
            LOG.info("soData_Value after: " + soData_Value_Result);

            //Достаем имя исполнителя        
            String expert = getStringFromFieldExpression(this.name_isExecute, execution);
            LOG.info("expert: {}", expert);
            //Достаем статус 
            String sTypeStatus = getStringFromFieldExpression(this.sStatusType, execution);
            LOG.info("sStatusType: {}", sTypeStatus);
            //Достаем ответ исполнителя
            /* String sTypeStatusExecutor = getStringFromFieldExpression(this.sStatusType, execution);
            LOG.info("sTypeStatusExecutor: {}", sTypeStatus);*/

            DboTkModel updateDataForUpdateToArm = ValidationARM.fillModel(soData_Value_Result);
            LOG.info("updateDataForUpdateToArm before:{}", updateDataForUpdateToArm);
            
            boolean isAllowToUpdate = true;

            String prilog = ValidationARM.getPrilog(updateDataForUpdateToArm.getPrilog(), oAttachmetService);
            updateDataForUpdateToArm.setPrilog(ValidationARM.getValidSizePrilog(prilog));

            //костыльно сетим в Longterm null, чтоб приоритет заявки был "текущий"
            updateDataForUpdateToArm.setLongterm(null);

            if (sTypeStatus.equals("approve")) {
                LOG.info("sStatusType == approve");

                String sOut_number = "";
                String sZametki = "";
                String sProtocol_Number = "";
                String sCorrectionDoc = "";
                String sAuctionForm = "";                
                String sUslovie = "";
                String sBank = "";
                String sFinans = "";
                String sSmeta = "";
                String sNotes = "";
                String sPriemka = "";
                String sArhiv = "";                
                

                Integer nNumber411 = 0;
                Integer maxNum442 = 0;

                /**
                 *
                 */
                if (updateDataForUpdateToArm.getOut_number() != null) {
                    sOut_number = updateDataForUpdateToArm.getOut_number();
                }
                if (updateDataForUpdateToArm.getZametki() != null) {
                    sZametki = updateDataForUpdateToArm.getZametki();
                }
                if (updateDataForUpdateToArm.getProtocol_Number() != null) {
                    sProtocol_Number = updateDataForUpdateToArm.getProtocol_Number();
                }
                if (updateDataForUpdateToArm.getCorrectionDoc() != null) {
                    sCorrectionDoc = updateDataForUpdateToArm.getCorrectionDoc();
                }
                if (updateDataForUpdateToArm.getAuctionForm() != null) {
                    sAuctionForm = updateDataForUpdateToArm.getAuctionForm();
                }
               /* if (updateDataForUpdateToArm.getIf_oplata() != null) {
                    sIf_oplata = updateDataForUpdateToArm.getIf_oplata();
                }*/
                if (updateDataForUpdateToArm.getUslovie() != null) {
                    sUslovie = updateDataForUpdateToArm.getUslovie();
                }
                if (updateDataForUpdateToArm.getBank() != null) {
                    sBank = updateDataForUpdateToArm.getBank();
                }
                if (updateDataForUpdateToArm.getFinans() != null) {
                    sFinans = updateDataForUpdateToArm.getFinans();
                }
                if (updateDataForUpdateToArm.getSmeta() != null) {
                    sSmeta = updateDataForUpdateToArm.getSmeta();
                }
                if (updateDataForUpdateToArm.getNotes() != null) {
                    sNotes = updateDataForUpdateToArm.getNotes();
                }
                if (updateDataForUpdateToArm.getPriemka() != null) {
                    sPriemka = updateDataForUpdateToArm.getPriemka();
                }
                if (updateDataForUpdateToArm.getArhiv() != null) {
                    sArhiv = updateDataForUpdateToArm.getArhiv();
                }

                /**
                 *
                 */
                String snID_Process_Activiti = execution.getProcessInstanceId();
                String sID_sTable_Winners = "sTableWinners";
                String WinnerFromTable = "sShortName_SubjectOrgan_01";
                String Kod_okpoFromTable = "sID_SubjectOrgan_OKPO_01";
                String PhoneFromTable = "phone";
                String SumzakFromTable = "sStartSum";
                String SummaFromTable = "sFinalSum";
                String UANFromTable = "sCurrency";
                String If_oplataFromTable = "sPayConditions";

                /**
                 *
                 */
                String sID_sTable_Number411 = "sTableNumber441";
                String Number441FromTable = "sDocument";

                DboTkModel dataToUpdate;

                List<String> aNumber441 = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Number411, Number441FromTable);
                LOG.info("aNumber441: {}", aNumber441);
                
                if ((aNumber441.isEmpty() || aNumber441 == null) && updateDataForUpdateToArm.getNumber_441() != null) {                    
                    aNumber441.add(String.valueOf(updateDataForUpdateToArm.getNumber_441()));
                }                

                if (!aNumber441.isEmpty() && aNumber441 != null) {
                    LOG.info("I'm in");
                    for (int i = 0; i < aNumber441.size(); i++) {
                        // вытягиваем обновленную модель
                        nNumber411 = Integer.valueOf(aNumber441.get(i));
                        dataToUpdate = armService.getDboTkByNumber441(nNumber411).get(0);                        
                        
                        dataToUpdate.setSrok(sOut_number);
                        dataToUpdate.setZametki(sZametki);
                        dataToUpdate.setProtocol_Number(sProtocol_Number);
                        dataToUpdate.setCorrectionDoc(sCorrectionDoc);
                        dataToUpdate.setAuctionForm(sAuctionForm);
                        //dataToUpdate.setIf_oplata(sIf_oplata);
                        dataToUpdate.setUslovie(sUslovie);
                        dataToUpdate.setBank(sBank);
                        dataToUpdate.setFinans(sFinans);
                        dataToUpdate.setSmeta(sSmeta);
                        dataToUpdate.setNotes(sNotes);
                        dataToUpdate.setPriemka(sPriemka);
                        dataToUpdate.setArhiv(sArhiv);
                        dataToUpdate.setState("Подписана");
                        

                        Date dDate = dataToUpdate.getDataBB();
                        LOG.info("dDate: {}", dDate);

                        if (dDate == null) {
                            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
                            dataToUpdate.setDataBB(sqlDate);
                            dataToUpdate.setData_out_raz(sqlDate);
                        } else {
                            dataToUpdate.setDataBB(dDate);
                            dataToUpdate.setData_out_raz(dDate);
                        }

                        dataToUpdate.setUpdateData(ValidationARM.UpdateDate());

                        List<String> aWinner = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, WinnerFromTable);                       
                        List<String> aKod_okpo = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, Kod_okpoFromTable);                        
                        List<String> aPhone = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, PhoneFromTable);                        
                        List<String> aSumzak = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, SumzakFromTable);                        
                        List<String> aSumma = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, SummaFromTable);                       
                        List<String> aUAN = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, UANFromTable);                        
                        List<String> aIf_oplata = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Winners, If_oplataFromTable);                        

                        for (int j = 0; j < aWinner.size(); j++) {
                            dataToUpdate.setWinner(aWinner.get(j));
                            dataToUpdate.setKod_okpo(aKod_okpo.get(j));
                            dataToUpdate.setPhone(aPhone.get(j));                                                        
                            dataToUpdate.setSumzak(BigDecimal.valueOf(Double.parseDouble(aSumzak.get(j) == null ? "0" : aSumzak.get(j))));                            
                            dataToUpdate.setSumma(BigDecimal.valueOf(Double.parseDouble(aSumma.get(j) == null ? "0" : aSumma.get(j))));
                            dataToUpdate.setuAN(aUAN.get(j));
                            dataToUpdate.setuAN(aIf_oplata.get(j));

                            maxNum442 = armService.getMaxValue442() + 1;
                            dataToUpdate.setNumber_442(maxNum442);

                            if (j == 0) {
                                armService.updateDboTkByAnswer(dataToUpdate);
                            } else {                                
                                dataToUpdate.setNumber_441(nNumber411);
                                armService.createDboTk(dataToUpdate);
                            }
                        }
                    }
                }
            } else if (sTypeStatus.equals("inwork") || sTypeStatus.equals("refuse")) {

                /**
                 * ************************************проверяем входящий номер
                 * документа*************************************
                 */
                String sOutNumber = updateDataForUpdateToArm.getOut_number();
                LOG.info("sOutNumber: {}", sOutNumber);

                List<DboTkModel> listOfModels;

                if (sOutNumber.length() < 9) {
                    //Long nID_order = ogeneralConfig.getProtectedNumber_ByProcess(sOutNumber);                       
                    sOutNumber = generalConfig.getOrderId_ByProcess(Long.valueOf(execution.getProcessInstanceId()));
                    updateDataForUpdateToArm.setOut_number(sOutNumber);
                }

                listOfModels = armService.getDboTkByOutNumber(sOutNumber);
                LOG.info("listOfModels: {}", listOfModels);
                /**
                 * ************************************************************************************************************
                 */

                if (listOfModels != null && !listOfModels.isEmpty()) {
                    LOG.info("transfer exist");
                    updateDataForUpdateToArm.setNumber_441(listOfModels.get(0).getNumber_441());

                    if (sTypeStatus.equals("inwork")) {
                        LOG.info("sStatusType == inwork");
                        // ветка - когда назначаются исполнители
                        if (expert == null) {
                            if (updateDataForUpdateToArm.getExpert() != null) {
                                List<String> asExecutorsFromsoData = ValidationARM.getAsExecutors(updateDataForUpdateToArm.getExpert(), oAttachmetService, "sName_isExecute");// json с ключом из монги																		
                                LOG.info("asExecutorsFromsoData = {}", asExecutorsFromsoData);

                                String snID_Process_Activiti = execution.getProcessInstanceId();
                                String sID_sTable_Expert = "sTableExecutor";

                                //id поле исполнителя
                                String sID_Name_Expert = "sLogin_isExecute";
                                List<String> aExpert = taskForm.getValuesFromTableField(snID_Process_Activiti, sID_sTable_Expert, sID_Name_Expert);

                                LOG.info("aExpert = {}", aExpert);

                                Optional<SubjectGroup> oSubjectGroup;
                                //oObjectGroup - для отделения, aObjectGroup - для фамилии
                                ObjectGroup oObjectGroup, aObjectGroup;

                                if (listOfModels != null && !listOfModels.isEmpty()) {
                                    if (asExecutorsFromsoData != null && !asExecutorsFromsoData.isEmpty()) {
                                        //updateDataForUpdateToArm.setExpert(asExecutorsFromsoData.get(0));
                                        LOG.info("updateDataForUpdateToArm before first expert: {}", updateDataForUpdateToArm);

                                        Long nId = 0l;
                                        String sName = "";

                                        try {
                                            //Department
                                            oObjectGroup = oRelationService.getObjectGroupParentOrNull(aExpert.get(0));                                            
                                            if (oObjectGroup == null) {
                                                //sName = oSubjectGroup.get().getName();
                                                isAllowToUpdate = false;
                                            } else {
                                                updateDataForUpdateToArm.setDep_number(oObjectGroup.getsName());
                                            }
                                            //updateDataForUpdateToArm.setDep_number("???");

                                            //Фамилия
                                            oSubjectGroup = SubjectGroupDao.findBy("sID_Group_Activiti", aExpert.get(0));
                                            nId = oSubjectGroup.get().getoSubject().getId();

                                            aObjectGroup = oRelationService.getObjectGroupBySubject_Source(String.valueOf(nId));
                                            if (aObjectGroup == null) {
                                                //sName = oSubjectGroup.get().getName();
                                                isAllowToUpdate = false;
                                            } else {
                                                sName = aObjectGroup.getsName();
                                            }

                                            updateDataForUpdateToArm.setExpert(sName);
                                        } catch (Exception e) {
                                            LOG.info("Exception in getting from ObjectGroup: {}", e);
                                            throw new RuntimeException("Обрана Вами людина не може приймати участь в тендері!");
                                        }

                                        //Date EZ
                                        Date dDateIn = updateDataForUpdateToArm.getData_in();
                                        Date dDateEZ = updateDataForUpdateToArm.getDataEZ();
                                        java.sql.Date sqlDate = new java.sql.Date(ValidationARM.Date_EZ(dDateIn, dDateEZ).getTime());
                                        updateDataForUpdateToArm.setDataEZ(sqlDate);

                                        updateDataForUpdateToArm.setUpdateData(ValidationARM.UpdateDate());

                                        if (isAllowToUpdate){
                                            armService.updateDboTk(updateDataForUpdateToArm);
                                        }                                       

                                        // если в листе не одно значение - для каждого исполнителя сетим
                                        if (asExecutorsFromsoData.size() > 1) {

                                            // вытягиваем обновленную модель
                                            updateDataForUpdateToArm = armService.getDboTkByOutNumber(sOutNumber).get(0);                                           

                                            for (int i = 1; i < asExecutorsFromsoData.size(); i++) {
                                                isAllowToUpdate = true;
                                                //updateDataForUpdateToArm.setExpert(asExecutorsFromsoData.get(i));
                                                Integer maxNum441 = armService.getMaxValue();
                                                updateDataForUpdateToArm.setNumber_441(maxNum441 + 1);

                                                try {
                                                    //Department
                                                    oObjectGroup = oRelationService.getObjectGroupParentOrNull(aExpert.get(i));
                                                    if (oObjectGroup == null) {
                                                        //sName = oSubjectGroup.get().getName();
                                                        isAllowToUpdate = false;
                                                    } else {
                                                        updateDataForUpdateToArm.setDep_number(oObjectGroup.getsName());
                                                    }                                                    
                                                    //updateDataForUpdateToArm.setDep_number("???");

                                                    //Фамилия
                                                    oSubjectGroup = SubjectGroupDao.findBy("sID_Group_Activiti", aExpert.get(i));
                                                    nId = oSubjectGroup.get().getoSubject().getId();

                                                    aObjectGroup = oRelationService.getObjectGroupBySubject_Source(String.valueOf(nId));
                                                    if (aObjectGroup == null) {
                                                        //sName = oSubjectGroup.get().getName();
                                                        isAllowToUpdate = false;
                                                    } else {
                                                        sName = aObjectGroup.getsName();
                                                    }

                                                    updateDataForUpdateToArm.setExpert(sName);

                                                } catch (Exception e) {
                                                    LOG.info("Exception in getting from ObjectGroup: {}", e);
                                                    throw new RuntimeException("Обрана Вами людина не може приймати участь в тендері!");
                                                }

                                                //Date EZ
                                                dDateIn = updateDataForUpdateToArm.getData_in();
                                                dDateEZ = updateDataForUpdateToArm.getDataEZ();
                                                sqlDate = new java.sql.Date(ValidationARM.Date_EZ(dDateIn, dDateEZ).getTime());
                                                updateDataForUpdateToArm.setDataEZ(sqlDate);

                                                updateDataForUpdateToArm.setUpdateData(ValidationARM.UpdateDate());

                                                LOG.info("updateDataForUpdateToArm before next expert: = {}", updateDataForUpdateToArm);
                                                if (isAllowToUpdate) {
                                                    armService.createDboTk(updateDataForUpdateToArm);
                                                }                                                
                                            }
                                        }
                                    } else {
                                        LOG.info("Executors are empty");
                                    }

                                } else {
                                    LOG.info("Model include sID_order " + updateDataForUpdateToArm.getOut_number()
                                            + "not found in ARM");
                                }
                            }
                        } else {
                            // ветка, когда исполнители уже есть и они отрабатывают свое задание
                            /*for(DboTkModel model: listOfModels){
                            if(dataWithExecutorForTransferToArm.getExpert().equals(expert)){*/
                            LOG.info("expert != null");
                            /*Integer maxNum442 = armService.getMaxValue442();
                            updateDataForUpdateToArm.setExpert(expert);                
                            updateDataForUpdateToArm.setNumber_442(maxNum442+1);
                            armService.updateDboTkByExpert(updateDataForUpdateToArm);*/
                        }
                    } else if (sTypeStatus.equals("refuse")) {
                        LOG.info("sStatusType == refuse");
                        updateDataForUpdateToArm.setNumber_442(0);
                        updateDataForUpdateToArm.setExpert(null);

                        //проверка на наличие фактической даты закрытия, если ее нет, генерируем текущую
                        Date dDate = updateDataForUpdateToArm.getDataBB();
                        LOG.info("dDate: {}", dDate);

                        if (dDate == null) {
                            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
                            updateDataForUpdateToArm.setDataBB(sqlDate);
                            updateDataForUpdateToArm.setData_out_raz(sqlDate);
                        } else {
                            updateDataForUpdateToArm.setDataBB(dDate);
                            updateDataForUpdateToArm.setData_out_raz(dDate);
                        }

                        LOG.info("Data_out_raz: {}", updateDataForUpdateToArm.getData_out_raz());

                        updateDataForUpdateToArm.setUpdateData(ValidationARM.UpdateDate());

                        armService.updateDboTk(updateDataForUpdateToArm);
                    }
                }
            } else {
                LOG.info("sStatusType == unfefined");
                //throw new RuntimeException("sStatusType == unfefined");
            }
        } catch (Exception e) {
            LOG.info("Exception in Update_ARM: {}", e);
            throw e;
        }
    }
}
