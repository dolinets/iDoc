package org.igov.service.business.document;

import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.model.document.DocumentStepSubjectRightDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author idenysenko
 */
@Service
public class DocumentStepSubjectRightService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStepSubjectRightService.class);

    @Autowired private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;

    /**
     * Найти все не подписанные права. Если sKey_Step == null поиск будет по всем степам (или только по заданногому),
     * bNeedECPCheck == true -  проверять так же подпись ЭЦП для каждого права. Подписанным DocumentStepSubjectRight
     * считается когда sDate != null или и sDate != null, и sDateECP != null когда нужно проверять и подпись ЭЦП.
     *
     * @param snID_Process_Activiti ид процесса
     * @param sKey_Step название степа
     * @param bNeedECPCheck проверять подпись ЭЦП или нет
     * @return лист найденных не подписанных прав
     */
    public List<DocumentStepSubjectRight> getUnsignedRights(String snID_Process_Activiti, String sKey_Step, boolean bNeedECPCheck) {
        return oDocumentStepSubjectRightDao.findUnsignedRights(snID_Process_Activiti, sKey_Step, bNeedECPCheck);
    }

    /**
     * Получить все DocumentStepSubjectRight по персонализированной группе и ид процесса.
     *
     * @param sKey_GroupPostfix персонализированная группа (логин)
     * @param snID_Process_Activiti ид процесса
     * @return лист DocumentStepSubjectRight из бд
     */
    public List<DocumentStepSubjectRight> getRightsByProcessAndGroup(String snID_Process_Activiti, String sKey_GroupPostfix) {
        return oDocumentStepSubjectRightDao.getRightsByProcessAndGroup(snID_Process_Activiti, sKey_GroupPostfix);
    }

}
