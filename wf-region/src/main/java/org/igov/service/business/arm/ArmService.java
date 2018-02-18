package org.igov.service.business.arm;

import java.util.List;

import org.igov.model.arm.DboTkModel;
import org.igov.model.arm.DboTkResult;

public interface ArmService {

	public List<DboTkModel> getDboTkByOutNumber(String outNumber);
        
        public List<DboTkModel> getDboTkByNumber441(Integer Number_441);

	public DboTkResult createDboTk(DboTkModel dboTkModel);

	public DboTkResult updateDboTk(DboTkModel dboTkModel);
	
	public DboTkResult updateDboTkByExpert(DboTkModel dboTkModel);
        
        public DboTkResult updateDboTkByAnswer(DboTkModel dboTkModel);
	
	public Integer getMaxValue();
	
	public Integer getMaxValue442(); 
        
        public Integer getNumber441(String sID_Order, String sExecutor);	
}
