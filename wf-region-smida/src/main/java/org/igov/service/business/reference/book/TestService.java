/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.reference.book;

import org.igov.model.process.processChat.ProcessChatResult;
import org.igov.service.business.process.processChat.ProcessChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author alex
 */
@Service
public class TestService {

    @Autowired
    private ProcessChatService oProcessChatService;

    public ProcessChatResult getProcessChat(Long nID_Process_Activiti) throws Exception{
        return oProcessChatService.findByProcess_Activiti(nID_Process_Activiti);
    }

}
