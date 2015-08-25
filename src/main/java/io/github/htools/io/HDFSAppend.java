package io.github.htools.io;

import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

/**
 * This class is intended to remove all the Java fuzz regarding files. There is
 * just one class RFile that provides methods to read a line, read the entire
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
class HDFSAppend extends HDFSOut {

   private static Log log = new Log(HDFSAppend.class);

   public HDFSAppend(FileSystem fs, String filename, int buffersize) {
      super(fs, new Path(filename), buffersize);
   }

   @Override
   public void openWrite() {
      buffer.offset = 0;
      try {
         if (fs.exists(path)) {
            Path in = new Path(path.toString() + ".old");
            fs.rename(path, in);
            buffer.offset = HDFSIn.getLength(fs, in);
            FSDataInputStream fsin = fs.open(in);
            fsout = fs.create(path, true, buffersize);
            IOUtils.copyBytes(fsin, fsout, 4096, false);
            fsin.close();
            fs.delete(in, true);
         } else {
            fsout = fs.create(path, true, buffersize);
         }
         return;
      } catch (IOException ex) {
         log.exception(ex, "openWrite( %s %d )", path.toString(), buffersize);
      }
   }
}
