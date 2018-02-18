package org.igov.model.document;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Oleksandr Belichenko
 */
@Repository
public class DocumentStepSubjectSignTypeDaoImpl extends GenericEntityDao<Long, DocumentStepSubjectSignType> implements DocumentStepSubjectSignTypeDao{
    public DocumentStepSubjectSignTypeDaoImpl() {
        super(DocumentStepSubjectSignType.class);
    }
    
}
