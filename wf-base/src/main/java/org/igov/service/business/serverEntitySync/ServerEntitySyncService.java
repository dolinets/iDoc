package org.igov.service.business.serverEntitySync;

import com.google.common.base.Optional;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.igov.io.GeneralConfig;
import org.igov.io.web.HttpRequester;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.igov.model.server.entity.sync.ServerEntitySync;
import org.igov.model.server.entity.sync.ServerEntitySyncDao;
import org.igov.model.server.entity.sync.ServerEntitySyncStatus;
import org.igov.model.server.entity.sync.ServerEntitySyncStatusDao;
import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.service.business.subject.SubjectGroupTreeService;
import org.igov.service.business.subject.SubjectService;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.igov.service.business.action.task.core.UsersService;
import org.json.simple.JSONArray;

/**
 * @author Yegor Kovylin
 * Load and synchronization staff
 */
@Service
public class ServerEntitySyncService {
    
    private static final int BUFFER_SIZE = 4096;
    private static final int BACKUP_FILE_SIZE = 20;
    
    public final String LOAD_FOLDER = "staffData";
    public final String BACKUP_FOLDER = "staff_backup";
    public final String BACKUP_FOLDER_RELATION = "relation_backup";
        
    public final String INSERT_ACTION = "INSERT";
    public final String REMOVE_ACTION = "REMOVE";
    public final String UPDATE_ACTION = "UPDATE";
    
    private static final Logger LOG = LoggerFactory.getLogger(ServerEntitySyncService.class);
    
    @Autowired
    private ServerEntitySyncDao oServerEntitySyncDao;
    
    @Autowired
    private ServerEntitySyncStatusDao oServerEntitySyncStatusDao;
        
    @Autowired
    private ServerDao oServerDao;
    
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    @Autowired
    private SubjectHumanDao oSubjectHumanDao;
    
    @Autowired
    private SubjectOrganDao oSubjectOrganDao;
    
    @Autowired
    private GeneralConfig oGeneralConfig;
    
    @Autowired
    private IdentityService oIdentityService;
            
    @Autowired
    private HttpRequester oHttpRequester;
    
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;
    
    @Autowired
    private SubjectService oSubjectService;
    
    @Autowired
    private SubjectHumanPositionCustomDao oSubjectHumanPositionCustomDao;
    
    @Autowired
    private UsersService oUsersService;
    
    /**
     * Add record to staff-synchronization qeue
     * @param sID_EntityRow - sID_Group_Activiti - key for synchronization
     * @param sID_EntityAction - inseret, update or delete
     * @param sID_Entity - name of entity (equals table an hibernate-class name)
     */
    public void addRecordToServerEntitySync(String sID_EntityRow, String sID_EntityAction, String sID_Entity){
        
        List<Server> aServer = oServerDao.findAll();
        List<ServerEntitySync> aServerEntitySync_ToUpdate = new ArrayList<>();
        List<ServerEntitySync> aServerEntitySync_ToDelete = new ArrayList<>();
        
        List<ServerEntitySync> aServerEntitySync_Saved = 
                oServerEntitySyncDao.getaServerEntitySync_ByEntityAndRow(sID_EntityRow, sID_Entity);
        
        if(aServerEntitySync_Saved.isEmpty()){
            aServerEntitySync_ToUpdate.addAll(addServerEntitySyncData(aServer, sID_EntityRow, sID_EntityAction, sID_Entity));
        }else{
            for(ServerEntitySync oServerEntitySync_Saved : aServerEntitySync_Saved){
                if(oServerEntitySync_Saved.getsID_EntityAction().equals(INSERT_ACTION) && 
                    sID_EntityAction.equals(REMOVE_ACTION))
                {
                    aServerEntitySync_ToDelete.add(oServerEntitySync_Saved);
                }
                else if(oServerEntitySync_Saved.getsID_EntityAction().equals(UPDATE_ACTION) && 
                    sID_EntityAction.equals(REMOVE_ACTION))
                {
                    oServerEntitySync_Saved.setsID_EntityAction(REMOVE_ACTION);
                    oServerEntitySync_Saved.setsDate(new Date());
                    aServerEntitySync_ToUpdate.add(oServerEntitySync_Saved);
                    
                }else if(oServerEntitySync_Saved.getsID_EntityAction().equals(REMOVE_ACTION) && 
                    sID_EntityAction.equals(INSERT_ACTION))
                {
                    oServerEntitySync_Saved.setsID_EntityAction(UPDATE_ACTION);
                    oServerEntitySync_Saved.setsDate(new Date());
                    aServerEntitySync_ToUpdate.add(oServerEntitySync_Saved);
                }
            }
            
            aServerEntitySync_ToUpdate.addAll(addServerEntitySyncData(aServer, sID_EntityRow, sID_EntityAction, sID_Entity));
        }
                
        oServerEntitySyncDao.saveOrUpdate(aServerEntitySync_ToUpdate);
        oServerEntitySyncDao.delete(aServerEntitySync_ToDelete);
    }
    
    private List<ServerEntitySync> addServerEntitySyncData(List<Server> aServer, String sID_EntityRow, 
            String sID_EntityAction, String sID_Entity){
        List<ServerEntitySync> aServerEntitySync_ToUpdate = new ArrayList<>();
        
        for(Server oServer : aServer){
            if(oServer.getId() != oGeneralConfig.getSelfServerId().longValue()){
                ServerEntitySync oServerEntitySync = new ServerEntitySync();
                oServerEntitySync.setnID_Server(oServer.getId());
                oServerEntitySync.setnTry(0L);
                oServerEntitySync.setsID_EntityRow(sID_EntityRow);
                oServerEntitySync.setsID_EntityAction(sID_EntityAction);
                oServerEntitySync.setsID_Entity(sID_Entity);
                oServerEntitySync.setsDate(new Date());
                ServerEntitySyncStatus oServerEntitySyncStatus = oServerEntitySyncStatusDao.findByIdExpected(1L);
                oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatus);
                aServerEntitySync_ToUpdate.add(oServerEntitySync);
            }
        }
        
