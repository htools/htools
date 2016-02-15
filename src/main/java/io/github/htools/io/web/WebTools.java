package io.github.htools.io.web;

import io.github.htools.lib.Log;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author jeroen
 */
public enum WebTools {

    ;

    public static final Log log = new Log(WebTools.class);
    private static SystemProps systemPropsSingleton;
    private static TrustManager[] trustmanager = trustManager();

    private static enum SystemProps {

        SNI("jsse.enableSNIExtension", "false"),
        CONNECT("sun.net.client.defaultConnectTimeout", 20000),
        READ("sun.net.client.defaultReadTimeout", 20000);

        SystemProps(String prop, int value) {
            System.setProperty(prop, Integer.toString(value));
        }

        SystemProps(String prop, String value) {
            System.setProperty(prop, value);
        }
    }

    public static UrlResult getUrlResult(String urlpage) {
        return getContent(urlpage, 2000);
    }

    public static UrlResult getContent(String urlpage, int timeout) {
        UrlResult result = null;
        try {
            if (urlpage.contains("://")) {
                return getContent(new URL(urlpage), timeout);
            }
            for (String method : new String[]{"http://", "https://"}) {
                result = getContent(new URL(method + urlpage), timeout);
                if (result.responsecode < 400) {
                    return result;
                }
            }
        } catch (MalformedURLException ex) {
            result = new UrlResult();
            result.responsecode = 600;
            result.ex = ex;
        }
        return result;
    }

    public static class Verifier implements HostnameVerifier {

        public boolean verify(String arg0, SSLSession arg1) {
            return true;   // mark everything as verified
        }
    }

    private static TrustManager[] trustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new Verifier());
        } catch (Exception e) {
        }
        return trustAllCerts;
    }

    private static CookieManager getCookieManager() {
        CookieManager manager = new CookieManager();
        manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
        return manager;
    }

    public static UrlConnection getUrlConnection(URL url, int timeout) {
        UrlConnection result = new UrlConnection();
        result.connection = getConnection(url, timeout);
        boolean redirect = true;
        try {
            // try four times, following redirects
            for (int t = 0; t < 4 && redirect; t++) {
                result.connection.connect();
                result.url = result.connection.getURL();
                result.responsecode = result.connection.getResponseCode();
                redirect = result.responsecode >= 300 && result.responsecode < 400;
                if (redirect) {
                    result.url = new URL(result.connection.getHeaderField("Location"));
                    result.connection = getConnection(result.url, timeout * (1 + t));
                    log.info("redirect %s", result.url);
                } else {
                    // check if gzip compressed, then wrap in decompressor
                    result.encoding = result.connection.getHeaderField("Content-Encoding");
                    if (result.encoding == null) {
                        result.inputstream = result.connection.getInputStream();
                    } else if (result.encoding.equals("gzip") || result.encoding.equals("x-gzip")) {
                        result.inputstream = new GZIPInputStream(result.connection.getInputStream());
                    } else {
                        log.info("Unsupported encoding %s", result.encoding);
                    }
                }
            }
        } catch (IOException ex1) {
            try {
                log.exception(ex1, "getUrlConnection %d %s", result.responsecode, result.url);
                result.responsecode = result.connection.getResponseCode();
                result.ex = ex1;
                result.inputstream = result.connection.getErrorStream();
            } catch (IOException ex) {
                result.ex = ex;
            }
        }
        return result;
    }

    public static HttpURLConnection getConnection(URL url, int timeout) {
        HttpURLConnection connection = null;
        getCookieManager();
        System.setProperty("sun.net.client.defaultConnectTimeout", Integer.toString(timeout));
        System.setProperty("sun.net.client.defaultReadTimeout", Integer.toString(timeout));
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        } catch (IOException ex) {
        }
        return connection;
    }

    public static UrlResult getContent(URL url, int timeout) {
        UrlConnection conn = getUrlConnection(url, timeout);
        return getContent(conn);
    }

    public static UrlResult getContent(UrlConnection conn) {
        UrlResult content = new UrlResult();
        content.responsecode = conn.responsecode;
        content.ex = conn.ex;
        content.redirected = conn.connection.getURL();

        if (conn.inputstream != null && conn.isOK()) {
            // read url contents
            ByteArrayOutputStream bais = new ByteArrayOutputStream();
            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
            int n;
            try {
                while ((n = conn.inputstream.read(byteChunk)) > 0) {
                    bais.write(byteChunk, 0, n);
                }
                conn.inputstream.close();
            } catch (IOException ex1) {
                log.exception(ex1, "getContent(%s)", conn.url);
                content.ex = ex1;
            }
            content.content = bais.toByteArray();
        }

        return content;
    }

    private static UrlResult getUrlByteArray(URL url) {
        return getContent(url, 2000);
    }

    public static class UrlResult {

        public URL url;
        public URL redirected;
        public byte content[];
        public int responsecode;
        public String contenttype;
        public IOException ex;

        public boolean isOK() {
            return responsecode == 200;
        }

        public boolean isNoResponse() {
            return responsecode == 204;
        }
    }

    public static class UrlConnection {

        public URL url;
        public String encoding;
        public HttpURLConnection connection;
        public InputStream inputstream;
        public int responsecode;
        public IOException ex;

        public boolean isOK() {
            return responsecode == 200;
        }

        public boolean isNoResponse() {
            return responsecode == 204;
        }

        public HttpURLConnection getConnection() {
            return connection;
        }
    }
}
