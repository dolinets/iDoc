package org.igov.model.subject;

import com.google.common.base.Optional;
import org.igov.model.core.EntityDao;

import java.util.List;

/**
 * User: goodg_000
 * Date: 27.12.2015
 * Time: 17:52
 */
public interface SubjectContactTypeDao extends EntityDao<Long, SubjectContactType> {

    public SubjectContactType getEmailType();

    public SubjectContactType getPhoneType();

    Optional<SubjectContactType> findByName(String sName);

}
