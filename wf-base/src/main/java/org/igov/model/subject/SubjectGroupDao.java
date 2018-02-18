package org.igov.model.subject;

import org.igov.model.core.EntityDao;

import java.util.List;

/**
 *
 * @author idenysenko
 */
public interface SubjectGroupDao extends EntityDao<Long, SubjectGroup>   {

    SubjectGroup findSubjectById(Long nID_Subject);
    
    List<SubjectGroup> findHumansInCompany(String sChain);
    List<SubjectGroup> findDepartsInCompany(String sChain);

    List<SubjectGroup> findHumansInCompanyByLikeNameOrLogin(String sChain, String sName);
    List<SubjectGroup> findDepartsInCompanyByLikeName(String sChain, String sName);
    
    List<SubjectGroup> findAllByLikeNameOrLogin(String sNameOrLogin);
    List<SubjectGroup> findAllByLikeLogin(String sLogin);
    
    List<String> findLoginDubles();

}
