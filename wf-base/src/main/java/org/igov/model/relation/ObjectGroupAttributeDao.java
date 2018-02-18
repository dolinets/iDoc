package org.igov.model.relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.igov.model.core.AbstractEntity;
import org.igov.model.core.EntityDao;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

public interface ObjectGroupAttributeDao extends EntityDao<Long, ObjectGroupAttribute> {

}
