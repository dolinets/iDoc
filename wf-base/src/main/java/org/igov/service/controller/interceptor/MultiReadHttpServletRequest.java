/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.controller.interceptor;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author olya
 */
public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory
            .getLogger(MultiReadHttpServletRequest.class);
    private static volatile String errorText_NotSupported = "mark/reset/isFinished/isReady/setReadListener not supported";
    private byte[] body;
    
    private final Map<String, String[]> modifiableParameters;
    private Map<String, String[]> allParameters = null;
    
    public MultiReadHttpServletRequest(HttpServletRequest httpServletRequest, Map<String, String[]> additionalParams) throws IOException {
        super(httpServletRequest);
        // Read the request body and save it as a byte array
        InputStream is = super.getInputStream();
        body = IOUtils.toByteArray(is);
        modifiableParameters = new TreeMap<String, String[]>();
        modifiableParameters.putAll(additionalParams);
    }

      @Override
    public String getParameter(final String name)
    {
        String[] strings = getParameterMap().get(name);
        if (strings != null)
        {
            return strings[0];
        }
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap()
    {
        if (allParameters == null)
        {
            allParameters = new TreeMap<String, String[]>();
            allParameters.putAll(super.getParameterMap());
            allParameters.putAll(modifiableParameters);
        }
        //Return an unmodifiable collection because we need to uphold the interface contract.
        return Collections.unmodifiableMap(allParameters);
    }

    @Override
    public Enumeration<String> getParameterNames()
    {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(final String name)
    {
        return getParameterMap().get(name);
    }
    
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamImpl(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        String enc = getCharacterEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        return new BufferedReader(new InputStreamReader(getInputStream(), enc));
    }
    
    private class ServletInputStreamImpl extends ServletInputStream {

        private InputStream is;

        public ServletInputStreamImpl(InputStream is) {
            this.is = is;
        }

        public int read() throws IOException {
            return is.read();
        }

        public boolean markSupported() {
            return false;
        }

        public synchronized void mark(int i) {
            throw new UnsupportedOperationException(new IOException(errorText_NotSupported));
        }

        public synchronized void reset() throws IOException {
            throw new UnsupportedOperationException(errorText_NotSupported);
        }

        @Override
        public boolean isFinished() {
            throw new UnsupportedOperationException(errorText_NotSupported);
        }

        @Override
        public boolean isReady() {
            throw new UnsupportedOperationException(errorText_NotSupported);
        }

        @Override
        public void setReadListener(ReadListener rl) {
            throw new UnsupportedOperationException(errorText_NotSupported);
        }
    }
}
