package io.github.repir.tools.lib;

import io.github.repir.tools.lib.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeroen
 */
public enum WebTools {

    ;

    public static final Log log = new Log(WebTools.class);
    private static SystemProps systemPropsSingleton;

    private static enum SystemProps {

        CONNECT("sun.net.client.defaultConnectTimeout", 20000),
        READ("sun.net.client.defaultReadTimeout", 20000);

        SystemProps(String prop, int value) {
            System.setProperty(prop, Integer.toString(value));
        }
    }

    public static UrlResult getUrlByteArray(String urlpage) {
        return getUrlByteArray(urlpage, 2000);
    }

    public static UrlResult getUrlByteArray(String urlpage, int timeout) {
        UrlResult result = null;
        try {
            if (urlpage.contains("://")) {
                return getUrlByteArray(new URL(urlpage), timeout);
            }
            for (String method : new String[]{"http://", "https://"}) {
                result = getUrlByteArray(new URL(method + urlpage), timeout);
                if (result.responsecode < 400)
                    return result;
            }
        } catch (MalformedURLException ex) {
            result = new UrlResult();
            result.responsecode = 600;
            result.ex = ex;
        }
        return result;
    }

    private static UrlResult getUrlByteArray(URL url, int timeout) {
        HttpURLConnection connection = null;
        InputStream is = null;
        System.setProperty("sun.net.client.defaultConnectTimeout", Integer.toString(timeout));
        System.setProperty("sun.net.client.defaultReadTimeout", Integer.toString(timeout));
        UrlResult content = new UrlResult();
        content.url = url;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            content.responsecode = connection.getResponseCode();
            //content.contenttype = connection.getContentType();
            is = connection.getInputStream();
        } catch (IOException ex1) {
            try {
                content.responsecode = connection.getResponseCode();
                content.ex = ex1;
                log.exception(ex1, "getUrlByteArray(%s) responsecode=%d", url, content.responsecode);
                is = connection.getErrorStream();
            } catch (IOException ex2) {
                content.ex = ex2;
                log.exception(ex1, "getUrlByteArray(%s) error getting responsecode", url);
            }
        }
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        content.redirected = connection.getURL();
        log.info("getUrlByteArray %s %s", url.toString(), connection.getURL().getPath());
        byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
        int n;

        try {
            while ((n = is.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
            is.close();
        } catch (IOException ex1) {
            content.ex = ex1;
            log.exception(ex1, "getUrlByteArray(%s) reading inputstream", url);
        }
        content.content = bais.toByteArray();
        return content;
    }

    private static UrlResult getUrlByteArray(URL url) {
        return getUrlByteArray(url, 2000);
    }

    public static class UrlResult {

        public URL url;
        public URL redirected;
        public byte content[];
        public int responsecode;
        public String contenttype;
        public IOException ex;
    }
}
