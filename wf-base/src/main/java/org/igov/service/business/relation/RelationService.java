package org.igov.service.business.relation;

import com.google.common.base.Optional;
import java.util.List;
import java.util.ArrayList;
import org.igov.model.action.vo.Attribute_VO;
import org.igov.model.action.vo.Relation_VO;
import org.igov.model.relation.ObjectGroup;
import org.igov.model.relation.ObjectGroupAttribute;
import org.igov.model.relation.ObjectGroupDao;
import org.igov.model.relation.Relation;
import org.igov.model.relation.Relation_ObjectGroupDao;
import org.igov.model.relation.RelationClassDao;
import org.igov.model.relation.RelationDao;
import org.igov.model.relation.Relation_ObjectGroup;
import org.igov.model.subject.Subject;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.subject.SubjectGroupTreeService;
import org.igov.service.business.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * @author Kovilin
 */
@Service
@Component("relationService")
public class RelationService {
    
    private static final Logger LOG = LoggerFactory.getLogger(RelationService.class);
    
    @Autowired
    private RelationDao oRelationDao;

    @Autowired
    private RelationClassDao oRelationClassDao;

    @Autowired
    private ObjectGroupDao oObjectGroupDao;

    @Autowired
    private Relation_ObjectGroupDao oRelation_ObjectGroupDao;

    @Autowired
    private SubjectService oSubjectService;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;

    public List<Relation_VO> getRelations(String sID_Relation, Long nID_Parent, String sFindChild) {

        List<Relation_VO> aRelation_VO = new ArrayList<>();

        Relation oRelation = oRelationDao.findByExpected("sID", sID_Relation);
        //Long nID_RelationClass = oRelation.getnID_RelationClass();

        //if(oRelationClassDao.findByExpected("id", nID_RelationClass).getsClass().equals("ObjectGroup")){
        if (oRelation.getoRelationClass().getsClass().equals("ObjectGroup")) {
            List<Relation_ObjectGroup> aRelation_ObjectGroup = new ArrayList<>();

            aRelation_ObjectGroup.addAll(oRelation_ObjectGroupDao.getRelation_ObjectGroups(oRelation.getId(), nID_Parent));

            for (Relation_ObjectGroup oRelation_ObjectGroup : aRelation_ObjectGroup) {
                ObjectGroup oObjectGroup = oRelation_ObjectGroup.getoObjectGroup();
                //oObjectGroupDao.findByExpected("id", oRelation_ObjectGroup.getnID_ObjectGroup_Child());
                if (sFindChild == null || oObjectGroup.getsName().contains(sFindChild)) {
                    Relation_VO oRelation_VO = new Relation_VO();
                    oRelation_VO.setnID(oObjectGroup.getId());
                    oRelation_VO.setsID_Private_Source(oObjectGroup.getsID_Private_Source());
                    oRelation_VO.setsName(oObjectGroup.getsName());
                    
                    List<Attribute_VO> aAttribute_VO = new ArrayList<>();
                    for(ObjectGroupAttribute oObjectGroupAttribute : oObjectGroup.getaObjectGroupAttribute()){
                        Attribute_VO oAttribute_VO = new Attribute_VO();
                        oAttribute_VO.setoAttributeObject(oObjectGroupAttribute.getoAttributeObject());
                        oAttribute_VO.setsValue(oObjectGroupAttribute.getsValue());
                        aAttribute_VO.add(oAttribute_VO);
                    }
                    oRelation_VO.setaAttribute_VO(aAttribute_VO);
                    aRelation_VO.add(oRelation_VO);
                }
            }
        }

        return aRelation_VO;
    }

    public ObjectGroup getObjectGroupBySubject_Source(String sID_Private_Source) {
        Optional<ObjectGroup> objectGroupBySubject_Source = oObjectGroupDao.findBy("sID_Private_Source", sID_Private_Source);
        if (objectGroupBySubject_Source.isPresent()) {
            return objectGroupBySubject_Source.get();
        } else {
            LOG.info("objectGroupBySubject_Source with sID_Private_Source - {} wasn't found", sID_Private_Source);
            return null;
        }
    }
    
    /*
    public ObjectGroup getObjectGroupBySubject_Source(String sID_Private_Source) {
        return oObjectGroupDao.findByExpected("sID_Private_Source", sID_Private_Source);
    }*/

    public ObjectGroup getCompany(String sLogin) {
        LOG.info("getObjectGroup started");
        List<SubjectGroup> aSubjectGroup = oSubjectGroupTreeService.getSubjectGroupsTreeUp(sLogin, "Organ", 0L);
        LOG.info("aSubjectGroup {}", aSubjectGroup);
        if (aSubjectGroup == null || aSubjectGroup.isEmpty()) {
            throw new RuntimeException("Can't find any Subject by login: " + sLogin);
        } else {
            return getObjectGroupBySubject_Source(aSubjectGroup.get(0).getoSubject().getId().toString());
        }
    }

    public ObjectGroup getObjectGroupParent(String sLoginChild) {

        //List<ObjectGroup> aObjectGroup_Parent = new ArrayList<>();
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginChild);
        ObjectGroup oObjectGroup_Child = null;
        
        if(oSubjectGroup != null){
            oObjectGroup_Child = getObjectGroupBySubject_Source(oSubjectGroup.getId().toString());
        }
        else{
            throw new RuntimeException("Can't find any SubjectGroup for login " + sLoginChild);
        }
         
        ObjectGroup oObjectGroup_Parent = null;
        if (oObjectGroup_Child == null) {
            throw new RuntimeException("Can't find any ObjectGroup child for login " + sLoginChild);
        }else{
            
            Relation_ObjectGroup oRelation_ObjectGroup
                    = oRelation_ObjectGroupDao.findByExpected("oObjectGroup_Child", oObjectGroup_Child);

            if (oRelation_ObjectGroup == null) {
                    throw new RuntimeException("Can't find any oRelation_ObjectGroup for ObjectGroup id " + oObjectGroup_Child.getId());
            }else{
                oObjectGroup_Parent = oRelation_ObjectGroup.getoObjectGroup_Parent();
            }
        }

        //for (ObjectGroup oObjectGroup_Child : aObjectGroup_Child) {
        
        //}

        return oObjectGroup_Parent;
    }
    
    public ObjectGroup getObjectGroupParentOrNull(String sLoginChild) {

        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginChild);
        ObjectGroup oObjectGroup_Child = null;

        if (oSubjectGroup != null) {
            oObjectGroup_Child = getObjectGroupBySubject_Source(oSubjectGroup.getId().toString());
        }

        ObjectGroup oObjectGroup_Parent = null;
        if (oObjectGroup_Child != null) {

            Relation_ObjectGroup oRelation_ObjectGroup
                    = oRelation_ObjectGroupDao.findByExpected("oObjectGroup_Child", oObjectGroup_Child);

            if (oRelation_ObjectGroup != null) {
                oObjectGroup_Parent = oRelation_ObjectGroup.getoObjectGroup_Parent();
            }
        }
        return oObjectGroup_Parent;
    }
}