        return aServerEntitySync_ToUpdate;
    }
    
    /**
     * Run staff synchronization (from controller or sheduler)
     * @param sID_Entity name of entity (equals table an hibernate-class name)
     * @param sID_EntityRow sID_Group_Activiti - key for synchronization
     */
    public void runServerEntitySync(String sID_Entity, String sID_EntityRow){
        LOG.info("runServerEntitySync started... sID_Entity {}, sID_EntityRow {}", sID_Entity, sID_EntityRow);
                
        List<ServerEntitySync> aServerEntitySync = new ArrayList<>();
        
        aServerEntitySync.addAll(oServerEntitySyncDao.
                getaServerEntitySync_ByAction(sID_EntityRow, sID_Entity, oServerEntitySyncStatusDao.findByIdExpected(1L), null));
        aServerEntitySync.addAll(oServerEntitySyncDao.
                getaServerEntitySync_ByAction(sID_EntityRow, sID_Entity, oServerEntitySyncStatusDao.findByIdExpected(2L), 3L));
        
        LOG.info("aServerEntitySync size {}", aServerEntitySync.size());
        
        List<Server> aServer = oServerDao.findAll();
        
        for(ServerEntitySync oServerEntitySync : aServerEntitySync){
            if(oServerEntitySync.getsID_Entity().equals("SubjectHuman")){
                processSubjectHuman(oServerEntitySync, aServer);
            }
            if(oServerEntitySync.getsID_Entity().equals("SubjectOrgan")){
                processSubjectOrgan(oServerEntitySync, aServer);
            }
            if(oServerEntitySync.getsID_Entity().equals("SubjectHumanPositionCustom")){
                processSubjectHumanPositionCustom(oServerEntitySync, aServer);
            }
            if(oServerEntitySync.getsID_Entity().equals("SubjectContact")){
                processSubjectContact(oServerEntitySync, aServer);
            }
        }
    }
    
    private void updateFailStatus(ServerEntitySync oServerEntitySync, String sMessage){
        oServerEntitySync.setoServerEntitySyncStatus( oServerEntitySyncStatusDao.findByIdExpected(2L));
        oServerEntitySync.setnTry(oServerEntitySync.getnTry() + 1);
        oServerEntitySync.setsDate(new Date());
        oServerEntitySync.setsAnswer(sMessage);
        oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
    }
    
     /**
     * delete all records from ServerEntitySync table
     */
    public void removeAllServerEntitySync(){
        List<ServerEntitySync> aServerEntitySyncStatus = oServerEntitySyncDao.findAll();
        oServerEntitySyncDao.delete(aServerEntitySyncStatus);
    }
    
    private void processSubjectHumanPositionCustom(ServerEntitySync oServerEntitySync, List<Server> aServer){
        try{    
            
            LOG.info("processSubjectHumanPositionCustom started..");
            
            Server oServer_Current = aServer.stream()
                            .filter(oServer -> oServer.getId().equals(oServerEntitySync.getnID_Server())).findAny().get();
            LOG.info("oServerEntitySync.getsID_EntityRow() {}", oServerEntitySync.getsID_EntityRow());
            Optional<SubjectHumanPositionCustom> oSubjectHumanPositionCustom_Optional =
                    oSubjectHumanPositionCustomDao.findBy("name", oServerEntitySync.getsID_EntityRow());
            LOG.info("oSubjectHumanPositionCustom_Optional.isPresent() {}", oSubjectHumanPositionCustom_Optional.isPresent());
            StringBuilder sURL = new StringBuilder();
            Map<String, String> mParamMap = new HashMap<>();
            //sURL.append(oServer_Current.getsURL()).append("/wf/service/subject/setSubjectHumanPositionCustom");
            sURL.append(oServer_Current.getsURL()).append("/wf/service/subject/setSubjectHumanPositionCustom");
            
            if(oSubjectHumanPositionCustom_Optional.isPresent()){
                mParamMap.put("isSync", "true");
                mParamMap.put("sName", oSubjectHumanPositionCustom_Optional.get().getName());
                mParamMap.put("sNote", oSubjectHumanPositionCustom_Optional.get().getsNote());
                
                if(oSubjectHumanPositionCustom_Optional.get().getName().contains("_")){
                    mParamMap.put("sChain", oSubjectHumanPositionCustom_Optional.get().getName().split("_")[0] + "_");
                }
                if(oServerEntitySync.getsID_EntityAction().equals(INSERT_ACTION)){
                    mParamMap.put("bCreate", "true");
                }
                
                if(oServerEntitySync.getsID_EntityAction().equals(UPDATE_ACTION)){
                   mParamMap.put("bCreate", "false");
                }
                
                LOG.info("sURL in processSubjectHumanPositionCustom is {}", sURL.toString());
                oHttpRequester.getInside(sURL.toString(), mParamMap);
                oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatusDao.findByIdExpected(3L));
                oServerEntitySync.setsDate(new Date());
                oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
                
                LOG.info("processSubjectHumanPositionCustom ended..");
            }else{
                updateFailStatus(oServerEntitySync, "Can't find SubjectHumanPositionCustom");
            }
        } catch (Exception ex) {
            updateFailStatus(oServerEntitySync, ex.getMessage());
        }
    }
    
    private void processSubjectOrgan(ServerEntitySync oServerEntitySync, List<Server> aServer){
        try{
            Server oServer_Current = aServer.stream()
                        .filter(oServer -> oServer.getId().equals(oServerEntitySync.getnID_Server())).findAny().get();
            StringBuilder sURL = new StringBuilder();
            
            Optional<SubjectGroup> oSubjectGroup_Optional = oSubjectGroupDao.findBy("sID_Group_Activiti", oServerEntitySync.getsID_EntityRow());
        
            Subject oSubject = null;
            SubjectOrgan oSubjectOrgan = null;
            String sMessage = null;
            Map<String, String> mParamMap = new HashMap<>();
            
            if (oSubjectGroup_Optional.isPresent()) {
                
                SubjectGroup oSubjectGroup = oSubjectGroup_Optional.get();
                oSubject = oSubjectGroup.getoSubject();
                oSubjectOrgan = oSubjectOrganDao.getSubjectOrgan(oSubject);
                if (oSubject != null && oSubjectOrgan != null){
                    //sURL.append(oServer_Current.getsURL()).append("/wf/service/subject/setSubjectOrgan);
                    sURL.append(oServer_Current.getsURL()).append("/wf/service/subject/setSubjectOrgan");
                    
                    mParamMap.put("isSync", "true");
                    
                    if(oSubjectOrgan.getName() != null){
                        mParamMap.put("sName", oSubjectOrgan.getName());
                    }
                    
                    if(oSubjectGroup.getsID_Group_Activiti() != null){
                        mParamMap.put("sID_Group_Activiti", oSubjectGroup.getsID_Group_Activiti());
                    }
                    
                    if(oSubjectOrgan.getsOKPO() != null){
                        mParamMap.put("sOKPO", oSubjectOrgan.getsOKPO());
                    }
                    
                    if(oSubjectOrgan.getsFormPrivacy() != null){
                        mParamMap.put("sFormPrivacy", oSubjectOrgan.getsFormPrivacy());
                    }
                    
                    String sEmail = oSubjectService.getEmailByLogin(oSubjectGroup.getsID_Group_Activiti());
                    if(sEmail != null){
                        mParamMap.put("sEmail", sEmail);
                    }
                    
                    List<SubjectGroup> aCompany_Parent = oSubjectGroupTreeService.getSubjectGroupsTreeUp(oSubjectGroup.getsID_Group_Activiti(), "Organ", 1L);   
                    
                    if(!aCompany_Parent.isEmpty()){
                        mParamMap.put("sID_Group_Activiti_Parent", aCompany_Parent.get(0).getsID_Group_Activiti());
                    }
                    
                    if(oServerEntitySync.getsID_EntityAction().equals(INSERT_ACTION)){
                       mParamMap.put("bCreate", "true");
                       if(!aCompany_Parent.isEmpty()){
                           mParamMap.remove("sID_Group_Activiti");
                       }
                    }
                    
                    if(oServerEntitySync.getsID_EntityAction().equals(UPDATE_ACTION)){
                        mParamMap.put("bCreate", "false");
                    }
                    
                    LOG.info("sURL in processSubjectOrgan {}", sURL.toString());
                    oHttpRequester.getInside(sURL.toString(), mParamMap);
                    oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatusDao.findByIdExpected(3L));
                    oServerEntitySync.setsDate(new Date());
                    oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
                }else{
                    sMessage = "Can't find Subject or SubjectOrgan";
                }
            } else {
                sMessage = "Can't find SubjectGroup";
            }
            
            if(sMessage != null){
                updateFailStatus(oServerEntitySync, sMessage);
            }
            
        } catch (Exception ex) {
            updateFailStatus(oServerEntitySync, ex.getMessage());
        }
    }
    
    private void processSubjectHuman(ServerEntitySync oServerEntitySync, List<Server> aServer) {
        try{
            Server oServer_Current = aServer.stream()
                        .filter(oServer -> oServer.getId().equals(oServerEntitySync.getnID_Server())).findAny().get();
            LOG.info("oServer_Current url is {}", oServer_Current.getsURL());
            StringBuilder sURL_Params = new StringBuilder();
            Optional<SubjectGroup> oSubjectGroup_Optional = oSubjectGroupDao.findBy("sID_Group_Activiti", oServerEntitySync.getsID_EntityRow());
            Map<String, String> mParamMapReferent = new HashMap<>();
            Subject oSubject = null;
            SubjectHuman oSubjectHuman = null;
            String sMessage = null;
            Map<String, String> mParamMap = new HashMap<>();
            
            if (oSubjectGroup_Optional.isPresent()) {
                SubjectGroup oSubjectGroup = oSubjectGroup_Optional.get();
                oSubject = oSubjectGroup.getoSubject();
                oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);

                if (oSubject != null && oSubjectHuman != null) {
                    User oUser = oIdentityService.createUserQuery().userId(oSubjectGroup.getsID_Group_Activiti()).singleResult();
                        
                    if(oUser == null){
                        sMessage = "Can't find identity user";
                    }
                        
                    List<SubjectGroup> aCompany = oSubjectGroupTreeService.getSubjectGroupsTreeUp(oSubjectGroup.getsID_Group_Activiti(), "Organ", 1L);
                        
                    if(aCompany.isEmpty()){
                        sMessage = "Can't find company for SubjectHuman";
                    }
                        
                    if(sMessage == null){
                        
                        if(oSubjectHuman.getsFamily() != null){
                            mParamMap.put("sFamily", oSubjectHuman.getsFamily());
                            sURL_Params.append("&sFamily=").append(oSubjectHuman.getsFamily());
                        }
                        
                        if(oSubjectHuman.getName() != null){
                            mParamMap.put("sName", oSubjectHuman.getName());
                            sURL_Params.append("&sName=").append(oSubjectHuman.getName());
                        }
                        
                        if(oSubjectHuman.getsSurname() != null){
                            mParamMap.put("sSurname", oSubjectHuman.getsSurname());
                            sURL_Params.append("&sSurname=").append(oSubjectHuman.getsSurname());
                        }
                        
                        if(oSubjectGroup.getsID_Group_Activiti() != null){
                            mParamMap.put("sLoginStaff", oSubjectGroup.getsID_Group_Activiti());
                            sURL_Params.append("&sLoginStaff=").append(oSubjectGroup.getsID_Group_Activiti());
                        }
                        
                        if(oSubjectHuman.getDefaultPhone()!= null && oSubjectHuman.getDefaultPhone().getsValue() != null){
                            mParamMap.put("sPhone", oSubjectHuman.getDefaultPhone().getsValue());
                            sURL_Params.append("&sPhone=").append(oSubjectHuman.getDefaultPhone().getsValue());
                        }
                        
                        if(oSubjectHuman.getDefaultEmail() != null && oSubjectHuman.getDefaultEmail().getsValue() != null){
                            mParamMap.put("sEmail", oSubjectHuman.getDefaultEmail().getsValue());
                            sURL_Params.append("&sEmail=").append(oSubjectHuman.getDefaultEmail().getsValue());
                        }
                        
                        if(oSubjectGroup.getoSubjectHumanPositionCustom() != null && oSubjectGroup.getoSubjectHumanPositionCustom().getName() != null){
                            mParamMap.put("sPosition", oSubjectGroup.getoSubjectHumanPositionCustom().getName());
                            sURL_Params.append("&sPosition=").append(oSubjectGroup.getoSubjectHumanPositionCustom().getName());
                        }
                        
                        if(oUser != null && oUser.getPassword() != null){
                            mParamMap.put("sPassword", oUser.getPassword());
                            sURL_Params.append("&sPassword=").append(oUser.getPassword());
                        }
                        
                        if(!aCompany.isEmpty() && aCompany.get(0).getsID_Group_Activiti() != null){
                            mParamMap.put("sID_Group_Activiti_Organ", aCompany.get(0).getsID_Group_Activiti());
                            sURL_Params.append("&sID_Group_Activiti_Organ=").append(aCompany.get(0).getsID_Group_Activiti());
                        }
                        
                        if(oSubject.getoSubjectStatus() != null && oSubject.getoSubjectStatus().getsName() != null){
                            mParamMap.put("sStatus", oSubject.getoSubjectStatus().getsName());
                            sURL_Params.append("&sStatus=").append(oSubject.getoSubjectStatus().getsName());
                        }
                        
                        if(oSubjectHuman.getsDateBirth() != null){
                            DateTimeFormatter dtf = org.joda.time.format.DateTimeFormat.forPattern("dd.MM.yyyy");
                            sURL_Params.append("&sDateBirth=").append(dtf.print(oSubjectHuman.getsDateBirth()));
                            mParamMap.put("sDateBirth", dtf.print(oSubjectHuman.getsDateBirth()));
                        }
                        
                        SubjectGroup oSubjectGroupHead = oSubjectGroupTreeService.getHeadInDepart(aCompany.get(0));
                        
                        if(oSubjectGroupHead != null && oSubjectGroupHead.getsID_Group_Activiti().equals(oSubjectGroup.getsID_Group_Activiti())){
                            sURL_Params.append("&isHead=true");
                            mParamMap.put("isHead", "true");
                        }
                        
                        //set referent
                        /*List<Map<String, String>> amUserGroup_Current = 
                                oUsersService.getFioUserGroupMember(oSubjectGroup.getsID_Group_Activiti());
                        
                        JSONArray aJSON_Referent = new JSONArray();
                        aJSON_Referent.addAll(amUserGroup_Current);
                        LOG.info("aJSON_Referent {}", aJSON_Referent.toJSONString());*/
                        
                        Map<String, String> mParamMap_Group = new HashMap<>();
                        mParamMap_Group.put("sLoginStaff", oSubjectGroup.getsID_Group_Activiti());
                        String sGroup = oHttpRequester.getInside(oGeneralConfig.getSelfHost() + "/wf/service/action/identity/getUserGroupMember", mParamMap_Group);
                        LOG.info("sGroups is {}", sGroup);
                        mParamMapReferent.put("saGroup", sGroup);
                        mParamMapReferent.put("sLoginStaff", oSubjectGroup.getsID_Group_Activiti());
                        mParamMapReferent.put("isSync", "true");
                    }
                }else{
                    sMessage = "Can't find Subject or SubjectHuman";
                }
                    
            } else {
                sMessage = "Can't find SubjectGroup";
            }
            
            if(sMessage == null){
                LOG.info("sURL_Params in processSubjectHuman {}", sURL_Params.toString());
                if (oServerEntitySync.getsID_EntityAction().equals(INSERT_ACTION)) {
                    LOG.info("SubjectHuman post request started");
                    //oHttpRequester.postInside(oServer_Current.getsURL() + "/wf/service/subject/setSubjectHuman?isSync=true" + sURL_Params.toString(), 
                    oHttpRequester.postInside(oServer_Current.getsURL() + "/wf/service/subject/setSubjectHuman?isSync=true" + sURL_Params.toString(), 
                            null, null, null, "kermit", "kermit");
                    oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatusDao.findByIdExpected(3L));
                    oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
                }   
                if (oServerEntitySync.getsID_EntityAction().equals(UPDATE_ACTION)) {
                    LOG.info("SubjectHuman get request started");
                    //oHttpRequester.getInside(oServer_Current.getsURL() + "/wf/service/subject/setSubjectHuman?isSync=true" + sURL_Params.toString(), null);
                    mParamMap.put("isSync", "true");
                    mParamMap.put("sLogin", mParamMap.get("sLoginStaff"));
                    oHttpRequester.getInside(oServer_Current.getsURL() + "/wf/service/subject/setSubjectHuman", mParamMap);
                    oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatusDao.findByIdExpected(3L));
                    oServerEntitySync.setsDate(new Date());
                    oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
                }
                oHttpRequester.getInside(oServer_Current.getsURL() + "/wf/service/action/identity/replaseUserGroupMember", mParamMapReferent);
            }else{
                updateFailStatus(oServerEntitySync, sMessage);
            }
        } catch (Exception ex) {
            updateFailStatus(oServerEntitySync, ex.getMessage());
        }
    }
    
    private void processSubjectContact(ServerEntitySync oServerEntitySync, List<Server> aServer){
        
        try{
            Server oServer_Current = aServer.stream()
                            .filter(oServer -> oServer.getId().equals(oServerEntitySync.getnID_Server())).findAny().get();

            Map<String, String> mParamMap = new HashMap<>();
            mParamMap.put("isSync", "true");
            
            /*String sID_Group_Activiti = oServerEntitySync.getsID_EntityRow();
            mParamMap.put("sID_Group_Activiti", sID_Group_Activiti);
            oHttpRequester.getInside("https://alpha.test.region.igov.org.ua" + "/wf/service/subject/deleteAllSubjectContact", mParamMap);*/
            
            JSONParser oJSONParser = new JSONParser();
            JSONObject oJSONObjectGot = (JSONObject) oJSONParser.parse(oServerEntitySync.getsID_EntityRow());
            String sBody = (String) oJSONObjectGot.get("oArray");
            String sID_Group_Activiti = (String) oJSONObjectGot.get("sLogin");
            
            StringBuilder osURL = new StringBuilder();
            
            if(oServerEntitySync.getsID_EntityAction().equals(INSERT_ACTION)){
              osURL.append(oServer_Current.getsURL()).append("/wf/service/subject/setSubjectContact?isSync=true")
                    .append("&sID_Group_Activiti=").append(sID_Group_Activiti);
            }
            
            if(oServerEntitySync.getsID_EntityAction().equals(REMOVE_ACTION)){
              osURL.append(oServer_Current.getsURL()).append("/wf/service/subject/deleteSubjectContact?isSync=true")
                    .append("&sID_Group_Activiti=").append(sID_Group_Activiti);
            }
            
            oHttpRequester.postInside(osURL.toString(), null, sBody, "application/json", "kermit", "kermit");
            oServerEntitySync.setoServerEntitySyncStatus(oServerEntitySyncStatusDao.findByIdExpected(3L));
            oServerEntitySync.setsDate(new Date());
            oServerEntitySyncDao.saveOrUpdate(oServerEntitySync);
        } catch (Exception ex) {
            updateFailStatus(oServerEntitySync, ex.getMessage());
        }
    }
    
     /**
     * Run staff synchronization after action with a staff
     * @param sID_Entity name of entity (equals table an hibernate-class name)
     * @param sID_EntityRow sID_Group_Activiti - key for synchronization
     * @param sAction - insert, update or delete
     * @param isSync - if it is a synchronization
     */
    public void startSyncFromService(String sID_Entity, String sID_EntityRow, String sAction, Boolean isSync){
        LOG.info("startSyncFromService satrted");
        if(isSync == null || isSync == false){
        addRecordToServerEntitySync(sID_EntityRow, sAction, sID_Entity);
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                runServerEntitySync(sID_Entity, sID_EntityRow);
                }
            }).start();
        }
    }
    
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Clean all main staff tables
     */
    @Transactional("transactionManager")
    public void cleanTables(String sTablesMarker) throws SQLException{
        LOG.info("cleanTables is started...");
        dropIndexes(sTablesMarker); //truncate with cascade drop all related tables, so we should destroy some FK before truncating
        if(sTablesMarker.equals("StaffTables")){
            jdbcTemplate.execute("TRUNCATE \"SubjectContact\", \"SubjectAccount\", "
                    + "\"SubjectHumanPositionCustom\", \"SubjectGroupTree\",  "
                    + "\"SubjectOrgan\",  \"SubjectHuman\", \"SubjectGroup\", \"Subject\", \"SubjectRightBP\" CASCADE");
        }
        if(sTablesMarker.equals("RelationTables")){
            jdbcTemplate.execute("TRUNCATE \"RelationClass\", \"Relation\", \"ObjectGroup\", \"AttributeObject\", " +
                    "\"ObjectGroup\", \"ObjectGroupAttribute\", \"Relation_ObjectGroup\" CASCADE"); 
        }
        
        LOG.info("cleanTables is finished...");
    }
    
    @Transactional("transactionManager")
    private void dropIndexes(String sTablesMarker){
        if(sTablesMarker.equals("StaffTables")){
            jdbcTemplate.execute("ALTER TABLE \"public\".\"SubjectMessage\" DROP CONSTRAINT IF EXISTS \"FK_SubjectContact_Mail_SubjectContact_nID\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"SubjectOrganJoin\" DROP CONSTRAINT IF EXISTS \"FK_SubjectOrganJoin_SubjectOrgan\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"ServiceData\" DROP CONSTRAINT IF EXISTS \"FK_ServiceData_SubjectOrgan\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"SubjectHumanRole_SubjectHuman\" DROP CONSTRAINT IF EXISTS \"FK_SubjectHuman\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"HistoryEvent\" DROP CONSTRAINT IF EXISTS \"FK_Subject_HistoryEvent\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"SubjectMessage\" DROP CONSTRAINT IF EXISTS \"FK_Subject_SubjectMessage\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"ObjectGroup\" DROP CONSTRAINT IF EXISTS \"FK_ObjectGroup_Subject\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"Document\" DROP CONSTRAINT IF EXISTS \"FK_Document_Subject\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"Document\" DROP CONSTRAINT IF EXISTS \"FK_Document_Subject_Upload\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"ObjectItem\" DROP CONSTRAINT IF EXISTS \"FK_ObjectItem_Subject\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"SubjectOrgan\" DROP CONSTRAINT IF EXISTS \"FK_SubjectOrgan_Country\";");
        }
        if(sTablesMarker.equals("RelationTables")){
            //jdbcTemplate.execute("ALTER TABLE \"public\".\"ObjectGroupAttribute\" DROP CONSTRAINT IF EXISTS \"FK_ObjectGroup\";");
            jdbcTemplate.execute("ALTER TABLE \"public\".\"Relation_ObjectItem\" DROP CONSTRAINT IF EXISTS \"FK_Relation_ObjectItem_Relation\";");
        }
    }
    
    @Transactional("transactionManager")
    private void reCreateIndexes(String sTablesMarker){
        dropIndexes(sTablesMarker);
        if(sTablesMarker.equals("StaffTables")){
            jdbcTemplate.execute("ALTER TABLE \"SubjectMessage\" ADD CONSTRAINT \"FK_SubjectContact_Mail_SubjectContact_nID\" FOREIGN KEY (\"nID_SubjectContact_Mail\") REFERENCES \"SubjectContact\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"SubjectOrganJoin\" ADD CONSTRAINT \"FK_SubjectOrganJoin_SubjectOrgan\" FOREIGN KEY (\"nID_SubjectOrgan\") REFERENCES \"SubjectOrgan\"(\"nID\") ON DELETE CASCADE;");
            jdbcTemplate.execute("ALTER TABLE \"ServiceData\" ADD CONSTRAINT \"FK_ServiceData_SubjectOrgan\" FOREIGN KEY (\"nID_Subject_Operator\") REFERENCES \"SubjectOrgan\"(\"nID\") ON DELETE CASCADE;");
            jdbcTemplate.execute("ALTER TABLE \"SubjectHumanRole_SubjectHuman\" ADD CONSTRAINT \"FK_SubjectHuman\" FOREIGN KEY (\"nID_SubjectHuman\") REFERENCES \"SubjectHuman\"(\"nID\") ON DELETE CASCADE;");
            jdbcTemplate.execute("ALTER TABLE \"HistoryEvent\" ADD CONSTRAINT \"FK_Subject_HistoryEvent\" FOREIGN KEY (\"nID_Subject\") REFERENCES \"Subject\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"SubjectMessage\" ADD CONSTRAINT \"FK_Subject_SubjectMessage\" FOREIGN KEY (\"nID_Subject\") REFERENCES \"Subject\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"ObjectGroup\" ADD CONSTRAINT \"FK_ObjectGroup_Subject\" FOREIGN KEY (\"nID_Subject_Source\") REFERENCES \"Subject\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"Document\" ADD CONSTRAINT \"FK_Document_Subject\" FOREIGN KEY (\"nID_Subject\") REFERENCES \"Subject\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"Document\" ADD CONSTRAINT \"FK_Document_Subject_Upload\" FOREIGN KEY (\"nID_Subject_Upload\") REFERENCES \"Subject\" (\"nID\") ON DELETE CASCADE;");
            jdbcTemplate.execute("ALTER TABLE \"ObjectItem\" ADD CONSTRAINT \"FK_ObjectItem_Subject\" FOREIGN KEY (\"nID_Subject_Source\") REFERENCES \"Subject\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"SubjectOrgan\" ADD CONSTRAINT \"FK_SubjectOrgan_Country\" FOREIGN KEY (\"nID_Country\") REFERENCES \"Country\"(\"nID\") ON DELETE CASCADE;");
        }
        if(sTablesMarker.equals("RelationTables")){
            //jdbcTemplate.execute("ALTER TABLE \"ObjectGroupAttribute\" ADD CONSTRAINT \"FK_ObjectGroup\" FOREIGN KEY (\"nID_ObjectGroup\") REFERENCES \"ObjectGroup\" (\"nID\");");
            jdbcTemplate.execute("ALTER TABLE \"Relation_ObjectItem\" ADD CONSTRAINT \"FK_Relation_ObjectItem_Relation\" FOREIGN KEY (\"nID_Relation\") REFERENCES \"Relation\" (\"nID\");");
        }
    }
    
    /**
     * Reload staff-data from zip of csv
     * @param aMultipartFile_ByteBody - byte-body of zip archive
     * @param isReload - if we load after exception
     * @param httpResponse - for setting 500 http-status if error
     * @return zip-backup of staff tables befor loading
     * @throws Exception 
     */
    @Transactional("transactionManager")
    public byte[] reloadTables(byte[] aMultipartFile_ByteBody, Boolean isReload, HttpServletResponse httpResponse, String sTablesMarker) throws Exception{
        
        ByteArrayInputStream aByteArrayInputStream = new ByteArrayInputStream(aMultipartFile_ByteBody);
        
        File oDirectoryToLoad =  new File(LOAD_FOLDER);
        
        if(oDirectoryToLoad.exists() && !isReload){
            FileUtils.deleteDirectory(oDirectoryToLoad); 
        }
                
        unzipFiles(aByteArrayInputStream); //get files from input zip
        
        byte[] aByteZipBackup = backupTables(sTablesMarker); //backup current tables from database in output zip file
        
        if(!isReload){
            saveBackupedFilesToServer(aByteZipBackup, sTablesMarker, BACKUP_FOLDER, true); //save output backup zip to server too
        }
        
        try{
            String[] asTable = null;
        
            if(sTablesMarker.equals("StaffTables")){
                String[] sTable_Array = {"Subject", "SubjectContact", "SubjectHuman", "SubjectOrgan", "SubjectHumanPositionCustom", 
                        "SubjectGroup", "SubjectGroupTree", "SubjectAccount", "SubjectRightBP"}; //order of tables in array is important!
                asTable = sTable_Array;
                oHttpRequester.getInside(oGeneralConfig.getSelfHost() + "/wf/service/sync/entity/cleanTables", new HashMap<>()); //clean all staff tables
            }
            
            if(sTablesMarker.equals("RelationTables")){
                String[] sTable_Array = {"RelationClass", "Relation", "ObjectGroup", "AttributeObject", 
                    "ObjectGroupAttribute", "Relation_ObjectGroup"}; //order of tables in array is important!
                asTable = sTable_Array;
                oHttpRequester.getInside(oGeneralConfig.getSelfHost() + "/wf/service/sync/entity/cleanRelationTables", new HashMap<>()); //clean all staff tables
            }
            
            for(int i = 0; i < asTable.length; i++){
                reloadStaffTable(asTable[i]); //load data from input csv in table
            }
            
            reCreateIndexes(sTablesMarker); // after success loading we remake deleted indexes
        }
        catch (Exception ex){
            
            if(httpResponse != null){
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            
            LOG.info("Exception during staff reload {}", ex.getMessage());
            //jdbcTemplate.execute("SELECT pg_terminate_backend(\"pid\") FROM \"pg_stat_activity\" WHERE \"query\" LIKE 'COPY%'");
            if(!isReload){
                LOG.info("Rollback started...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reloadTables(aByteZipBackup, true, null, sTablesMarker);
                            reCreateIndexes(sTablesMarker);
                        } catch (Exception ex1) {
                           LOG.info("Exception during staff rollback {}", ex1.getMessage());
                        }
                    }
                }).start();
            }
            
            return createErrorArchive(aByteZipBackup, ex.getMessage());
        }
        
        return aByteZipBackup;
    }
    
    @Transactional("transactionManager")
    public byte[] backupTables(String sTablesMarker) throws SQLException, UnsupportedEncodingException, IOException{
        //backup tables in zip archive
        String[] asTable = null;
        
        if(sTablesMarker.equals("StaffTables")){
            String[] sTable_Array = {"Subject", "SubjectContact", "SubjectHuman", "SubjectOrgan", "SubjectGroup", 
                "SubjectGroupTree", "SubjectAccount", "SubjectHumanPositionCustom", "SubjectRightBP", 
                "act_id_user", "act_id_group", "act_id_membership"}; 
            asTable = sTable_Array;
        }
        
        if(sTablesMarker.equals("RelationTables")){
            String[] sTable_Array = {"RelationClass", "Relation", "ObjectGroup", "AttributeObject", 
                    "ObjectGroupAttribute", "Relation_ObjectGroup"};
            asTable = sTable_Array;
        }
        
        LOG.info("prepareStaffTables is started...");
        ByteArrayOutputStream oByteArrayOutputStream_Output;
        try (Connection oConnection = jdbcTemplate.getDataSource().getConnection()) {
            CopyManager oCopyManager = new CopyManager((BaseConnection)oConnection);
            oByteArrayOutputStream_Output = new ByteArrayOutputStream();
            try (ZipOutputStream oZipOutputStream = new ZipOutputStream(oByteArrayOutputStream_Output)) {
                for(int i = 0; i < asTable.length; i++){
                    ByteArrayOutputStream ByteArrayOutputStream_FileOutput = new ByteArrayOutputStream();
                    ByteArrayOutputStream_FileOutput.write(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF }); //for correct work in Excel we should add BOM-bytes
                    oCopyManager.copyOut("COPY \"public\".\"" + asTable[i] + "\" TO STDOUT WITH DELIMITER ';' NULL AS 'NULL' CSV HEADER;", ByteArrayOutputStream_FileOutput);
                    ZipEntry oZipEntry_Output = new ZipEntry(asTable[i]  + ".csv");
                    oZipOutputStream.putNextEntry(oZipEntry_Output);
                    oZipOutputStream.write(ByteArrayOutputStream_FileOutput.toByteArray(), 0, ByteArrayOutputStream_FileOutput.toByteArray().length);
                    //jdbcTemplate.execute("SELECT pg_cancel_backend(\"pid\") FROM \"pg_stat_activity\" WHERE \"query\" LIKE 'COPY%'");
                }
                oZipOutputStream.closeEntry();
            }
            //jdbcTemplate.execute("SELECT pg_terminate_backend(\"pid\") FROM \"pg_stat_activity\" WHERE \"query\" LIKE 'COPY%'");
        }
        LOG.info("prepareStaffTables is finished...");
        return oByteArrayOutputStream_Output.toByteArray();
    }
    
    private byte[] createErrorArchive(byte[] aByteZipBackup, String sError) throws SQLException, UnsupportedEncodingException, IOException {
        ByteArrayOutputStream oByteArrayOutputStream_Output = new ByteArrayOutputStream();
        try (ZipOutputStream oZipOutputStream = new ZipOutputStream(oByteArrayOutputStream_Output)) {
            ZipEntry oZipEntry_Output_Backup = new ZipEntry("staff_backup.zip");
            oZipOutputStream.putNextEntry(oZipEntry_Output_Backup);
            oZipOutputStream.write(aByteZipBackup, 0, aByteZipBackup.length);
            ZipEntry oZipEntry_Output_Error = new ZipEntry("error.txt");
            oZipOutputStream.putNextEntry(oZipEntry_Output_Error);
            oZipOutputStream.write(sError.getBytes(), 0, sError.getBytes().length);
            oZipOutputStream.closeEntry();
        }
        return oByteArrayOutputStream_Output.toByteArray();
    }
    
    
    
    @Transactional("transactionManager")
    private void reloadStaffTable(String sTable) throws SQLException, UnsupportedEncodingException, IOException{   
        //load data from folder to table
        LOG.info("reloadStaff is started...{}", sTable);
        String[] asTable = {sTable};
        
        try (Connection oConnection = jdbcTemplate.getDataSource().getConnection()) {
            CopyManager oCopyManager = new CopyManager((BaseConnection)oConnection);

            for(int i = 0; i < asTable.length; i++){
                File sTableFile = new File(LOAD_FOLDER + "/" + asTable[i] + ".csv");
                
                try (InputStreamReader oInputStreamReader = new InputStreamReader(
                        new BOMInputStream(new FileInputStream(sTableFile), false, ByteOrderMark.UTF_8,
                                                ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
                                                ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE))) //BOM - for correct work wit Excel
                {
                    String sColumns_ToCopy;
                    String sDelimiter = null;
                    try (BufferedReader oBufferedReader = new BufferedReader(oInputStreamReader)) 
                    {
                        String sHeader = oBufferedReader.readLine();
                        String[] asColumnsName = null;
                        //Excel doesn't work with csv delimiter ";", so we shold support both schemes
                        //after saving with it, all ";" automaticly replace with tabulation
                        if(sHeader.contains(";")){
                            LOG.info("sHeader.contains(\";\")");
                            asColumnsName = sHeader.split(";");
                            sDelimiter = "';'";
                        }
                        else{
                            LOG.info("sHeader.contains(\"\t\")");
                            asColumnsName = sHeader.split("\t", -1);
                            sDelimiter = "E'\t'";
                        }
                        
                        sColumns_ToCopy = "";
                        for(int j = 0; j < asColumnsName.length; j++){
                            sColumns_ToCopy = sColumns_ToCopy + " \"" + asColumnsName[j] + "\"";
                            
                            if(j != asColumnsName.length - 1){
                                sColumns_ToCopy = sColumns_ToCopy + ",";
                            }
                        }
                    }
                    
                    LOG.info("sColumns_ToCopy is {}", sColumns_ToCopy);
                    try (InputStreamReader oInputStreamReader_load = 
                            /*new InputStreamReader(new BOMInputStream(new FileInputStream(sTableFile), false, ByteOrderMark.UTF_8,
                                                ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
                                                ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE), "UTF-8"))*/
                            new InputStreamReader(new FileInputStream(sTableFile), "UTF-8"))
                    {
                        oCopyManager.copyIn("COPY \"public\".\"" + asTable[i] + "\" ("+ sColumns_ToCopy +") "
                            + "FROM STDIN WITH DELIMITER " + sDelimiter + " NULL AS 'NULL' CSV HEADER;", oInputStreamReader_load);
                    }
                }
            }
            LOG.info("reloadStaff is finished...");
            //jdbcTemplate.execute("SELECT pg_cancel_backend(\"pid\") FROM \"pg_stat_activity\" WHERE \"query\" LIKE 'COPY%'");
            //jdbcTemplate.execute("SELECT pg_terminate_backend(\"pid\") FROM \"pg_stat_activity\" WHERE \"query\" LIKE 'COPY%'");
        }
    }
    
    public void saveBackupedFilesToServer(byte[] backupData, String sTablesMarker, 
            String sBackup_Folder_Name, Boolean bDeleteOldestFile) throws IOException{
        
        File oDirectoryToBackup = null;
        String sSubPath = null;
        
        if(sTablesMarker.equals("StaffTables")){
            oDirectoryToBackup = new File(sBackup_Folder_Name);
            sSubPath = "/staffBackup_";
        }
        if(sTablesMarker.equals("RelationTables")){
            oDirectoryToBackup = new File(BACKUP_FOLDER_RELATION);
            sSubPath = "/relationBackup_";
        }
        
        LOG.info("saveBackupedFilesToServer started..");
        
        if(!oDirectoryToBackup.exists()){
            oDirectoryToBackup.mkdir();
        }
        
        Integer savedFileCount = oDirectoryToBackup.listFiles().length;
        LOG.info("savedFileCount is {}", savedFileCount);
        
        if(savedFileCount  == BACKUP_FILE_SIZE && bDeleteOldestFile){
            File[] aFile = oDirectoryToBackup.listFiles();
            Arrays.sort(aFile, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            aFile[aFile.length - 1].delete();
        }
        
        String sBackupPath =  (sBackup_Folder_Name + sSubPath + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + 
                ".zip").replace(" ", "_");    
        LOG.info("sBackupPath is {}", sBackupPath);
        FileUtils.writeByteArrayToFile(new File(sBackupPath), backupData);
    }
    
    private void unzipFiles(InputStream oZipFileInputStream) throws IOException {
        try (ZipInputStream oZipInputStream = new ZipInputStream(oZipFileInputStream)) {
            ZipEntry oZipEntry = oZipInputStream.getNextEntry();
            while (oZipEntry != null) {
                File oDirectoryToLoad = new File(LOAD_FOLDER);
                if (!oDirectoryToLoad.exists()) {
                    oDirectoryToLoad.mkdir();
                }
                String sFilePath = LOAD_FOLDER + "/" + oZipEntry.getName();
                extractFile(oZipInputStream, sFilePath);
                oZipInputStream.closeEntry();
                oZipEntry = oZipInputStream.getNextEntry();
            }
        }
    }
    
    private void extractFile(ZipInputStream oZipInputStream, String sFilePath) throws IOException {
        try (BufferedOutputStream oBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(sFilePath))) {
            byte[] aByte_Readed = new byte[BUFFER_SIZE];
            int nReadStatus = 0;
            while ((nReadStatus = oZipInputStream.read(aByte_Readed)) != -1) {
                oBufferedOutputStream.write(aByte_Readed, 0, nReadStatus);
            }
        }
    }
}
