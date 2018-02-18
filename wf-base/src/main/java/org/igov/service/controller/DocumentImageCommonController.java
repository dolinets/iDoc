package org.igov.service.controller;

import com.google.common.base.Strings;
import io.swagger.annotations.*;
import org.igov.model.document.image.DocumentImageFileSignVO;
import org.igov.model.document.image.DocumentImageFileVO;
import org.igov.service.business.document.image.DocumentImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 * @author Kovylin
 */
@Controller
@Api(tags = {"DocumentImageController -- Накладывание ЭЦП на независимые документы"})
@RequestMapping(value = "/document/image")
public class DocumentImageCommonController {
    
    private static final Logger LOG = LoggerFactory.getLogger(DocumentImageCommonController.class);
    
    @Autowired
    private DocumentImageService documentImageService;

    @ApiOperation(value = "Отправка исходного файла-образа документа на сервер", consumes = "multipart/form-data")
    @ApiResponse(code = 200, message = "DocumentImageFileVO")
    @RequestMapping(value = "/setDocumentImageFile", method = RequestMethod.POST)
    @ResponseBody
    public DocumentImageFileVO setDocumentImageFile(
            @ApiParam(required = false, value = "Строка-ИД токена, по которому был открыт доступ на загрузку") @RequestParam(required = false, value = "sID_Token") String sID_Token,
            @ApiParam(required = true, value = "Отсылаемый документ") @RequestParam("file") MultipartFile file
    ) throws IOException {
        return documentImageService.setDocumentImageFile(sID_Token, file);
    }

    @ApiOperation("Загрузка исходного файла-образа документа с сервера")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Скачивание файла"),
        @ApiResponse(code = 404, message = "{\"code\":\"BUSINESS_ERR\",\"message\":\"DocumentImageFile with nID = [value] not found\"}")
    })
    @RequestMapping(value = "/getDocumentImageFile", method = RequestMethod.GET)
    public HttpEntity<byte[]> getDocumentImageFile(
            @ApiParam(required = true, value = "ИД документа") @RequestParam Long nID,
            @ApiParam(required = true, value = "Ключ доступа к скачиванию") @RequestParam String sSecret,
            HttpServletResponse httpResponse
    ) throws IOException {
        MultipartFile oDocumentImageFile = documentImageService.getDocumentImageFile(nID, sSecret);
        httpResponse.setHeader("Accept-Ranges", "bytes");
        return createHttpEntity(oDocumentImageFile);
    }
    
    @ApiOperation(value = "Получение информации о файле-образе документа", notes = "Пример использования:\n" +
            "http://alpha.test.region.igov.org.ua/wf/service/document/image/getDocumentImageFile?nID=[nIDValue]&sSecret=[sSecret]\n" +
            "В ответ:\n" +
            "{\"sURL\":\"val\",\"nID\":val,\"sSecret\":\"val\",\"sHash\":\"val\",\"sKey_File\":\"val\",\"sID_FileStorage\":\"MongoDB\",\"sFileType\":\"application/pdf\",\"sFileExtension\":\"pdf\",\"nBytes\":99,\"sDateSave\":\"val\",\"aDocumentImageFileSign\":[{\"sSign\":\"val\",\"sSignData_JSON\":\"{\\\"sClass\\\":{\\\"sName\\\":\\\"sNameValue\\\",\\\"sNote\\\":\\\"sNoteValue\\\"}}\",\"nID\":1}]}" +
            "Если не найден:\n" +
            "{\"code\":\"BUSINESS_ERR\",\"message\":\"DocumentImageFile with nID = [id] not found\"}")
    @RequestMapping(value = "/getDocumentImageFileVO", method = RequestMethod.GET)
    @Transactional
    @ResponseBody
    public DocumentImageFileVO getDocumentImageFileVO(
            @ApiParam(required = true, value = "ИД документа") @RequestParam Long nID,
            @ApiParam(required = true, value = "Ключ доступа к мета-данным") @RequestParam String sSecret) {
        return documentImageService.getDocumentImageFileVO(nID, sSecret);
    }
    
    @ApiOperation(value = "Скачать документ со всеми приложеными цифровыми подписями", notes = "Пример использования:\n" +
            "http://alpha.test.region.igov.org.ua/wf/service/document/image/getDocumentImageFileSigned?nID=[value]&sSecret=[value]\n" +
            "В ответ: Скачивание файла с его подписями\n" +
            "Если не найден документ, то возвращается:\n" +
            "{\"code\":\"BUSINESS_ERR\",\"message\":\"DocumentImageFile with nID = [id] not found\"}")
    @RequestMapping(value = "/getDocumentImageFileSigned", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> getDocumentImageFileSigned(
            @ApiParam(required = true, value = "ИД документа") @RequestParam Long nID,
            @ApiParam(required = true, value = "Ключ доступа к скачиванию") @RequestParam String sSecret
    ) throws IOException {
        MultipartFile oDocumentImageFileSigned = documentImageService.getDocumentImageFileSigned(nID, sSecret);
        return createHttpEntity(oDocumentImageFileSigned);
    }

    @ApiOperation("Сохранить подпись к документу")
    @RequestMapping(value = "/setDocumentImageFileSign", method = RequestMethod.POST)
    @ApiResponse(code = 200, message = "DocumentImageFileVO")
    @ResponseBody
    public DocumentImageFileVO setDocumentImageFileSign(
            @ApiParam(required = true, value = "ИД документа") @RequestParam Long nID_DocumentImageFile,
            @ApiParam(required = true, value = "Ключ доступа") @RequestParam String sSecret_DocumentImageFile,
            @RequestBody DocumentImageFileSignVO oDocumentImageFileSignVO) {
        String sSign = oDocumentImageFileSignVO.getsSign();
        if (Strings.isNullOrEmpty(sSign)) {
            throw new IllegalArgumentException("Param 'sSign' can't be null");
        }
    
        return documentImageService.setDocumentImageFileSign(sSign, oDocumentImageFileSignVO.getsID_SignType(), oDocumentImageFileSignVO.getsSignData_JSON(), nID_DocumentImageFile, sSecret_DocumentImageFile);
    }

    @ApiOperation("Удалить подпись к документу")
    @RequestMapping(value = "/removeDocumentImageFileSign", method = RequestMethod.GET)
    @ResponseBody
    public DocumentImageFileVO removeDocumentImageFileSign(
        @ApiParam(required = true, value = "Строка с подписью") @RequestParam String sSign,
        @ApiParam(required = true, value = "ИД документа") @RequestParam Long nID_DocumentImageFile,
        @ApiParam(required = true, value = "Ключ доступа") @RequestParam String sSecret_DocumentImageFile
    ) {
        return documentImageService.removeDocumentImageFileSign(sSign, nID_DocumentImageFile, sSecret_DocumentImageFile);
    }

    private static HttpEntity<byte[]> createHttpEntity(MultipartFile oMultipartFile) throws IOException {
        return new HttpEntity<>(oMultipartFile.getBytes(), createoHttpHeaders(oMultipartFile));
    }

    private static HttpHeaders createoHttpHeaders(MultipartFile oMultipartFile) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(oMultipartFile.getContentType()));
        httpHeaders.setContentLength(oMultipartFile.getSize());
        httpHeaders.set("Content-Disposition", "attachment; filename=" + oMultipartFile.getName().replace(" ", "_"));
        return httpHeaders;
    }

}
