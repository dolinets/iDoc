package org.igov.model.subject;

import com.google.common.base.Optional;
import org.igov.model.core.EntityDao;

import java.util.List;

public interface SubjectHumanPositionCustomDao extends EntityDao<Long, SubjectHumanPositionCustom> {
    Optional<SubjectHumanPositionCustom> getGroupDepartment();
    
    List<SubjectHumanPositionCustom> findAllByStartLikeName(String sName);
    List<SubjectHumanPositionCustom> findAllByStartLikeNameAndAroundLikeNote(String sName, String sNote);
}
