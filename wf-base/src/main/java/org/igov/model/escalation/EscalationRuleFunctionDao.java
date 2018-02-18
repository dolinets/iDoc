package org.igov.model.escalation;

import org.igov.model.core.EntityDao;

public interface EscalationRuleFunctionDao extends EntityDao<Long, EscalationRuleFunction> {

    EscalationRuleFunction saveOrUpdate(Long nID, String sName, String sBeanHandler);
}
