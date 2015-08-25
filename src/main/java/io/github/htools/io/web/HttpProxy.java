package io.github.htools.io.web;

import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.ProxyHost;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.params.CoreProtocolPNames;

/**
 *
 * @author jeroen
 */
public class HttpProxy {

    public static final Log log = new Log(HttpProxy.class);
    private static TrustManager[] trustmanager = trustManager();
    HttpClient httpClient;
    String YCAv2 = getYCA();

    public HttpProxy(int timeout, String proxy) {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setSoTimeout(timeout);
        httpClient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        System.setProperty("jsse.enableSNIExtension", "false");
        System.setProperty("sun.net.client.defaultConnectTimeout", "20000");
        System.setProperty("sun.net.client.defaultReadTimeout", "20000");
        proxy = "httpproxy-res.tan.ygrid.yahoo.com:4080";
        ProxyHost p = new ProxyHost(proxy);
        httpClient.getHostConfiguration().setProxyHost(p);
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
    }

    static ByteSection certopen = ByteSearch.create("<certificate").toSection(ByteSearch.create(">")).innerQuoteSafe();
    static ByteSection certificate = certopen.toSection(ByteSearch.create("</certificate>"));
    
    public static String getYCA() {
        WebTools.UrlResult content
            = WebTools.getContent("http://ca.yca.platform.yahoo.com:4080/wsca/v2/certificates/kerberos/dht.set.standalone?http_proxy_role=grid.tan.res.httpproxy", 2000);
        log.info("%s", ByteTools.toString(content.content));
        ByteSearchSection section = certificate.findPos(content.content);
        if (section.found()) {
            return section.toString();
        } else {
            log.info("Invalid certificate %s", ByteTools.toString(content.content));
        }
        return null;
    }

    public UrlResult getUrlResult(String urlpage) throws IOException {
        return getContent(urlpage, 2000);
    }

    public static UrlResult getContent(String urlpage, int timeout) throws IOException, IOException {
        UrlResult result = null;
        try {
            if (urlpage.contains("://")) {
                return getContent(urlpage, timeout);
            }
            for (String method : new String[]{"http://", "https://"}) {
                result = getContent(method + urlpage, timeout);
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
        manager.setCookiePolicy(java.net.CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(manager);
        return manager;
    }

    public UrlConnection getUrlConnection(String url, int timeout) throws IOException {
        UrlConnection result = new UrlConnection();
        final HttpMethod httpMethod = new GetMethod(url);
        httpMethod.addRequestHeader("Yahoo-App-Auth", YCAv2);
        httpMethod.addRequestHeader("Accept-Encoding", "gzip");
        httpMethod.addRequestHeader("Accept-Encoding", "x-gzip");
        result.responsecode = httpClient.executeMethod(httpMethod);
        result.url = httpMethod.getPath();
        result.method = httpMethod;
        result.encoding = httpMethod.getResponseHeader("Content-Encoding");
        if (result.encoding == null) {
            result.inputstream = httpMethod.getResponseBodyAsStream();
        } else if (result.encoding.equals("gzip") || result.encoding.equals("x-gzip")) {
            result.inputstream = new GZIPInputStream(httpMethod.getResponseBodyAsStream());
        } else {
            log.info("Unsupported encoding %s", result.encoding);
        }
        return result;
    }

    public UrlResult getContent(UrlConnection conn) {
        UrlResult content = new UrlResult();
        content.responsecode = conn.responsecode;
        content.ex = conn.ex;
        content.redirected = conn.url;

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

    private UrlResult getUrlByteArray(String url) throws IOException {
        return getContent(url, 2000);
    }

    public static class UrlResult {

        public String url;
        public String redirected;
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

        public String url;
        public Header encoding;
        public HttpMethod method;
        public InputStream inputstream;
        public int responsecode;
        public IOException ex;

        public boolean isOK() {
            return responsecode == 200;
        }

        public boolean isNoResponse() {
            return responsecode == 204;
        }

        public HttpMethod getConnection() {
            return method;
        }
    }
}
