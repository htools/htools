package io.github.htools.io;

import io.github.htools.lib.Log;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.IOException;
import java.io.InputStream;

public class FSFileGZ extends FSFile {

   public static Log log = new Log(FSFileGZ.class);
   private InputStream inputstream;

   public FSFileGZ(String fullpathname) {
      super(fullpathname);
   }

   @Override
   public InputStream getInputStream() {
      try {
         inputstream = (InputStream) new GzipCompressorInputStream(super.getInputStream());
      } catch (IOException ex) {
         log.exception(ex, "getInputStream()");
      }
      return inputstream;
   }

   @Override
   public void close() {
      try {
         if (inputstream != null) {
            inputstream.close();
            super.close();
            inputstream = null;
         }
      } catch (IOException ex) {
         log.exception(ex, "close() inputstream %s", inputstream);
      }
   }
}
