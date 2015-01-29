package io.github.repir.tools.io;

import io.github.repir.tools.lib.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

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
