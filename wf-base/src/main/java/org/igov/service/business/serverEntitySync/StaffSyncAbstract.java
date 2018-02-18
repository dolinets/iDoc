package org.igov.service.business.serverEntitySync;

import org.hibernate.SessionFactory;
import org.igov.model.core.NamedEntity;
import org.igov.model.subject.*;
import org.igov.service.business.serverEntitySync.staff.SubjectVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

public abstract class StaffSyncAbstract {

    private final static Logger LOG = LoggerFactory.getLogger(StaffSyncAbstract.class);

    @Autowired
    private SubjectHumanPositionCustomDao oSubjectHumanPositionCustomDao;
    @Autowired
    private SubjectStatusDao oSubjectStatusDao;
    @Autowired
    private SubjectDao oSubjectDao;
    @Autowired
    private SubjectHumanDao oSubjectHumanDao;
    @Autowired
    private SubjectContactTypeDao oSubjectContactTypeDao;
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    @Autowired
    private SubjectGroupTreeDao oSubjectGroupTreeDao;
    @Autowired
    private SubjectContactDao oSubjectContactDao;

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory oSessionFactory;

    protected abstract SubjectVO getStaffFromSource();

    public void syncStaffAll() {
        SubjectVO oSubjectVO = getStaffFromSource();
        syncWithDb(oSubjectVO);
        LOG.info("staff synchronized successfully");
    }

    private void syncWithDb(SubjectVO subjectVO) {
        deleteSubjectGroupTree(subjectVO.getAoSubjectGroupTree());
        deleteSubjectGroup(subjectVO.getAoSubjectGroup());
        deleteSubjectHuman(subjectVO.getAoSubjectHuman());
        deleteSubjectContact(subjectVO.getAoSubjectContact());
        deleteSubjectContactType(subjectVO.getAoSubjectContactType());
        deleteSubject(subjectVO.getAoSubject());
        deleteSubjectStatus(subjectVO.getAoSubjectStatus());
        deleteSubjectPositions(subjectVO.getAoSubjectHumanPositionCustoms());

        oSessionFactory.getCurrentSession().flush();

        updatePositions(subjectVO.getAoSubjectHumanPositionCustoms());
        updateContactType(subjectVO.getAoSubjectContactType());
        updateStatus(subjectVO.getAoSubjectStatus());
        updateSubject(subjectVO.getAoSubject());
        updateSubjectContact(subjectVO.getAoSubjectContact());
        updateSubjectHuman(subjectVO.getAoSubjectHuman());
        updateSubjectGroup(subjectVO.getAoSubjectGroup());
        updateSubjectGroupTree(subjectVO.getAoSubjectGroupTree());
    }

    private void deleteSubjectStatus(List<SubjectStatus> aoStatus) {
        List<SubjectStatus> aoCurrentStatus = oSubjectStatusDao.findAll();

        Set<String> asStatus = aoStatus
                .stream()
                .map(SubjectStatus::getsName)
                .collect(Collectors.toSet());

        List<SubjectStatus> aoSubjectStatusToDelete = aoCurrentStatus
                .stream()
                .filter(e -> !asStatus.contains(e.getsName()))
                .collect(Collectors.toList());
        LOG.info("SubjectStatus to delete: {}", aoSubjectStatusToDelete.size());

        if (aoSubjectStatusToDelete.size() > 0) {
            oSubjectStatusDao.delete(aoSubjectStatusToDelete);
        }
    }

    private void deleteSubject(List<Subject> aoSubject) {
        List<Subject> aoCurrentSubject = oSubjectDao.findAll();

        Set<String> asSubject = aoSubject
                .stream()
                .map(Subject::getsLabel)
                .collect(Collectors.toSet());

        List<Subject> aoSubjectToDelete = aoCurrentSubject
                .stream()
                .filter(e -> !asSubject.contains(e.getsLabel()))
                .collect(Collectors.toList());
        LOG.info("Subject to delete: {}", aoSubjectToDelete.size());

        if (aoSubjectToDelete.size() > 0) {
            oSubjectDao.delete(aoSubjectToDelete);
        }
    }

