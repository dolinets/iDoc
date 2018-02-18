package org.igov.model.dictionary;

import java.util.List;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 * @author alex
 */
@Repository
public class DictionaryDaoImpl extends GenericEntityDao<Long, Dictionary> implements DictionaryDao {

    protected DictionaryDaoImpl() {
        super(Dictionary.class);
    }

    @Override
    public List<Dictionary> findByDictionaryType(Long nID_DictionaryType) {
        return findAllBy("oDictionaryType.id", nID_DictionaryType);
    }
}
