/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.nais;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import org.igov.io.GeneralConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.igov.bjust.IService;
import org.igov.bjust.ServiceLocator;

/**
 *
 * @author olga
 */
@Service
public class NaisService {

    private static final Logger LOG = LoggerFactory.getLogger(NaisService.class);

    @Autowired
    GeneralConfig generalConfig;

    public String getServiceURL(String sID_NAIS_Service_code_value, String sID_NAIS_Application_id_value) throws RemoteException, ServiceException {

        ServiceLocator serviceLocator = new ServiceLocator();
        IService service = serviceLocator.getBinding_IService();
        String sessionId = RandomStringUtils.random(20, true, true);
        LOG.info("Got web service locator. Parameter of the method. service_code:{} application ID:{} sessionId:{}",
                sID_NAIS_Service_code_value, sID_NAIS_Application_id_value, sessionId);
        LOG.info("Before calling getServiceURL method");
        String result = service.getServiceURL(sID_NAIS_Service_code_value, Integer.valueOf(sID_NAIS_Application_id_value), sessionId);
        LOG.info("Received response from getServiceURL in just web service:" + result);
        return result;
    }

}
