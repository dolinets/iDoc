package org.igov.model.subject;

import org.igov.model.core.GenericEntityDao;

import org.springframework.stereotype.Repository;

@Repository
public class SubjectStatusDaoImpl extends GenericEntityDao<Long, SubjectStatus> implements SubjectStatusDao {

    private static final String WORK_STATUS = "Working";

    public SubjectStatusDaoImpl() {
        super(SubjectStatus.class);
    }

    public SubjectStatus getWorkStatus() {
        return findBy("sName", WORK_STATUS).orNull();
    }

}