    private void deleteSubjectContactType(List<SubjectContactType> aoContactType) {
        List<SubjectContactType> aoCurrentContactType = oSubjectContactTypeDao.findAll();

        Set<String> asContactType = aoContactType
                .stream()
                .map(SubjectContactType::getsName_EN)
                .collect(Collectors.toSet());

        List<SubjectContactType> aoSubjectContactTypeToDelete = aoCurrentContactType
                .stream()
                .filter(e -> !asContactType.contains(e.getsName_EN()))
                .collect(Collectors.toList());
        LOG.info("SubjectContactType to delete: {}", aoSubjectContactTypeToDelete.size());

        if (aoSubjectContactTypeToDelete.size() > 0) {
            oSubjectContactTypeDao.delete(aoSubjectContactTypeToDelete);
        }
    }

    private void deleteSubjectContact(List<SubjectContact> aoSubjectContact) {
        List<SubjectContact> aoCurrentSubjectContact = oSubjectContactDao.findAll();

        Set<String> asSubjectContact = aoSubjectContact
                .stream()
                .map(contact -> contact.getSubjectContactType().getsName_EN().concat(contact.getsValue()))
                .collect(Collectors.toSet());

        List<SubjectContact> aoSubjectContactToDelete = aoCurrentSubjectContact
                .stream()
                .filter(e -> !asSubjectContact.contains(e.getSubjectContactType().getsName_EN().concat(e.getsValue())))
                .collect(Collectors.toList());
        LOG.info("SubjectContact to delete: {}", aoSubjectContactToDelete.size());

        aoSubjectContactToDelete.forEach(this::deleteReferences);

        if (aoSubjectContactToDelete.size() > 0) {
            oSubjectContactDao.delete(aoSubjectContactToDelete);
        }
    }

    private void deleteReferences(SubjectContact oContact) {
        SubjectContactType oEmailType = oSubjectContactTypeDao.getEmailType();
        if (oEmailType.equals(oContact.getSubjectContactType())) {
            SubjectHuman oSubjectHuman = oSubjectHumanDao.findBy("defaultEmail", oContact).orNull();
            if (oSubjectHuman != null) {
                oSubjectHuman.setDefaultEmail(null);
            }
        } else {
            SubjectHuman oSubjectHuman = oSubjectHumanDao.findBy("defaultPhone", oContact).orNull();
            if (oSubjectHuman != null) {
                oSubjectHuman.setDefaultPhone(null);
            }
        }
    }

    private void deleteSubjectHuman(List<SubjectHuman> aoSubjectHuman) {
        List<SubjectHuman> aoCurrentSubjectHuman = oSubjectHumanDao.findAll();

        Set<String> asSubjectHuman = aoSubjectHuman
                .stream()
                .map(human -> human.getoSubject().getsLabel())
                .collect(Collectors.toSet());

        List<SubjectHuman> aoSubjectHumanToDelete = aoCurrentSubjectHuman
                .stream()
                .filter(e -> !asSubjectHuman.contains(e.getoSubject().getsLabel()))
                .collect(Collectors.toList());
        LOG.info("SubjectHuman to delete: {}", aoSubjectHumanToDelete.size());

        if (aoSubjectHumanToDelete.size() > 0) {
            oSubjectHumanDao.delete(aoSubjectHumanToDelete);
        }
    }

    private void deleteSubjectGroupTree(List<SubjectGroupTree> aoSubjectGroupTree) {
        oSubjectGroupTreeDao.deleteAll();
    }

    private void deleteSubjectGroup(List<SubjectGroup> aoSubjectGroup) {
        List<SubjectGroup> aoCurrentSubjectGroup = oSubjectGroupDao.findAll();

        Set<String> asSubjectGroup = aoSubjectGroup
                .stream()
                .map(SubjectGroup::getsID_Group_Activiti)
                .collect(Collectors.toSet());

        List<SubjectGroup> aoSubjectGroupToDelete = aoCurrentSubjectGroup
                .stream()
                .filter(e -> !asSubjectGroup.contains(e.getsID_Group_Activiti()))
                .collect(Collectors.toList());
        LOG.info("SubjectGroup to delete: {}", aoSubjectGroupToDelete.size());

        if (aoSubjectGroupToDelete.size() > 0) {
            oSubjectGroupDao.delete(aoSubjectGroupToDelete);
        }
    }

