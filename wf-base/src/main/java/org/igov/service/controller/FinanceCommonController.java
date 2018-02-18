package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.igov.model.finance.CurrencyDao;
import org.igov.service.business.finance.FinanceService;
import org.igov.service.exception.CommonServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = { "FinanceCommonController -- Финансы общие (в т.ч. платежи)" })
@Controller
@RequestMapping(value = "/finance")
public class FinanceCommonController {
 
    private final Logger LOG = LoggerFactory.getLogger(FinanceCommonController.class);

    @Autowired
    private FinanceService oFinanceService;

    @Autowired
    private CurrencyDao currencyDao;

    private StringBuffer sb = new StringBuffer();
    

    @ApiOperation(value = "/setPaymentStatus_TaskActiviti", notes = "##### Контроллер платежей. Регистрация проведенного платежа - по колбэку от платежной системы\n")
    @RequestMapping(value = { "/finance/setPaymentStatus_TaskActiviti",
            "/setPaymentStatus_TaskActiviti" }, method = RequestMethod.POST,
            headers = { "Accept=application/json" })
    public
    @ResponseBody
    String setPaymentStatus_TaskActiviti(
	    @ApiParam(value = "Строка-ИД заявки", required = true) @RequestParam String sID_Order,
	    @ApiParam(value = "Строка-ИД платежной системы", required = true) @RequestParam String sID_PaymentSystem,
	    @ApiParam(value = "Строка со вспомогательными данными", required = true) @RequestParam String sData,
	    @ApiParam(value = "Строка-префикс платежа (если их несколько в рамках заявки)", required = false) @RequestParam(value = "sPrefix", required = false) String sPrefix,
            @ApiParam(value = "Строка-Данные от платежной системы", required = false) @RequestBody(required = false) String data,
            @ApiParam(value = "Строка-Подпись платежной системы", required = false) @RequestParam(value = "signature", required = false) String signature,
            HttpServletRequest request
    ) throws Exception {
    
        String resultString = oFinanceService.setPaymentStatus_TaskActiviti(sID_Order, sID_PaymentSystem, sData, sPrefix, sData, signature, request);
        return resultString;     
    }
    


    @ApiOperation(value = "/setPaymentStatus_TaskActiviti_Direct", notes = "##### Контроллер платежей. Регистрация проведенного платежа - по прямому вызову\n")
    @RequestMapping(value = "/finance/setPaymentStatus_TaskActiviti_Direct", method = RequestMethod.GET, headers = {
            "Accept=application/json" })
    public
    @ResponseBody
    String setPaymentStatus_TaskActiviti_Direct(
            @ApiParam(value = "Строка-ИД заявки", required = true) @RequestParam String sID_Order,
	    @ApiParam(value = "Строка-ИД платежной системы", required = true) @RequestParam String sID_PaymentSystem,
	    @ApiParam(value = "Строка со вспомогательными данными", required = true) @RequestParam String sData,
	    @ApiParam(value = "Cтрока-префикс платежа (если их несколько в рамках заявки)", required = false) @RequestParam(value = "sPrefix", required = false) String sPrefix,
	    @ApiParam(value = "Строка-ИД транзакции", required = true) @RequestParam String sID_Transaction,
	    @ApiParam(value = "Строка-статуса платежа", required = true) @RequestParam String sStatus_Payment

    ) throws Exception {       
        
        String resultString = oFinanceService.setPaymentStatus_TaskActiviti_Direct(sID_Order, sID_PaymentSystem, sData, sPrefix, sID_Transaction, sStatus_Payment);
        return resultString; 
    }


