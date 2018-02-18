package org.igov.model.subject;

import org.igov.model.core.EntityDao;

public interface SubjectStatusDao extends EntityDao<Long, SubjectStatus> {
    SubjectStatus getWorkStatus();
}