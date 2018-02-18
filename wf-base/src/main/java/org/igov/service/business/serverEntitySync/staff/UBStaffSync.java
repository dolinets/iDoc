package org.igov.service.business.serverEntitySync.staff;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.igov.model.subject.*;
import org.igov.service.business.serverEntitySync.StaffSyncAbstract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("temp") // TODO remove it when set properties up
public class UBStaffSync extends StaffSyncAbstract {

    /**
     * using another data source which has same table schema
     */
    @Autowired
    @Qualifier("ubSessionFactory")
    private SessionFactory sessionFactory;

    @Override
    protected SubjectVO getStaffFromSource() {

        Session session = sessionFactory.openSession();
        session.setDefaultReadOnly(true);
        session.getTransaction().begin();

        SubjectVO oSubjectVO = createSubjectVO(session);

        session.getTransaction().commit();
        session.close();

        return oSubjectVO;
    }

    @SuppressWarnings("unchecked")
    private SubjectVO createSubjectVO(Session session) {
        SubjectVO oSubjectVO = new SubjectVO();
        oSubjectVO.aoSubjectHumanPositionCustoms = (List<SubjectHumanPositionCustom>) session.createCriteria(SubjectHumanPositionCustom.class).list();
        oSubjectVO.aoSubjectContactType = (List<SubjectContactType>) session.createCriteria(SubjectContactType.class).list();
        oSubjectVO.aoSubjectStatus = (List<SubjectStatus>) session.createCriteria(SubjectStatus.class).list();
        oSubjectVO.aoSubject = (List<Subject>) session.createCriteria(Subject.class).list();
        oSubjectVO.aoSubjectHuman = (List<SubjectHuman>) session.createCriteria(SubjectHuman.class).list();
        oSubjectVO.aoSubjectGroup = (List<SubjectGroup>) session.createCriteria(SubjectGroup.class).list();
        oSubjectVO.aoSubjectGroupTree = (List<SubjectGroupTree>) session.createCriteria(SubjectGroupTree.class).list();
        oSubjectVO.aoSubjectContact = (List<SubjectContact>) session.createCriteria(SubjectContact.class).list();
        return oSubjectVO;
    }

}
