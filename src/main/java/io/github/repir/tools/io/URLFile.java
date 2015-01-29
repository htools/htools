package io.github.repir.tools.io;

import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a URL as a RString. Through this class URL's are only
 * readable as a whole or per line. URLFile's can therefore only be opened in
 * SCANNER mode. For writing to URL's, using cookies and other fancy stuff see
 * {@link URL}
 */
public class URLFile extends FileGeneric {

   private static Log log = new Log(URLFile.class);
   public java.net.URL url;
   public Status status;
   public InputStream is;
   public Scanner scanner;

   @Override
   public InputStream getInputStream() {
      try {
         is = url.openStream();
      } catch (IOException ex) {
         log.exception(ex, "getInputStream() url %s", url);
      }
      return is;
   }

   @Override
   public void print(String s, Object... p) {
      throw new UnsupportedOperationException("Can't print to a URL");
   }

   enum Status {

      CLOSED,
      INPUTSTREAM
   }

   /**
    * The URL to read from
    * <p/>
    * @param url
    */
   public URLFile(java.net.URL url) {
      this.url = url;
   }

   public URLFile(String url) throws MalformedURLException {
      this(new java.net.URL(url));
   }

   @Override
   public void close() {
      try {
         if (status == Status.INPUTSTREAM) {
            is.close();
            is = null;
         }
      } catch (IOException ex) {
         log.exception(ex, "close() inputstream %s", is);
      }
   }
}
