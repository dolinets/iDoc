package org.igov.model.document;

import org.igov.model.core.EntityDao;

public interface DocumentStepSubjectRightFieldDao extends EntityDao<Long, DocumentStepSubjectRightField> {

   public void deleteBySqlQuery(Long nID);

}
