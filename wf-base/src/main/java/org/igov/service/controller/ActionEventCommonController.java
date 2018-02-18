package org.igov.service.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.igov.model.action.task.core.entity.ActionProcessCount;
import org.igov.model.action.task.core.entity.ActionProcessCountDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.json.simple.JSONValue;

/**
 *
 * @author Kovylin
 */
@Controller
@Api(tags = {"ActionEventController -- События по действиям и статистика"})
@RequestMapping(value = "/action/event")
public class ActionEventCommonController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ActionTaskCommonController.class);
    
    @Autowired
    private ActionProcessCountDao actionProcessCountDao;
    
    @ApiOperation(value = "getActionProcessCount", notes = "getActionProcessCount")
    @RequestMapping(value = "/getActionProcessCount", method = RequestMethod.GET)
    public @ResponseBody
    String getActionProcessCount(
            @ApiParam(required = false) @RequestParam(value = "sID_BP", required = false) String sID_BP,
            @ApiParam(required = false) @RequestParam(value = "nID_Service", required = false) Integer nID_Service,
            @ApiParam(required = false) @RequestParam(value = "nYear ", required = false) Integer nYear,
            HttpServletResponse httpResponse) {
        
        LOG.info("getActionProcessCount sID_BP: " + sID_BP);
        LOG.info("getActionProcessCount nID_Service: " + nID_Service);
        LOG.info("getActionProcessCount nYear: " + nYear);
        
        ActionProcessCount res = actionProcessCountDao.getByCriteria(sID_BP, nID_Service, nYear == null ? Calendar.getInstance().get(Calendar.YEAR) : nYear);

        Map<String, Integer> mReturn = new HashMap<String, Integer>();

        if (res != null) {
            mReturn.put("nCountYear", res.getnCountYear().intValue());
        } else {
            mReturn.put("nCountYear", 0);
        }
        
        LOG.info("getActionProcessCount mReturn:" + JSONValue.toJSONString(mReturn));
        return JSONValue.toJSONString(mReturn);
    }
    
    @ApiOperation(value = "setActionProcessCount", notes = "getActionProcessCount")
    @RequestMapping(value = "/setActionProcessCount", method = RequestMethod.GET)
    public @ResponseBody
    String setActionProcessCount(
            @ApiParam(required = false) @RequestParam(value = "sID_BP", required = false) String sID_BP,
            @ApiParam(required = false) @RequestParam(value = "nID_Service", required = false) Integer nID_Service,
            @ApiParam(required = false) @RequestParam(value = "nYear ", required = false) Integer nYear,
            HttpServletResponse httpResponse) {
        ActionProcessCount oActionProcessCountReturn = actionProcessCountDao.getByCriteria(sID_BP, nID_Service, nYear == null ? Calendar.getInstance().get(Calendar.YEAR) : nYear);

        LOG.info("Found ActionProcessCount {}", oActionProcessCountReturn);
        if (oActionProcessCountReturn == null) {
            ActionProcessCount oActionProcessCount = new ActionProcessCount();
            oActionProcessCount.setsID_BP(sID_BP);
            oActionProcessCount.setnCountYear(0);
            oActionProcessCount.setnID_Service(nID_Service);
            oActionProcessCount.setnYear(nYear == null ? Calendar.getInstance().get(Calendar.YEAR) : nYear);
            oActionProcessCountReturn = oActionProcessCount;
        } else {
            oActionProcessCountReturn.setnCountYear(oActionProcessCountReturn.getnCountYear() + 1);
        }
        oActionProcessCountReturn = actionProcessCountDao.saveOrUpdate(oActionProcessCountReturn);
        LOG.info("Saved updated info {}: {}", oActionProcessCountReturn, oActionProcessCountReturn.getId());

        List<ActionProcessCount> list = actionProcessCountDao.findAll();
        LOG.info("Total number of elements: {}", list);
        Map<String, Integer> mReturn = new HashMap<String, Integer>();

        if (oActionProcessCountReturn != null) {
            mReturn.put("nCountYear", oActionProcessCountReturn.getnCountYear());
        } else {
            mReturn.put("nCountYear", 0);
        }
        return JSONValue.toJSONString(mReturn);
    }
}