    @ApiOperation(value = "/redirectPaymentLiqpay", notes = "##### Получение URL-а и параметров для отправки платежа через POST-запрос\n")
    @RequestMapping(value = "/finance/redirectPaymentLiqpay", method = RequestMethod.GET, headers = {
            "Accept=application/json" })
    public
    @ResponseBody
    Map<String, String> getRedirectPaymentLiqpay(
        @ApiParam(value = "Строка-ИД Мерчанта", required = true) @RequestParam(value = "sID_Merchant", required = true) String sID_Merchant,
        @ApiParam(value = "Сумма (разделитель копеек - точка)", required = false) @RequestParam(value = "sSum", required = false) String sSum,
        @ApiParam(value = "Строка-ИД заявки-платежа", required = false) @RequestParam(value = "sID_Order", required = false) String sID_Order,
        @ApiParam(value = "Описание платежа", required = false) @RequestParam(value = "sDescription", required = false) String sDescription,
        @ApiParam(value = "ИД валюты (3 символа)", required = false) @RequestParam(value = "sID_Currency", required = false) String sID_Currency,
        @ApiParam(value = "", required = false) @RequestParam(value = "sURL_CallbackStatusNew", required = false) String sURL_CallbackStatusNew,
        @ApiParam(value = "", required = false) @RequestParam(value = "sURL_CallbackPaySuccess", required = false) String sURL_CallbackPaySuccess,
        @ApiParam(value = "номер-ИД субьекта", required = true) @RequestParam Long nID_Subject,
        @ApiParam("количество часов актуальности платежа") @RequestParam(required = false) Integer sExpired_Period_Hour
    ) throws Exception {

    	
        
        Map<String, String> mReturn = oFinanceService.getRedirectPaymentLiqpay(sID_Merchant, 
                sSum, sID_Order, sDescription, sID_Currency, sURL_CallbackStatusNew, 
                sURL_CallbackPaySuccess, nID_Subject, sExpired_Period_Hour);
        
        return mReturn;
    }
    

    /**
     * отдает список объектов сущности, подпадающих под критерии параметры.
     *
     * @param sID_UA (опциональный)
     * @param sName_UA (опциональный)
     * @param sName_EN (опциональный)
     * @return список Currency согласно фильтрам
     */
    @ApiOperation(value = "Возвращает список валют, подпадающих под параметры", notes = ""
            + "http://search.ligazakon.ua/l_doc2.nsf/link1/FIN14565.html[Источник данных]\n"
            + "Пример запроса: https://alpha.test.igov.org.ua/wf/service/finance/getCurrencies?sID_UA=004\n"
            + "Пример ответа:\n"
            + "\n```json\n"
            + "{\n"
            + "    \"sID_UA\"       : \"004\",\n"
            + "    \"sName_UA\"     : \"Афґані\",\n"
            + "    \"sName_EN\"     : \"Afghani\",\n"
            + "    \"nID\"          : 1\n"
            + "    \"sID_Currency\" : \"AFA\"\n"
            + "}\n"
            + "\n```\n")
    @RequestMapping(value = "/getCurrencies", method = RequestMethod.GET)
    public @ResponseBody
    List<org.igov.model.finance.Currency> getCurrencies(
            @ApiParam(value = "ИД-номер Код, в украинском классификаторе", required = false) @RequestParam(value = "sID_UA", required = false) String sID_UA,
            @ApiParam(value = "строка-название на украинском", required = false) @RequestParam(value = "sName_UA", required = false) String sName_UA,
            @ApiParam(value = "строка-название на английском", required = false) @RequestParam(value = "sName_EN", required = false) String sName_EN,
            @ApiParam(value = "Международный строковой трехсимвольный код валюты", required = false) @RequestParam(value = "sID_Currency", required = false) String sID_Currency) {

        return currencyDao.getCurrencies(sID_UA, sName_UA, sName_EN, sID_Currency);
    }

