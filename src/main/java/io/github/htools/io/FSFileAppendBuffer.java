package io.github.htools.io;

import io.github.htools.lib.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class is intended to remove all the Java fuzz regarding files. There is
 * just one class FSFile that provides methods to read a line, read the entire
 * thing, write stuff to it, without having bother about which stream to use.
 * However, Java objects like properly opened FileInputStream and FileChannel.
 * <br><br> Some methods are provided that will more easily allow to get
 * information on the file, such as the parent Dir object, the filename,
 * extension, etc. <br><br> Some static methods are provided to do big file
 * operations, such as copying, moving, running and converting a File to a
 * primitive.
 * <p>
 * @author jbpvuurens
 */
public class FSFileAppendBuffer extends FSFileOutBuffer {

   private static Log log = new Log(FSFileAppendBuffer.class);

   public FSFileAppendBuffer(String fullpathname) {
      super(fullpathname);
   }
   
   public OutputStream getOutputStream() {
      try {
         if (outputstream == null) {
            createIfNotExists();
            outputstream = new FileOutputStream(file, true);
         }
      } catch (IOException ex) {
         log.exception(ex, "getOutputStream() file %s", file);
      }
      return outputstream;
   }


}
