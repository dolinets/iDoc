package org.igov.service.business.util;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.springframework.stereotype.Service;

/**
 *
 * @author Kovylin
 */
@Service
public class CharsetDetector {
    
    private final String[] charsets = {"UTF-8", "windows-1251"};
    
    public Charset detectCharsets(byte[] aChar) {

        Charset charset = null;

        for (String charsetName : charsets) {
            charset = detectCharset(aChar, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }

        return charset;
    }

    private Charset detectCharset(byte[] aChar, Charset charset) {
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(aChar);

            CharsetDecoder decoder = charset.newDecoder();
            decoder.reset();

            byte[] buffer = new byte[512];
            boolean identified = false;
            while ((input.read(buffer) != -1) && (!identified)) {
                identified = identify(buffer, decoder);
            }

            input.close();

            if (identified) {
                return charset;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    private boolean identify(byte[] bytes, CharsetDecoder decoder) {
        try {
            decoder.decode(ByteBuffer.wrap(bytes));
        } catch (CharacterCodingException e) {
            return false;
        }
        return true;
    }
    
}