    private void deleteSubjectPositions(List<SubjectHumanPositionCustom> aoPosition) {
        List<SubjectHumanPositionCustom> aoCurrentPosition = oSubjectHumanPositionCustomDao.findAll();

        Set<String> asPosition = aoPosition
                .stream()
                .map(SubjectHumanPositionCustom::getName)
                .collect(Collectors.toSet());

        List<SubjectHumanPositionCustom> aoPositionToDelete = aoCurrentPosition
                .stream()
                .filter(e -> !asPosition.contains(e.getName()))
                .collect(Collectors.toList());

        if (aoPositionToDelete.size() > 0) {
            oSubjectHumanPositionCustomDao.delete(aoPositionToDelete);
        }
    }

    private void updateStatus(List<SubjectStatus> aoStatus) {
        Map<String, SubjectStatus> mCurrentStatus = oSubjectStatusDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(SubjectStatus::getsName, s -> s));

        for (SubjectStatus oStatus : aoStatus) {
            SubjectStatus oCurrentStatus = mCurrentStatus.get(oStatus.getsName());
            if (oCurrentStatus == null) {
                oStatus.setId(null);
                mCurrentStatus.put(oStatus.getsName(), oStatus);
            } else {
                oCurrentStatus.setsNote(oStatus.getsNote());
            }
        }

        List<SubjectStatus> aoSubjectStatusToSave = mCurrentStatus
                .values()
                .stream()
                .collect(Collectors.toList());
        LOG.info("SubjectStatus to save/update: {}", aoSubjectStatusToSave.size());

