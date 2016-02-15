package io.github.htools.io;

import io.github.htools.lib.Log;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.IOException;
import java.io.InputStream;

public class FSFileTARGZ extends FSFileGZ {

   public static Log log = new Log(FSFileTARGZ.class);
   private InputStream inputstream;

   public FSFileTARGZ(String fullpathname) {
      super(fullpathname);
   }

   @Override
   public InputStream getInputStream() {
      if (inputstream == null) {
         inputstream = (InputStream) new TarArchiveInputStream(super.getInputStream());
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
