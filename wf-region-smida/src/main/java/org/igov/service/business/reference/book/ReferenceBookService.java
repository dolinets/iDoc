package org.igov.service.business.reference.book;

import java.util.List;
import org.igov.model.action.event.ActionEventType;
import org.igov.model.action.event.ActionEventTypeDao;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.dictionary.DictionaryDao;
import org.igov.model.document.DocumentStatutoryState;
import org.igov.model.document.DocumentStatutoryStateDao;
import org.igov.model.document.TermType;
import org.igov.model.document.TermTypeDao;
import org.igov.model.registry.nssmc.RegistryNSSMC;
import org.igov.model.registry.nssmc.RegistryNSSMCDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author alex
 */
@Component("referenceBookService")
public class ReferenceBookService {
    
    @Autowired
    private DictionaryDao dictionaryDao;
    
    @Autowired
    private ActionEventTypeDao actionEventTypeDao;
       
    @Autowired
    private TermTypeDao termTypeDao;

    @Autowired
    private DocumentStatutoryStateDao documentStatutoryStateDao;
    
    @Autowired
    private RegistryNSSMCDao registryNSSMCDao;
    
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceBookService.class);
    
    /**
     * Get all Dictionary mapped by nID_DictionaryType
     *
     * @param nID_DictionaryType
     * @return List<Dictionary>
     */
    public List<Dictionary> getListDictionary(Long nID_DictionaryType){
        LOG.info(String.format("find Dictionary entities with nID_DictionaryType=%s", nID_DictionaryType));
        return dictionaryDao.findByDictionaryType(nID_DictionaryType);
    }
    
    /**
     * Get all ActionEventType mapped by nID_ActionEventTypeGroup
     *
     * @param nID_ActionEventTypeGroup
     * @return List<ActionEventType>
     */
    public List<ActionEventType> getListActionEventType(Long nID_ActionEventTypeGroup){
        LOG.info(String.format("find ActionEventType entities with nID_ActionEventTypeGroup=%s", nID_ActionEventTypeGroup));
        return actionEventTypeDao.findByActionEventTypeGroup(nID_ActionEventTypeGroup);
    }
    
    /**
     * Get ActionEventType by nID
     *
     * @param nID
     * @return ActionEventType
     */
    public ActionEventType getActionEventType(Long nID){
        LOG.info(String.format("find ActionEventType entity with nID=%s", nID));
        return actionEventTypeDao.findByIdExpected(nID);
    }
    
    /**
     * Get all TermType
     *
     * @return List<TermType>
     */
    public List<TermType> getListTermType(){
        LOG.info(String.format("find all TermType entities"));
        return termTypeDao.findAll();
    }
    
    /**
     * Get DocumentStatutoryState mapped by nID
     *
     * @param nID
     * @return DocumentStatutoryState
     */
    public DocumentStatutoryState getDocumentStatutoryState(Long nID){
        LOG.info(String.format("find DocumentStatutoryState entity with nID=%s", nID));
        return documentStatutoryStateDao.findByIdExpected(nID);
    }
    
    /**
     * Get all TermType
     *
     * @return List<RegistryNSSMC>
     */
    public List<RegistryNSSMC> getListRegistryNSSMC(){
        LOG.info(String.format("find all RegistryNSSMC entities"));
        return registryNSSMCDao.findAll();
    }
}
