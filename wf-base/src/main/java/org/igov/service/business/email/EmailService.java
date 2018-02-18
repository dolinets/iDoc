/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.email;

import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.mail.EmailException;
import org.igov.io.mail.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 *
 * @author iDoc-2
 */
@Service
public class EmailService {
    
    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private ApplicationContext context;
    
    public void sendEmail(String sTo, String sHead, String sBody, Multipart oMultipart) {
        try {
            
            if(oMultipart == null){            
               oMultipart = new MimeMultipart();
            }
            
            Mail oMail = context.getBean(Mail.class);
            oMail._To(sTo)
                    ._Head(sHead)
                    ._Body(sBody)
                    ._oMultiparts(oMultipart);
            oMail.send();
        } catch (EmailException | BeansException ex) {
            LOG.error("Error during mail sending: {}", ex);
        }
    }
    
}
