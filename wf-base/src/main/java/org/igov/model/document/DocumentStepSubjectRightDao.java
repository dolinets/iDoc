package org.igov.model.document;

import org.igov.model.core.EntityDao;

import java.util.List;
import java.util.Set;

public interface DocumentStepSubjectRightDao extends EntityDao<Long, DocumentStepSubjectRight> {

    /**
     * Найти всех участников процесса. дефолтные права не берутся в расчет
     *
     * @param snID_Process ид процесса
     * @return все учатники процесса
     */
    public Set<String> findDocumentParticipant(String snID_Process);

    List<DocumentStepSubjectRight> findUnsignedRights(String snID_Process, String sKey_Step, boolean bNeedECPCheck);

    List<DocumentStepSubjectRight> getRightsByProcessAndGroup(String snID_process_activiti, String sKey_groupPostfix);
}
