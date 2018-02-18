/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.controller.interceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.igov.service.controller.AccessCommonController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author olya
 */
public class MultiReadServletFilter implements Filter {

    private static final Logger LOG = LoggerFactory
            .getLogger(AccessCommonController.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpSession oSession = request.getSession(false);
            LOG.info("oSession: " + oSession + " id: " + (oSession != null ? oSession.getId() : "none"));
            Cookie[] aCookie = request.getCookies();
            LOG.info("aCookie: " + aCookie + " size: " + (aCookie != null ? aCookie.length : "null"));
            if (aCookie != null) {
                for (Cookie oCookie : aCookie) {
                    LOG.info("oCookie: " + oCookie.getName() + " : " + oCookie.getValue());
                }
            }
            Map<String, String[]> extraParams = new TreeMap<String, String[]>();
            
            if(oSession != null){
                String sLogin = (String) oSession.getAttribute("sLogin");
                String sLoginReferent = (String) oSession.getAttribute("sLoginReferent");
                extraParams.put("sLogin", new String[]{sLogin});
                extraParams.put("sLoginReferent", new String[]{sLoginReferent});
            }
            
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            MultiReadHttpServletRequest requestMultiRead = new MultiReadHttpServletRequest(request, extraParams);
            HttpSession oSessionMultiRead = requestMultiRead.getSession(false);
            LOG.info("oSessionMultiRead: " + oSessionMultiRead + " id: " + (oSessionMultiRead != null ? oSessionMultiRead.getId() : "none"));
            Cookie[] aCookieMultiRead = requestMultiRead.getCookies();
            LOG.info("aCookieMultiRead: " + aCookieMultiRead + " size: " + (aCookieMultiRead != null ? aCookieMultiRead.length : "null"));
            if (aCookieMultiRead != null) {
                for (Cookie oCookieMultiRead : aCookieMultiRead) {
                    LOG.info("oCookieMultiRead: " + oCookieMultiRead.getName() + " : " + oCookieMultiRead.getValue());
                }
            }
            MultiReaderHttpServletResponse responseMultiRead = new MultiReaderHttpServletResponse(response,
                    requestMultiRead);
            requestMultiRead.setAttribute("responseMultiRead", responseMultiRead);
            filterChain.doFilter(requestMultiRead, responseMultiRead);
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}
