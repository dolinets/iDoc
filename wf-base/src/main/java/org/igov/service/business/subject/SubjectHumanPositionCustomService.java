package org.igov.service.business.subject;

import com.google.common.base.Strings;
import javassist.NotFoundException;
import org.igov.model.subject.SubjectHumanPositionCustom;
import org.igov.model.subject.SubjectHumanPositionCustomDao;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import org.igov.service.business.util.SubjectUtils;
import org.igov.util.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectHumanPositionCustomService {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanPositionCustomService.class);
    
    @Autowired
    private SubjectHumanPositionCustomDao oSubjectHumanPositionCustomDao;
    @Autowired
    private ServerEntitySyncService oServerEntitySyncService;
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;

    public List<SubjectHumanPositionCustom> getSubjectHumanPositionCustoms(String sLogin, String sNote, String sLoginReferent) {
        String sCompanyChain = getCompanyChainForLogin(sLogin);
        if (Strings.isNullOrEmpty(sNote)) {
            return getAllPositionsInCompany(sCompanyChain);
        }
        return findPositionsInCompany(sCompanyChain, sNote);
    }
    
    private List<SubjectHumanPositionCustom> getAllPositionsInCompany(String sCompanyChain) {
        return oSubjectHumanPositionCustomDao.findAllByStartLikeName(sCompanyChain);
    }

    private List<SubjectHumanPositionCustom> findPositionsInCompany(String sCompanyChain, String sNote) {
        return oSubjectHumanPositionCustomDao.findAllByStartLikeNameAndAroundLikeNote(sCompanyChain, sNote);
    }

    /**
     * create, update (by flag bCreate)
     * sLogin used
     */
    public SubjectHumanPositionCustom setSubjectHumanPositionCustom(String sNote, String sName,
            Boolean isSync, Boolean bCreate, String sLogin, String sLoginReferent, String sChain) {
        
        LOG.info("{} SubjectHumanPositionCustom, sLogin: {}, sLoginReferent: {}", bCreate ? "create" : "update", sLogin, sLoginReferent);

        SubjectHumanPositionCustom oEntity = null;
        if (bCreate) {
            checkUniqueByNoteOrThrow(sNote);
            String sCompanyChain = null;
            if(sLogin != null){
                sCompanyChain = getCompanyChainForLogin(sLogin);
            }
            if(sChain != null){
                sCompanyChain = sChain;
            }
            
            String sPositionName = generatePositionName(sNote, sCompanyChain);
            
            LOG.info("position name: {}", sPositionName);
    
            oEntity = new SubjectHumanPositionCustom();
            oEntity.setsNote(sNote);
            oEntity.setName(sPositionName);
        } else {
            oEntity = oSubjectHumanPositionCustomDao.findByExpected("name", sName);
            oEntity.setsNote(sNote);
        }

        SubjectHumanPositionCustom oPosition = oSubjectHumanPositionCustomDao.saveOrUpdate(oEntity);
        LOG.info("oPosition id is {}", oPosition.getId());
        
        String sAction = null;
        
        if(bCreate){
            SubjectUtils.checkID(oPosition);
            sAction = oServerEntitySyncService.INSERT_ACTION;
        }
        else{
            sAction = oServerEntitySyncService.UPDATE_ACTION;
        }
        
        if(isSync == null || isSync == false){
            String sKeyName = oEntity.getName();
            oServerEntitySyncService.addRecordToServerEntitySync(sKeyName, sAction, "SubjectHumanPositionCustom");
              
            new Thread(new Runnable() {
                public void run() {
                    oServerEntitySyncService.runServerEntitySync("SubjectHumanPositionCustom", sKeyName);
                }
            }).start();
        }
        
        return oPosition;
    }
    
    private String getCompanyChainForLogin(String sLogin) {
        try {
            return oSubjectGroupTreeService.getCompany(sLogin).getsChain();
        } catch (NotFoundException e) {
            throw new RuntimeException("Співробітник '" + sLogin + "' не належить жодної компанії");
        }
    }
    
    private String generatePositionName(String sPositionNote, String sCompanyChain) {
        String sTransliteratedPositionNote = Tool.sTextTranslit(sPositionNote);
        String sName = sTransliteratedPositionNote;
        if(sCompanyChain != null){
            sName = sCompanyChain + sTransliteratedPositionNote;
        }
        
        return sName;
    }
    
    private void checkUniqueByNoteOrThrow(String sNote) {
        boolean bExist = oSubjectHumanPositionCustomDao.findBy("sNote", sNote).isPresent();
        if (bExist) {
            throw new RuntimeException(SubjectHumanPositionCustom.class.getSimpleName() + " with unique '" + sNote + "' already exists");
        }
    }
    
    public SubjectHumanPositionCustom getGroupDepartment() {
        return oSubjectHumanPositionCustomDao.getGroupDepartment().orNull();
    }
    
}
