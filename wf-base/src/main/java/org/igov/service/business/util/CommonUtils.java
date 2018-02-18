package org.igov.service.business.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

public class CommonUtils {

    public static String getStringStackTrace( Exception oException ){
	String sStack = null;
	if ( oException != null ) {
	    StringWriter errors = new StringWriter();
	    oException.printStackTrace(new PrintWriter(errors));
	    sStack = errors.toString();
	}

	return sStack;
    }

    public static String sO(Object oValue) {
        return sNull(oValue, "");
    }

    public static String sNull(Object oValue, String sDefault) {
        return oValue != null ? oValue.toString() : sDefault;
    }
    
    public static boolean bIs(String sValue) {
        return sValue != null && !"".equals(sValue);
    }
    
    public static String s(java.util.Date oDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(oDate);
    }
}
