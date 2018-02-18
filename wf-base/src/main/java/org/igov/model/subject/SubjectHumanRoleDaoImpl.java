package org.igov.model.subject;

import com.google.common.base.Optional;
import org.igov.model.core.GenericEntityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HS
 */
@Repository
public class SubjectHumanRoleDaoImpl extends GenericEntityDao<Long, SubjectHumanRole> implements SubjectHumanRoleDao {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanRoleDaoImpl.class);
    
    private static final String ROLE_ADMIN = "Admin";
    
    public SubjectHumanRoleDaoImpl() {
        super(SubjectHumanRole.class);
    }
    
    @Override
    public Optional<SubjectHumanRole> getAdminRole() {
        return findBy("name", ROLE_ADMIN);
    }
    
}