    /**
     * обновляет элемент (если задан один из уникальных-ключей) или вставляет
     * (если не задан nID), и отдает экземпляр нового объекта.
     *
     * @param nID (опциональный, если другой уникальный-ключ задан и по нему
     * найдена запись)
     * @param sID_UA (опциональный, если другой уникальный-ключ задан и по нему
     * найдена запись)
     * @param sName_UA (опциональный, если nID задан и по нему найдена запись)
     * @param sName_EN (опциональный, если nID задан и по нему найдена запись)
     * @return обновленный/вставленный обьект
     */
    @ApiOperation(value = "Обновляет запись валюты", notes = ""
            + "обновляет запись (если задан один из параметров: nID, sID_UA; и по нему найдена запись) или вставляет (если не задан nID), и отдает экземпляр нового объекта\n\n"
            + "http://search.ligazakon.ua/l_doc2.nsf/link1/FIN14565.html[Источник данных]\n"
            + "Пример добавления записи:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/setCurrency?sID_UA=050&sName_UA=Така&sName_EN=Taka&sID_Currency=BDT\n"
            + "Пример обновления записи:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/setCurrency?sID_UA=050&sName_UA=Така\n")
    @RequestMapping(value = "/setCurrency", method = RequestMethod.GET)
    public @ResponseBody
    org.igov.model.finance.Currency setCurrency(
            @ApiParam(value = "внутренний ИД-номер (уникальный; если sID_UA задан и по нему найдена запись)", required = false) @RequestParam(value = "nID", required = false) Long nID,
            @ApiParam(value = "ИД-номер Код, в украинском классификаторе (уникальный; если nID задан и по нему найдена запись)", required = false) @RequestParam(value = "sID_UA", required = false) String sID_UA,
            @ApiParam(value = "строка-название на украинском (уникальный; если nID задан и по нему найдена запись)", required = false) @RequestParam(value = "sName_UA", required = false) String sName_UA,
            @ApiParam(value = "строка-название на английском (уникальный; если nID задан и по нему найдена запись)", required = false) @RequestParam(value = "sName_EN", required = false) String sName_EN,
            @ApiParam(value = "международный строковой трехсимвольный код валюты (уникальный; если nID задан и по нему найдена запись)", required = false) @RequestParam(value = "sID_Currency", required = false) String sID_Currency)
            throws CommonServiceException {

        try {
            org.igov.model.finance.Currency currency = null;
            if (nID != null) {
                currency = currencyDao.findByIdExpected(nID);
            }
            if (sID_UA != null) {
                currency = currencyDao.findBy("sID_UA", sID_UA).orNull();
            }
            if (currency == null) {
                if (sID_UA == null || sName_UA == null || sName_EN == null || sID_Currency == null) {
                    throw new IllegalArgumentException(
                            "Currency by key params was not founded. "
                            + "Not enough params to insert.");
                }
                currency = new org.igov.model.finance.Currency();
            }
            if (sID_UA != null) {
                currency.setsID_UA(sID_UA);
            }
            if (sName_UA != null) {
                currency.setsName_UA(sName_UA);
            }
            if (sName_EN != null) {
                currency.setsName_EN(sName_EN);
            }
            if (sID_Currency != null) {
                currency.setsID_Currency(sID_Currency);
            }
            return currencyDao.saveOrUpdate(currency);

        } catch (Exception e) {
            LOG.warn("Error: {}", e.getMessage());
            LOG.trace("FAIL:",  e);
            throw new CommonServiceException(
                    "SYSTEM_ERR",
                    e.getMessage(),
                    e,
                    HttpStatus.FORBIDDEN);
        }
    }

    /**
     * удаляет элемент (по обязательно заданому одному из уникальных-ключей).
     *
     * @param nID (опциональный, если другой уникальный-ключ задан и по нему
     * найдена запись)
     * @param sID_UA (опциональный, если другой уникальный-ключ задан и по нему
     * найдена запись)
     */
    @ApiOperation(value = "Удаление элемента по обязательно заданному одному из параметров", notes = ""
            + "http://search.ligazakon.ua/l_doc2.nsf/link1/FIN14565.html[Источник данных]\n"
            + "Пример запроса:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/removeCurrency?sID_UA=050\n")
    @RequestMapping(value = "/removeCurrency", method = RequestMethod.GET)
    public @ResponseBody
    void removeCurrency(
            @ApiParam(value = "внутренний ИД-номер (уникальный; если sID_UA задан и по нему найдена запись)", required = false) @RequestParam(value = "nID", required = false) Long nID,
            @ApiParam(value = "ИД-номер Код, в украинском классификаторе (уникальный; если nID задан и по нему найдена запись)", required = false) @RequestParam(value = "sID_UA", required = false) String sID_UA)
            throws CommonServiceException {
        try {

            if (nID == null && sID_UA == null) {
                throw new IllegalArgumentException("Key param was not specified");
            }
            if (nID != null && sID_UA != null) {
                throw new IllegalArgumentException("Too many params");
            }
            if (nID != null) {
                currencyDao.delete(nID);
            } else {
                currencyDao.deleteBy("sID_UA", sID_UA);
            }

        } catch (Exception e) {
            LOG.warn("Error: {}", e.getMessage());
            LOG.trace("FAIL:",  e);
            throw new CommonServiceException(
                    "SYSTEM_ERR",
                    e.getMessage(),
                    e,
                    HttpStatus.FORBIDDEN);
        }
    }


}			
