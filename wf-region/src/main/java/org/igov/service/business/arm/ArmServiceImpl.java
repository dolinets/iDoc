package org.igov.service.business.arm;

import com.google.common.base.Optional;
import java.util.List;

import org.igov.model.arm.ArmDao;
import org.igov.model.arm.DboTkModel;
import org.igov.model.arm.DboTkResult;
import org.igov.model.relation.ObjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.service.business.relation.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component("armService")
@Service
public class ArmServiceImpl implements ArmService {
    
    private final static Logger LOG = LoggerFactory.getLogger(ArmServiceImpl.class);

    @Autowired
    private ArmDao armDao;
    
    @Autowired
    private SubjectGroupDao SubjectGroupDao;
    
    @Autowired
    private RelationService oRelationService;

    @Override
    public List<DboTkModel> getDboTkByOutNumber(String outNumber) {
        return armDao.getDboTkByOutNumber(outNumber);
    }

    @Override
    public List<DboTkModel> getDboTkByNumber441(Integer Number441) {
        return armDao.getDboTkByNumber441(Number441);
    }

    @Override
    public DboTkResult createDboTk(DboTkModel dboTkModel) {
        return armDao.createDboTk(dboTkModel);

    }

    @Override
    public DboTkResult updateDboTk(DboTkModel dboTkModel) {
        return armDao.updateDboTk(dboTkModel);

    }

    @Override
    public DboTkResult updateDboTkByExpert(DboTkModel dboTkModel) {
        return armDao.updateDboTkByExpert(dboTkModel);
    }

    @Override
    public DboTkResult updateDboTkByAnswer(DboTkModel dboTkModel) {
        return armDao.updateDboTkByAnswer(dboTkModel);
    }

    @Override
    public Integer getMaxValue() {
        return armDao.getMaxValue();
    }

    @Override
    public Integer getMaxValue442() {
        return armDao.getMaxValue442();
    }

    @Override
    public Integer getNumber441(String sID_Order, String sExecutor) {
        
        LOG.info("getNumber441: sID_Order - {}, sExecutor - {}", sID_Order, sExecutor);

        Optional<SubjectGroup> oSubjectGroup = SubjectGroupDao.findBy("sID_Group_Activiti", sExecutor);
        Long nId = oSubjectGroup.get().getoSubject().getId();

        String sName = " ";

        ObjectGroup aObjectGroup = oRelationService.getObjectGroupBySubject_Source(String.valueOf(nId));
        if (aObjectGroup != null) {
            sName = aObjectGroup.getsName();
        }
        return armDao.getNumber441(sID_Order, sName);
    }

}