        oSubjectStatusDao.saveOrUpdate(aoSubjectStatusToSave);
    }

    private void updateContactType(List<SubjectContactType> aoContactType) {
        Map<String, SubjectContactType> mCurrentContactType = oSubjectContactTypeDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(SubjectContactType::getsName_EN, s -> s));

        for (SubjectContactType oContactType : aoContactType) {
            SubjectContactType oCurrentContactType = mCurrentContactType.get(oContactType.getsName_EN());
            if (oCurrentContactType == null) {
                oContactType.setId(null);
                mCurrentContactType.put(oContactType.getsName_EN(), oContactType);
            } else {
                oCurrentContactType.setsName_RU(oContactType.getsName_RU());
                oCurrentContactType.setsName_UA(oContactType.getsName_UA());
            }
        }

        List<SubjectContactType> aoContactTypeToSave = mCurrentContactType
                .values()
                .stream()
                .collect(Collectors.toList());

        oSubjectContactTypeDao.saveOrUpdate(aoContactTypeToSave);
    }

    private void updatePositions(List<SubjectHumanPositionCustom> aoPosition) {
        Map<String, SubjectHumanPositionCustom> mCurrentPosition = oSubjectHumanPositionCustomDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(NamedEntity::getName, p -> p));

        for (SubjectHumanPositionCustom oPosition : aoPosition) {
            SubjectHumanPositionCustom oCurrentPosition = mCurrentPosition.get(oPosition.getName());
            if (oCurrentPosition == null) {
                oPosition.setId(null);
                mCurrentPosition.put(oPosition.getName(), oPosition);
            } else {
                oCurrentPosition.setsNote(oPosition.getsNote());
            }
        }

        List<SubjectHumanPositionCustom> aoPositionToSave = mCurrentPosition
                .values()
                .stream()
                .collect(Collectors.toList());
        LOG.info("SubjectHumanPositionCustom to save/update: {}", aoPositionToSave.size());

        oSubjectHumanPositionCustomDao.saveOrUpdate(aoPositionToSave);
    }

    private void updateSubject(List<Subject> aoSubject) {
        Map<String, Subject> mCurrentSubject = oSubjectDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(Subject::getsLabel, e -> e, (l, l2) -> l));

        for (Subject oSubject : aoSubject) {
            Subject oCurrentSubject = mCurrentSubject.get(oSubject.getsLabel());
            if (oCurrentSubject == null) {
                oSubject.setId(null);
                mCurrentSubject.put(oSubject.getsLabel(), oSubject);
            } else {
                updateSubject(oSubject, oCurrentSubject);
            }
        }

        List<Subject> aoSubjectToSave = mCurrentSubject
                .values()
                .stream()
                .collect(Collectors.toList());
        LOG.info("Subject to save/update: {}", aoSubjectToSave.size());

        oSubjectDao.saveOrUpdate(aoSubjectToSave);
    }

    private void updateSubjectContact(List<SubjectContact> aoSubjectContact) {
        Set<SubjectContact> aoCurrentContact = oSubjectContactDao
                .findAll()
                .stream()
                .collect(Collectors.toSet());

        for (SubjectContact oContact : aoSubjectContact) {
            if (!aoCurrentContact.contains(oContact)) {
                oContact.setId(null);
                Subject oSubject = oContact.getSubject();
                if (oSubject != null) {
                    Subject oCurrentSubject = oSubjectDao.findBy("sLabel", oSubject.getsLabel()).orNull();
                    oContact.setSubject(oCurrentSubject);
                }
                SubjectContactType oSubjectContactType = oContact.getSubjectContactType();
                if (oSubjectContactType != null) {
                    SubjectContactType oCurrentContactType = oSubjectContactTypeDao.findBy("sName_EN", oSubjectContactType.getsName_EN()).orNull();
                    oContact.setSubjectContactType(oCurrentContactType);
                }
                aoCurrentContact.add(oContact);
            }
        }

        oSubjectContactDao.saveOrUpdate(aoCurrentContact.stream().collect(Collectors.toList()));
    }

    private void updateSubjectHuman(List<SubjectHuman> aoSubjectHuman) {
        Map<String, SubjectHuman> mCurrentHuman = oSubjectHumanDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(sh -> sh.getoSubject().getsLabel(), e -> e, (l, l2) -> l));

        for (SubjectHuman oSubjectHuman : aoSubjectHuman) {
            SubjectHuman oCurrentHuman = mCurrentHuman.get(oSubjectHuman.getoSubject().getsLabel());
            if (oCurrentHuman == null) {
                oSubjectHuman.setId(null);
                Subject oSubject = oSubjectHuman.getoSubject();
                if (oSubject != null) {
                    Subject oCurrentSubject = oSubjectDao.findBy("sLabel", oSubject.getsLabel()).orNull();
                    oSubjectHuman.setoSubject(oCurrentSubject);
                    mCurrentHuman.put(oSubjectHuman.getoSubject().getsLabel(), oSubjectHuman);
                }
            } else {
                updateSubjectHuman(oSubjectHuman, oCurrentHuman);
            }
        }

        List<SubjectHuman> aoSubjectHumanToSave = mCurrentHuman
                .values()
                .stream()
                .collect(Collectors.toList());
        LOG.info("SubjectHuman to save/update: {}", aoSubjectHumanToSave.size());

        oSubjectHumanDao.saveOrUpdate(aoSubjectHumanToSave);
    }

    private void updateSubjectGroup(List<SubjectGroup> aoSubjectGroup) {
        Map<String, SubjectGroup> mCurrentGroup = oSubjectGroupDao
                .findAll()
                .stream()
                .collect(Collectors.toMap(SubjectGroup::getsID_Group_Activiti, e -> e, (l, l2) -> l));

        for (SubjectGroup oSubjectGroup : aoSubjectGroup) {
            SubjectGroup oCurrentGroup = mCurrentGroup.get(oSubjectGroup.getsID_Group_Activiti());
            if (oCurrentGroup == null) {
                oSubjectGroup.setId(null);
                Subject oSubject = oSubjectGroup.getoSubject();
                if (oSubject != null) {
                    Subject oCurrentSubject = oSubjectDao.findBy("sLabel", oSubject.getsLabel()).orNull();
                    oSubjectGroup.setoSubject(oCurrentSubject);

                    SubjectHumanPositionCustom oCurrentPosition = oSubjectHumanPositionCustomDao.findBy("name", oSubjectGroup.getoSubjectHumanPositionCustom().getName()).orNull();
                    oSubjectGroup.setoSubjectHumanPositionCustom(oCurrentPosition);

                    mCurrentGroup.put(oSubjectGroup.getsID_Group_Activiti(), oSubjectGroup);
                }
            } else {
                updateSubjectGroup(oSubjectGroup, oCurrentGroup);
            }
        }

        List<SubjectGroup> aoSubjectGroupToSave = mCurrentGroup
                .values()
                .stream()
                .collect(Collectors.toList());
        LOG.info("SubjectGroup to save/update: {}", aoSubjectGroupToSave.size());

        oSubjectGroupDao.saveOrUpdate(aoSubjectGroupToSave);
    }

    private void updateSubjectGroupTree(List<SubjectGroupTree> aoSubjectGroupTree) {
        for (SubjectGroupTree oSubjectGroupTree : aoSubjectGroupTree) {
            String sID_Child = oSubjectGroupTree.getoSubjectGroup_Child().getsID_Group_Activiti();
            String sID_Parent = oSubjectGroupTree.getoSubjectGroup_Parent().getsID_Group_Activiti();
            SubjectGroup oSubjectGroupChild = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Child).orNull();
            if (oSubjectGroupChild != null) {
                SubjectGroup oSubjectGroupParent = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Parent).orNull();
                if (oSubjectGroupParent != null) {
                    oSubjectGroupTreeDao.saveOrUpdate(oSubjectGroupChild, oSubjectGroupParent);
                }
            }
        }
    }

    private void updateSubject(Subject oSource, Subject oDest) {
        oDest.setsLabelShort(oSource.getsLabelShort());
        SubjectStatus oSubjectStatus = oSource.getoSubjectStatus();
        if (oSubjectStatus != null) {
            oDest.setoSubjectStatus(oSubjectStatusDao.findBy("sName", oSubjectStatus.getsName()).orNull());
        }
        oDest.setsID(oSource.getsID());
    }

    private void updateSubjectHuman(SubjectHuman oSource, SubjectHuman oDest) {
        oDest.setsSB(oSource.getsSB());
        oDest.setsINN(oSource.getsINN());
        oDest.setsPassportSeria(oSource.getsPassportSeria());
        oDest.setsPassportNumber(oSource.getsPassportNumber());
        oDest.setsFamily(oSource.getsFamily());
        oDest.setName(oSource.getName());
        oDest.setsSurname(oSource.getsSurname());
        oDest.setsDateBirth(oSource.getsDateBirth());
        oDest.setsTabel(oSource.getsTabel());

        SubjectContact oEmail = oSource.getDefaultEmail();
        if (oEmail != null) {
            SubjectContactType oContactType = oSubjectContactTypeDao
                    .findBy("sName_EN", oEmail.getSubjectContactType().getsName_EN())
                    .orNull();
            if (oContactType != null) {
                oSubjectContactDao.findContactsByValueAndContactType(oEmail.getsValue(), oContactType.getId())
                        .stream()
                        .findAny()
                        .ifPresent(oDest::setDefaultEmail);
            }
        }
        SubjectContact oPhone = oSource.getDefaultPhone();
        if (oPhone != null) {
            SubjectContactType oContactType = oSubjectContactTypeDao
                    .findBy("sName_EN", oPhone.getSubjectContactType().getsName_EN())
                    .orNull();
            if (oContactType != null) {
                oSubjectContactDao.findContactsByValueAndContactType(oEmail.getsValue(), oContactType.getId())
                        .stream()
                        .findAny()
                        .ifPresent(oDest::setDefaultPhone);
            }
        }
    }

    private void updateSubjectGroup(SubjectGroup oSource, SubjectGroup oDest) {
        oDest.setName(oSource.getName());
        oDest.setsChain(oSource.getsChain());

        Subject oSubject = oSource.getoSubject();
        if (oSubject != null) {
            oDest.setoSubject(oSubjectDao.findBy("sLabel", oSubject.getsLabel()).orNull());
        }

        SubjectHumanPositionCustom oSubjectHumanPositionCustom = oSource.getoSubjectHumanPositionCustom();
        if (oSubjectHumanPositionCustom != null) {
            oDest.setoSubjectHumanPositionCustom(oSubjectHumanPositionCustomDao.findBy("name", oSubjectHumanPositionCustom.getName()).orNull());
        }
    }

}
