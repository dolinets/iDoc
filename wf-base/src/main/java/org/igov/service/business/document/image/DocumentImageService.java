package org.igov.service.business.document.image;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.statical.IBytesDataStorage;
import org.igov.io.db.kv.temp.model.ByteArrayMultipartFile;
import org.igov.model.document.image.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.privatbank.cryptonite.CryptoniteException;
import ua.privatbank.cryptonite.CryptoniteX;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author Kovylin
 */
@Service
public class DocumentImageService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DocumentImageService.class);
    
    @Autowired
    @Qualifier("durableBytesDataStorage")
    private IBytesDataStorage oBytesDataStaticStorage;
    @Autowired
    private DocumentImageFileDao oDocumentImageFileDao;
    @Autowired
    private DocumentImageFileSignDao oDocumentImageFileSignDao;
    @Autowired
    private SignTypeDao oSignTypeDao;
    @Autowired
    private GeneralConfig oGeneralConfig;

//    @Transactional
    public DocumentImageFileVO setDocumentImageFile(String sID_Token, MultipartFile oMultipartFile) throws IOException {

        // todo проверка sID_Token

        byte[] aFileBytes = oMultipartFile.getBytes();

        DocumentImageFile oDocumentImageFile = new DocumentImageFile();
        oDocumentImageFile.setsSecret(UUID.randomUUID().toString());
        String sKey_File = oBytesDataStaticStorage.saveData(aFileBytes);
        oDocumentImageFile.setsKey_File(sKey_File);
        oDocumentImageFile.setsID_FileStorage("MongoDB");
        oDocumentImageFile.setsFileType("application/octet-stream");
        oDocumentImageFile.setsFileExtension("pdf");
        oDocumentImageFile.setnBytes(oMultipartFile.getSize());
        oDocumentImageFile.setsDateSave(getsCurrentDateTime());
        
        DocumentImageFile oSavedDocumentImageFile = oDocumentImageFileDao.saveOrUpdate(oDocumentImageFile);
        LOG.info("Document has created successfully, sKey: {}, sHash: {}", sKey_File, oDocumentImageFile.getsHash());
        return buildDocumentImageFileVO(oSavedDocumentImageFile);
    }

    public MultipartFile getDocumentImageFile(Long nID, String sSecret) {
        DocumentImageFile oDocumentImageFile = findoDocumentImageFile(nID, sSecret);
        String sFilename = getsFilename(oDocumentImageFile);
        return new ByteArrayMultipartFile(oBytesDataStaticStorage.getData(oDocumentImageFile.getsKey_File()), sFilename, sFilename, oDocumentImageFile.getsFileType());
    }

    public DocumentImageFileVO getDocumentImageFileVO(Long nID, String sSecret) {
        DocumentImageFile oDocumentImageFile = findoDocumentImageFile(nID, sSecret);
        return buildDocumentImageFileVO(oDocumentImageFile);
    }
    
    public MultipartFile getDocumentImageFileSigned(Long nID, String sSecret) {
        DocumentImageFile oDocumentImageFile = findoDocumentImageFile(nID, sSecret);
        
        List<byte[]> abDocumentSign = oDocumentImageFile
                .getaDocumentImageFileSign()
                .stream()
                .map(DocumentImageFileSign::getsSign)
                .map(this::decodeBase64ToBytes)
                .collect(Collectors.toList());
        LOG.info("DocumentImageFile nID: {}, has {} signs", nID, abDocumentSign.size());
        
        if (abDocumentSign.size() > 0) {
            byte[] abDocumentContent = getDocumentImageFileContent(oDocumentImageFile);
            
            try {
                // include document content to container
                CryptoniteX.cmsSetData(abDocumentSign.get(0), abDocumentContent);
                
                byte[] abJoin = CryptoniteX.cmsJoin(convertListToArray(abDocumentSign));
                String filename = getsFilename(oDocumentImageFile);
                return new ByteArrayMultipartFile(abJoin, filename, filename, oDocumentImageFile.getsFileType());
            } catch (CryptoniteException e) {
                LOG.error("CantBuildDocumentSignedContainer, error: {}", e);
                throw new RuntimeException("CantBuildDocumentSignedContainer");
            }
        } else {
            return createMultipartFileByDocumentImageFile(oDocumentImageFile);
        }
    }
    
    private byte[] decodeBase64ToBytes(String value) {
        return Base64.getDecoder().decode(value);
    }
    
    private MultipartFile createMultipartFileByDocumentImageFile(DocumentImageFile oDocumentImageFile) {
        String filename = getsFilename(oDocumentImageFile);
        byte[] abContent = getDocumentImageFileContent(oDocumentImageFile);
        return new ByteArrayMultipartFile(abContent, filename, filename, oDocumentImageFile.getsFileType());
    }
    
    private byte[] getDocumentImageFileContent(DocumentImageFile oDocumentImageFile) {
        return oBytesDataStaticStorage.getData(oDocumentImageFile.getsKey_File());
    }

    public DocumentImageFileVO setDocumentImageFileSign(String sSign, String sID_SignType, String sSignData_JSON, Long nID_DocumentImageFile, String sSecret) {

        DocumentImageFile oDocumentImageFile = findoDocumentImageFile(nID_DocumentImageFile, sSecret);

        DocumentImageFileSign oDocumentImageFileSign = new DocumentImageFileSign();
        oDocumentImageFileSign.setsSign(sSign);
        oDocumentImageFileSign.setsSignData_JSON(sSignData_JSON);
        if (sID_SignType != null) {
            Optional<SignType> oSignTypeWrapper = oSignTypeDao.findBy("sID", sID_SignType);
            if (oSignTypeWrapper.isPresent()) {
                SignType oSignType = oSignTypeWrapper.get();
                if (sSignData_JSON != null) {
                    setsClass(oSignType, sSignData_JSON);
                    oSignTypeDao.saveOrUpdate(oSignType);
                }
                oDocumentImageFileSign.setoSignType(oSignType);
            } else {
                LOG.warn("attempt to update not existing SignType: {}, sign data: {}", sID_SignType, sSignData_JSON);
            }
        }
        oDocumentImageFile.addoDocumentImageFileSign(oDocumentImageFileSign);

        oDocumentImageFileSignDao.saveOrUpdate(oDocumentImageFileSign);

        return buildDocumentImageFileVO(oDocumentImageFile);
    }

    public DocumentImageFileVO removeDocumentImageFileSign(String sSign, Long nID_DocumentImageFile, String sSecret_DocumentImageFile) {
        DocumentImageFile oDocumentImageFile = findoDocumentImageFile(nID_DocumentImageFile, sSecret_DocumentImageFile);
        oDocumentImageFile.getaDocumentImageFileSign().removeIf(difs -> difs.getsSign().equals(sSign));
        oDocumentImageFileDao.saveOrUpdate(oDocumentImageFile);
        return buildDocumentImageFileVO(oDocumentImageFile);
    }

    private void setsClass(SignType signType, String sSignData_JSON) {
        try {
            JsonNode oNode = new ObjectMapper().readTree(sSignData_JSON);
            if (oNode.has("sClass")) {
                JsonNode oClass = oNode.get("sClass");
                signType.setsClass(oClass.toString());
                if (oClass.has("sName")) {
                    signType.setsName(oClass.get("sName").asText());
                }
                if (oClass.has("sNote")) {
                    signType.setsNote(oClass.get("sNote").asText());
                }
            }
        } catch (IOException e) {
            LOG.warn("can't parse sSignData_JSON, error: {}", e);
        }
    }

    private DocumentImageFile findoDocumentImageFile(Long nID, String sSecret) {
        DocumentImageFile oDocumentImageFile = oDocumentImageFileDao.findByIdExpected(nID);
        if (!oDocumentImageFile.getsSecret().equals(sSecret)) {
            LOG.info("SECURITY-WARNING: nID {} sSecret {}", nID, sSecret);
            throw new RuntimeException("Access denied, reason: wrong secret");
        }
        return oDocumentImageFile;
    }

    private DocumentImageFileVO buildDocumentImageFileVO(DocumentImageFile oDocumentImageFile) {
        DocumentImageFileVO oDocumentImageFileVO = new DocumentImageFileVO();
        oDocumentImageFileVO.setaDocumentImageFileSign(oDocumentImageFile.getaDocumentImageFileSign());        
        oDocumentImageFileVO.setnBytes(oDocumentImageFile.getnBytes());        
        oDocumentImageFileVO.setnID(oDocumentImageFile.getId());        
        oDocumentImageFileVO.setsDateSave(oDocumentImageFile.getsDateSave());        
        oDocumentImageFileVO.setsFileExtension(oDocumentImageFile.getsFileExtension());       
        oDocumentImageFileVO.setsFileType(oDocumentImageFile.getsFileType());        
        oDocumentImageFileVO.setsHash(oDocumentImageFile.getsHash());        
        oDocumentImageFileVO.setsID_FileStorage(oDocumentImageFile.getsID_FileStorage());        
        oDocumentImageFileVO.setsKey_File(oDocumentImageFile.getsKey_File());        
        oDocumentImageFileVO.setsSecret(oDocumentImageFile.getsSecret());        
        oDocumentImageFileVO.setsURL(getsUrl(oDocumentImageFile));
        oDocumentImageFileVO.setsHash(toBase64String(oBytesDataStaticStorage.getData(oDocumentImageFile.getsKey_File())));
        return oDocumentImageFileVO;
    }
    
    private String toBase64String(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes));
    }

    private String getsUrl(DocumentImageFile oDocumentImageFile) {
        String sLocalUrl = "/share"; // fixme
        String sBaseUrl = oGeneralConfig.getSelfHost() + sLocalUrl;
        Long id = oDocumentImageFile.getId();
        String sSecret = oDocumentImageFile.getsSecret();
        return String.format("%s/nID=%d&sSecret=%s", sBaseUrl, id, sSecret);
    }

    private static String getFileExtension(MultipartFile file) {
        String[] parts = file.getOriginalFilename().split("\\.");
        return parts.length > 1 ? parts[parts.length - 1] : null;
    }

    private static String getsFilename(DocumentImageFile oDocumentImageFile) {
        return "file_" + oDocumentImageFile.getsKey_File() + "." + oDocumentImageFile.getsFileExtension();
    }

    private static String getsCurrentDateTime() {
        SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        return dtf.format(new Date());
    }
    
    private static byte[][] convertListToArray(List<byte[]> aBytes) {
        byte[][] aResultBytes = new byte[aBytes.size()][];
        for (int i = 0; i < aBytes.size(); i++) {
            aResultBytes[i] = aBytes.get(i);
        }
        return aResultBytes;
    }

}
