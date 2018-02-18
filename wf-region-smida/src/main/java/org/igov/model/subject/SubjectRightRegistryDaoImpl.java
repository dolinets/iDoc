package org.igov.model.subject;

import org.igov.model.core.GenericEntityDao;
import org.igov.model.registry.nssmc.RegistryNSSMC;
import org.igov.model.registry.nssmc.RegistryNSSMCDao;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class SubjectRightRegistryDaoImpl extends GenericEntityDao<Long, SubjectRightRegistry> implements SubjectRightRegistryDao {

    public SubjectRightRegistryDaoImpl() {
        super(SubjectRightRegistry.class);
    }

}
