package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Kovylin
 */
@Controller
@Api(tags = {"ServerEntitySyncController — Загрузка и синхронизация персонала"})
@RequestMapping(value = "/sync/entity")
@Transactional(rollbackFor = Exception.class)
public class ServerEntitySyncController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ServerEntitySyncController.class);
    
    @Autowired
    private ServerEntitySyncService oServerEntitySyncService;
    
    @ApiOperation(value = "Запуск синхронизации персонала для всех серверов из перечня Server.csv"
            + "Пример запроса - https://beta.test.idoc.com.ua/wf/service/sync/entity/serverEntitySyncService")
    @RequestMapping(value = {"/serverEntitySyncService"}, method = RequestMethod.GET)
    public @ResponseBody void serverEntitySyncService(HttpServletRequest oRequest) throws Exception {
        oServerEntitySyncService.runServerEntitySync(null, null);
    }
    
    @ApiOperation(value = "Очищает таблицу с перечнем сущностей для синхронизации (полное удаление очереди и старых записей). "
            + "Применять ТОЛЬКО для тестовых серверов или с разрешения рук-ва")
    @RequestMapping(value = {"/removeAllServerEntitySync"}, method = RequestMethod.GET)
    public @ResponseBody void removeAllServerEntitySync(HttpServletRequest oRequest) throws Exception {
        oServerEntitySyncService.removeAllServerEntitySync();
    }
    
    @ApiOperation(value = "Полная очистка пула таблиц Relation", notes = "Сервис делает trancate для таблиц Relation: "
            + "\"Relation_ObjectGroup\", \"ObjectGroup\", \"Relation\". \n" 
            + "Применять ТОЛЬКО для тестовых серверов или с разрешения рук-ва")
    @RequestMapping(value = {"/cleanRelationTables"}, method = RequestMethod.GET)
    public @ResponseBody void cleanRelationTables(HttpServletRequest oRequest) throws Exception {
        oServerEntitySyncService.cleanTables("RelationTables");
    }
    
    @ApiOperation(value = "Бекап таблиц Relation", notes = "Сервис возвращает бекап-архив для таблиц Relation: "
            + "\"Relation_ObjectGroup\", \"ObjectGroup\", \"Relation\". ")
    @RequestMapping(value = {"/downloadRelation"}, method = RequestMethod.GET)
    public @ResponseBody byte[] downloadRelation(HttpServletResponse httpResponse) throws Exception {
        
        httpResponse.setHeader("Content-disposition", "attachment; filename="
                + "relation_backup.zip");
        httpResponse.setHeader("Content-Type", "application/zip");       
        
        return oServerEntitySyncService.backupTables("RelationTables");
    }
    
    @ApiOperation(value = "Обновление таблиц Relation из архива с csv", notes =
            "Сontent-type запроса - убрать. В тело запроса поступает архив из восьми csv-файлов, соот-х таблицам персонала: "
                    + "\"Relation_ObjectGroup\", \"ObjectGroup\", \"Relation\", данные из которых полностью перезатрут собой соотв-е таблицы. "
                    + "В ответ сервис отдает архив с csv-дампами таблиц до изенений - на случай необходимости восстановления. Чтобы скачать его в "
                    + "виде файла - из постмана спользуйте кнопку Send and Download вместо Send. "
                    + "В случае ошибки - отдается бекап, http-статус ответа - 500, дамп автоматически перезаливается в таблицы. "
                    + "Пример запроса - https://beta.test.idoc.com.ua/wf/service/sync/entity/reloadRelation")
    @RequestMapping(value = {"/reloadRelation"}, method = RequestMethod.POST)
    public @ResponseBody byte[] reloadRelation(@RequestParam(value = "file") MultipartFile oMultipartFile,
            HttpServletRequest oRequest, HttpServletResponse httpResponse) throws Exception {
        
        httpResponse.setHeader("Content-disposition", "attachment; filename="
                + "relation_backup.zip");
        httpResponse.setHeader("Content-Type", "application/zip");       
        
        return oServerEntitySyncService.reloadTables(oMultipartFile.getBytes(), false, httpResponse, "RelationTables");
    }
    
    @ApiOperation(value = "Полная очистка таблиц персонала", notes = "Сервис делает trancate для таблиц персонала "
            + "\"Subject\", \"SubjectContact\", \"SubjectHuman\", \"SubjectOrgan\", \"SubjectHumanPositionCustom\", \n" 
            + "\"SubjectGroup\", \"SubjectGroupTree\", \"SubjectAccount\". "
            + "Применять ТОЛЬКО для тестовых серверов или с разрешения рук-ва")
    @RequestMapping(value = {"/cleanTables"}, method = RequestMethod.GET)
    public @ResponseBody void cleanTables(HttpServletRequest oRequest) throws Exception {
        oServerEntitySyncService.cleanTables("StaffTables");
    }
    
    @ApiOperation(value = "Бекап таблиц персонала", notes = "Сервис возвращает бекап-архив для таблиц персонала "
            + "\"Subject\", \"SubjectContact\", \"SubjectHuman\", \"SubjectOrgan\", \"SubjectHumanPositionCustom\", \n" 
            + "\"SubjectGroup\", \"SubjectGroupTree\", \"SubjectAccount\". ")
    @RequestMapping(value = {"/downloadStaff"}, method = RequestMethod.GET)
    public @ResponseBody byte[] downloadStaff(
            @RequestParam(value = "bSheduler", required = false, defaultValue = "false") Boolean bSheduler,
            HttpServletResponse httpResponse) throws Exception {
        
        httpResponse.setHeader("Content-disposition", "attachment; filename="
                + "staff_backup.zip");
        httpResponse.setHeader("Content-Type", "application/zip");  
        
        byte[] backupData = oServerEntitySyncService.backupTables("StaffTables");
        if(bSheduler){
            LOG.info("downloadStaff started with sheduler..");
            oServerEntitySyncService.saveBackupedFilesToServer(backupData, "StaffTables", "staff_sheduler_backup", false); 
        }
        return backupData; 
    }
    
    @ApiOperation(value = "Обновление таблиц персонала из архива с csv", notes =
            "Сontent-type запроса - убрать. В тело запроса поступает архив из восьми csv-файлов, соот-х таблицам персонала: "
                    + "\"Subject.csv\", \"SubjectContact.csv\", \"SubjectHuman.csv\", "
                    + "\"SubjectOrgan.csv\", \"SubjectHumanPositionCustom.csv\", \"SubjectGroup.csv\", \n" 
                    + "\"SubjectGroupTree.csv\", \"SubjectAccount.csv\", данные из которых полностью перезатрут собой соотв-е таблицы. "
                    + "В ответ сервис отдает архив с csv-дампами таблиц до изенений - на случай необходимости восстановления. Чтобы скачать его в "
                    + "виде файла - из постмана спользуйте кнопку Send and Download вместо Send. "
                    + "В случае ошибки - отдается бекап, http-статус ответа - 500, дамп автоматически перезаливается в таблицы. "
                    + "Пример запроса - https://beta.test.idoc.com.ua/wf/service/sync/entity/reloadStaff")
    @RequestMapping(value = {"/reloadStaff"}, method = RequestMethod.POST)
    public @ResponseBody byte[] reloadStaff(@RequestParam(value = "file") MultipartFile oMultipartFile,
            HttpServletRequest oRequest, HttpServletResponse httpResponse) throws Exception {
        
        httpResponse.setHeader("Content-disposition", "attachment; filename="
                + "staff_backup.zip");
        httpResponse.setHeader("Content-Type", "application/zip");       
        
        return oServerEntitySyncService.reloadTables(oMultipartFile.getBytes(), false, httpResponse, "StaffTables");
    }
}
