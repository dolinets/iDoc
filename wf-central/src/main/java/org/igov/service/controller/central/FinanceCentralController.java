package org.igov.service.controller.central;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import org.igov.model.finance.Merchant;
import org.igov.model.finance.MerchantDao;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.service.business.finance.Liqpay;
import org.igov.service.business.finance.MerchantVO;
import org.igov.util.JSON.JsonRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(tags = {"FinanceCentralController -- Финансовые и смежные сущности"})
@Controller
@RequestMapping(value = "/finance")
public class FinanceCentralController {

    private final Logger LOG = LoggerFactory.getLogger(FinanceCentralController.class);

    @Autowired
    private Liqpay oLiqpay;

    @Autowired
    private MerchantDao merchantDao;

    @Autowired
    private SubjectOrganDao subjectOrganDao;

    private StringBuffer sb = new StringBuffer();

    /**
     * получить весь список обьектов мерчантов
     */
    @ApiOperation(value = "Получить весь список обьектов мерчантов", notes = ""
            + "Пример:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/getMerchants"
            + "Response\n\n"
            + "\n```json\n"
            + "[\n"
            + "    {\n"
            + "        \"nID\":1\n"
            + "        ,\"sID\":\"Test_sID\"\n"
            + "        ,\"sName\":\"Test_sName\"\n"
            + "        ,\"sPrivateKey\":\"test_sPrivateKey\"\n"
            + "        ,\"sURL_CallbackStatusNew\":\"test_sURL_CallbackStatusNew\"\n"
            + "        ,\"sURL_CallbackPaySuccess\":\"test_sURL_CallbackPaySuccess\"\n"
            + "        ,\"nID_SubjectOrgan\":1\n"
            + "        ,\"sID_Currency\":\"UAH\"\n"
            + "    }\n"
            + "    ,{\n"
            + "        \"nID\":2\n"
            + "        ,\"sID\":\"i10172968078\"\n"
            + "        ,\"sName\":\"igov test\"\n"
            + "        ,\"sPrivateKey\":\"BStHb3EMmVSYefW2ejwJYz0CY6rDVMj1ZugJdZ2K\"\n"
            + "        ,\"sURL_CallbackStatusNew\":\"test_sURL_CallbackStatusNew\"\n"
            + "        ,\"sURL_CallbackPaySuccess\":\"test_sURL_CallbackPaySuccess\"\n"
            + "        ,\"nID_SubjectOrgan\":1\n"
            + "        ,\"sID_Currency\":\"UAH\"\n"
            + "    }\n"
            + "]\n"
            + "\n```\n")
    @RequestMapping(value = "/getMerchants", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getMerchants() {
        return JsonRestUtils.toJsonResponse(toVO(merchantDao.findAll()));
    }

    /**
     * получить обьект мерчанта
     *
     * @param sID ID-строка мерчанта(публичный ключ)
     */
    @ApiOperation(value = "Получить обьект мерчанта", notes = "Пример:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/getMerchant?sID=i10172968078"
            + "\nResponse```json\n"
            + "{\n"
            + "    \"nID\":1\n"
            + "    ,\"sID\":\"Test_sID\"\n"
            + "    ,\"sName\":\"Test_sName\"\n"
            + "    ,\"sPrivateKey\":\"test_sPrivateKey\"\n"
            + "    ,\"sURL_CallbackStatusNew\":\"test_sURL_CallbackStatusNew\"\n"
            + "    ,\"sURL_CallbackPaySuccess\":\"test_sURL_CallbackPaySuccess\"\n"
            + "    ,\"nID_SubjectOrgan\":1\n"
            + "    ,\"sID_Currency\":\"UAH\"\n"
            + "}\n"
            + "\n```\n")
    @RequestMapping(value = "/getMerchant", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getMerchant(@ApiParam(value = "ID-строка мерчанта(публичный ключ)", required = true) @RequestParam(value = "sID") String sID) {
        Merchant merchant = merchantDao.getMerchant(sID);
        if (merchant == null) {
            return new ResponseEntity("Merchant with sID=" + sID + " is not found!", HttpStatus.NOT_FOUND);
        }

        return JsonRestUtils.toJsonResponse(new MerchantVO(merchant));
    }

    /**
     * удалить мерчанта
     *
     * @param id ID-строка мерчанта(публичный ключ)
     */
    @ApiOperation(value = "Удаление мерчанта", notes = "Пример:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/removeMerchant?sID=i10172968078"
            + "Response\n"
            + "Status 200\n")
    @RequestMapping(value = "/removeMerchant", method = RequestMethod.DELETE)
    public ResponseEntity removeMerchant(@ApiParam(value = "ID-строка мерчанта(публичный ключ)", required = true) @RequestParam(value = "sID") String id) {
        return new ResponseEntity(merchantDao.deleteMerchant(id) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    /**
     * обновить информацию мерчанта
     *
     * @param nID ID-номер мерчанта(внутренний) //опциональный (если не задан
     * или не найден - будет добавлена запись)
     * @param sID ID-строка мерчанта(публичный ключ) //опциональный (если не
     * задан или не найден - будет добавлена запись)
     * @param sName строковое название мерчанта //опциональный (при добавлении
     * записи - обязательный)
     * @param sPrivateKey приватный ключ мерчанта //опциональный (при добавлении
     * записи - обязательный)
     * @param nID_SubjectOrgan ID-номер субьекта-органа мерчанта(может быть
     * общий субьект у нескольких мерчантов) //опциональный
     * @param sURL_CallbackStatusNew строка-URL каллбэка, при новом статусе
     * платежа(проведении проплаты) //опциональный
     * @param sURL_CallbackPaySuccess строка-URL каллбэка, после успешной
     * отправки платежа //опциональный
     */
    @ApiOperation(value = "Обновление информации мерчанта", notes = ""
            + "Response\n"
            + "\n```json\n"
            + "{\n"
            + "    \"nID\":1\n"
            + "    ,\"sID\":\"Test_sID\"\n"
            + "    ,\"sName\":\"Test_sName22\"\n"
            + "    ,\"sPrivateKey\":\"test_sPrivateKey\"\n"
            + "    ,\"sURL_CallbackStatusNew\":\"test_sURL_CallbackStatusNew\"\n"
            + "    ,\"sURL_CallbackPaySuccess\":\"test_sURL_CallbackPaySuccess\"\n"
            + "    ,\"nID_SubjectOrgan\":1\n"
            + "    ,\"sID_Currency\":\"UAH\"\n"
            + "}\n"
            + "\n```\n"
            + "Примеры обновления:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/setMerchant?sID=Test_sID&sName=Test_sName2\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/setMerchant?nID=1&sName=Test_sName22\n\n"
            + "Пример добавления:\n"
            + "https://alpha.test.igov.org.ua/wf/service/finance/setMerchant?sID=Test_sID3&sName=Test_sName3&sPrivateKey=121212")
    @RequestMapping(value = "/setMerchant", method = RequestMethod.POST)
    public ResponseEntity setMerchant(
            @ApiParam(value = "ID-номер мерчанта(внутренний) (если не задан или не найден - будет добавлена запись)", required = false) @RequestParam(value = "nID", required = false) Long nID,
            @ApiParam(value = "ID-строка мерчанта(публичный ключ) (если не задан или не найден - будет добавлена запись)", required = false) @RequestParam(value = "sID", required = false) String sID,
            @ApiParam(value = "строковое название мерчанта (при добавлении записи - обязательный)", required = false) @RequestParam(value = "sName", required = false) String sName,
            @ApiParam(value = "sPrivateKey строка-приватный ключ мерчанта (при добавлении записи - обязательный)", required = false) @RequestParam(value = "sPrivateKey", required = false) String sPrivateKey,
            @ApiParam(value = "ID-номер субьекта-органа мерчанта(может быть общий субьект у нескольких мерчантов)", required = false) @RequestParam(value = "nID_SubjectOrgan", required = false) Long nID_SubjectOrgan,
            @ApiParam(value = "строка-URL каллбэка, при новом статусе платежа(проведении проплаты)", required = false) @RequestParam(value = "sURL_CallbackStatusNew", required = false) String sURL_CallbackStatusNew,
            @ApiParam(value = "строка-URL каллбэка, после успешной отправки платежа", required = false) @RequestParam(value = "sURL_CallbackPaySuccess", required = false) String sURL_CallbackPaySuccess,
            @ApiParam(value = "международный строковой трехсимвольный код валюты", required = false) @RequestParam(value = "sID_Currency", required = false) String sID_Currency) {

        Merchant merchant = nID != null ? merchantDao.findById(nID).orNull() : new Merchant();

        if (merchant == null) {
            merchant = new Merchant();
        }

        if (sID != null) {
            merchant.setsID(sID);
        }

        if (sName != null) {
            merchant.setName(sName);
        }

        if (sPrivateKey != null) {
            merchant.setsPrivateKey(sPrivateKey);
        }

        if (nID_SubjectOrgan != null) {
            SubjectOrgan subjectOrgan = subjectOrganDao.findByIdExpected(nID_SubjectOrgan);
            merchant.setOwner(subjectOrgan);
        }

        if (sURL_CallbackStatusNew != null) {
            merchant.setsURL_CallbackStatusNew(sURL_CallbackStatusNew);
        }

        if (sURL_CallbackPaySuccess != null) {
            merchant.setsURL_CallbackPaySuccess(sURL_CallbackPaySuccess);
        }

        if (sID_Currency != null) {
            merchant.setsID_Currency(sID_Currency);
        }

        merchant = merchantDao.saveOrUpdate(merchant);
        return JsonRestUtils.toJsonResponse(new MerchantVO(merchant));
    }
    
    public static List<MerchantVO> toVO(List<Merchant> merchants) {
        List<MerchantVO> res = new ArrayList<>();
        for (Merchant merchant : merchants) {
            res.add(new MerchantVO(merchant));
        }

        return res;
    } 

}
