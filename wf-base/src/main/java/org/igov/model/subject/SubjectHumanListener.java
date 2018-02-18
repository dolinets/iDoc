package org.igov.model.subject;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author iDoc-2
 */

import org.igov.io.GeneralConfig;
import org.igov.model.core.AutowireHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;

@Component
public class SubjectHumanListener {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanListener.class);

    @Autowired
    private GeneralConfig oGeneralConfig;

    @PostPersist
    public void postLoad(SubjectHuman oSubjectHuman) {
        LOG.info("SubjectHumanListener start!!!");
        AutowireHelper.autowire(this, oGeneralConfig);
        LOG.info("SubjectHumanListener oGeneralConfig: " + oGeneralConfig);
        if (!oGeneralConfig.isHolding()) {
            LOG.info("SubjectHumanListener before setoServer set null");
            oSubjectHuman.setoServer(null);
            LOG.info("SubjectHumanListener after setoServer set null: " + oSubjectHuman.getoServer());
        }
        LOG.info("SubjectHumanListener end!!!");
    }

}
