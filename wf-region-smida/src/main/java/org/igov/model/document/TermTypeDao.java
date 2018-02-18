package org.igov.model.document;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 * @author alex
 */
public interface TermTypeDao  extends EntityDao<Long, TermType>{
    
    public List<TermType> findAllTermType();
    
}
