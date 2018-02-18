package org.igov.model.document;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alex
 */
@Repository
public class DocumentStatutoryDaoImpl extends GenericEntityDao<Long, DocumentStatutory> implements DocumentStatutoryDao {

    protected DocumentStatutoryDaoImpl() {
        super(DocumentStatutory.class);
    }

    @Override
    public List<DocumentStatutory> findAllByDocumentID(List<Long> anID_Document) {
        Criteria oCriteria = getSession().createCriteria(DocumentStatutory.class);
        if (anID_Document != null && !anID_Document.isEmpty()) {
            oCriteria.add(Restrictions.in("oDocument.id", anID_Document));
        }
        List<DocumentStatutory> aDocumentStatutory = (List<DocumentStatutory>) oCriteria.list();
        return aDocumentStatutory;
    }
}
