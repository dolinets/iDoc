package org.igov.model.subject;

import org.igov.model.core.EntityDao;

import java.util.List;

/**
 * User: goodg_000
 * Date: 27.12.2015
 * Time: 18:18
 */
public interface SubjectContactDao extends EntityDao<Long, SubjectContact> {

    List<SubjectContact> findContacts(Subject subject);
    List<SubjectContact> findoMail(SubjectContact oMail);
    List<SubjectContact> findContactsByCriteria(Subject subject, String sMail);
    List<SubjectContact> findContactsBySubjectAndContactType(Subject oSubject, long nID_SubjectContactType);
    List<SubjectContact> findContactsByInSubjectAndContactType(List<Long> anSubject, Long nID_SubjectContactType);
    List<SubjectContact> findContactsByValueAndContactType(String sValue, Long nID_SubjectContactType);
}
