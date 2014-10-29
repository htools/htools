package io.github.repir.tools.Lib;

import io.github.repir.tools.Lib.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author jeroen
 */
public enum WebTools {;

    public static final Log log = new Log(WebTools.class);
    private static SystemProps systemPropsSingleton;
    
    private static enum SystemProps {
        CONNECT("sun.net.client.defaultConnectTimeout", 20000),
        READ("sun.net.client.defaultReadTimeout", 20000);
        
        SystemProps(String prop, int value) {
            System.setProperty(prop, Integer.toString(value));
        }
    }
    
    public static byte[] getUrlByteArray(String urlpage) throws Exception {
        return getUrlByteArray(urlpage, 2000);
    }
    
    public static byte[] getUrlByteArray(String urlpage, int timeout) throws Exception {
        Exception exc = new Exception();
        if (urlpage.contains("://"))
            return getUrlByteArray(new URL(urlpage), timeout);
        for (String method : new String[]{ "http://", "https://"} ) {
            try {
                return getUrlByteArray(new URL(method + urlpage), timeout);
            } catch ( MalformedURLException ex ) {
                exc = ex;
            }
        }
        throw exc;
    }
    
    private static byte[] getUrlByteArray(URL url, int timeout) throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout", Integer.toString(timeout));
        System.setProperty("sun.net.client.defaultReadTimeout", Integer.toString(timeout));
        InputStream is = null;
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {
            is = url.openStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            log.exception(e, "Failed while reading bytes from %s: %s", url.toExternalForm(), e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return bais.toByteArray();
    }
    
    private static byte[] getUrlByteArray(URL url) throws Exception {
        return getUrlByteArray(url, 2000);
    }
}
