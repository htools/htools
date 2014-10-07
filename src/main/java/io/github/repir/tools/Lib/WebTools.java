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
        CONNECT("sun.net.client.defaultConnectTimeout", 2000),
        READ("sun.net.client.defaultReadTimeout", 2000);
        
        SystemProps(String prop, int value) {
            System.setProperty(prop, Integer.toString(value));
        }
    }
    
    public static byte[] getUrlByteArray(String urlpage) throws Exception {
        Exception exc = new Exception();
        if (urlpage.contains("://"))
            return getUrlByteArray(new URL(urlpage));
        for (String method : new String[]{ "http://", "https://"} ) {
            try {
                return getUrlByteArray(new URL(method + urlpage));
            } catch ( MalformedURLException ex ) {
                exc = ex;
            }
        }
        throw exc;
    }
    
    private static byte[] getUrlByteArray(URL url) throws Exception {
        System.setProperty("sun.net.client.defaultConnectTimeout", "2000");
        System.setProperty("sun.net.client.defaultReadTimeout", "2000");
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
}
