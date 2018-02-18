package org.igov.model.registry.nssmc;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alex
 */
@Repository
public class RegistryNSSMCDaoImpl extends GenericEntityDao<Long, RegistryNSSMC> implements RegistryNSSMCDao {

    protected RegistryNSSMCDaoImpl() {
        super(RegistryNSSMC.class);
    }
    
}
