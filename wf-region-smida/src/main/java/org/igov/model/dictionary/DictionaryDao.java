package org.igov.model.dictionary;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 * @author alex
 */
public interface DictionaryDao extends EntityDao<Long, Dictionary>{
    
    public List<Dictionary> findByDictionaryType(Long nID_DictionaryType);
        
}
