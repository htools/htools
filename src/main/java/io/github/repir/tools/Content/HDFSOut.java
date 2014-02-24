package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.fs.FSDataOutputStream;
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
 * <p/>
 * @author jbpvuurens
 */
class HDFSOut implements DataOut {

   private static Log log = new Log(HDFSOut.class);
   public FileSystem fs;
   public Path path;
   public int buffersize;
   public FSDataOutputStream fsout;
   public BufferReaderWriter buffer;
   public int lc = 0;

   protected HDFSOut() {
   }

   public HDFSOut(FileSystem fs, String filename, int buffersize) {
      this(fs, new Path(filename), buffersize);
   }

   public HDFSOut(FileSystem fs, Path path, int buffersize) {
      this.fs = fs;
      this.path = path;
      this.buffersize = buffersize;
   }

   public void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   public static void delete(FileSystem fs, Path path) {
      try {
         fs.delete(path, false);
      } catch (IOException ex) {
         log.exception(ex, "delete( %s, %s )", fs, path);
      }
   }

   public static void delete(FileSystem fs, String filename) {
      delete(fs, new Path(filename));
   }

   public static void delete(HDFSOut out) {
      delete(out.fs, out.path);
   }

   public static void delete(HDFSIn in) {
      delete(in.fs, in.path);
   }

   @Override
   public void close() {
      flushBuffer(buffer);
      try {
         fsout.close();
      } catch (IOException ex) {
         log.exception(ex, "close() buffer %s fsout %s", buffer, fsout);
      }
      fsout = null;
   }

   @Override
   public void flushBuffer(BufferReaderWriter buffer) {
      try {
         fsout.write(buffer.buffer, 0, buffer.bufferpos);
         buffer.offset += buffer.bufferpos;
         buffer.bufferpos = 0;
      } catch (IOException ex) {
         log.fatal(ex);
      }
   }

   public void flushFile() {
      try {
         flushBuffer(buffer);
         fsout.sync();
      } catch (IOException ex) {
         log.fatal(ex);
      }
   }

   public long getOffset() {
      return buffer.offset + buffer.bufferpos;
   }

   public void openWrite() {
      try {
         fsout = fs.create(path, true, buffersize);
      } catch (IOException ex) {
         log.exception(ex, "openWrite( %s %d )", path.toString(), buffersize);
      }
   }

   public boolean exists() {
      return HDFSDir.isFile(fs, this.path);
   }

   @Override
   public void openAppend() {
      buffer.bufferpos = 0;
      if (exists() && HDFSIn.getLength(fs, path) > 0) {
         try {
            Path in = new Path(path + ".in");
            fs.delete(in, false);
            fs.rename(path, in);
            InputStream is = fs.open(in);
            this.openWrite();
            IOUtils.copyBytes(is, this.getOutputStream(), 4096, false);
            is.close();
            buffer.offset = fsout.getPos();
            fs.delete(in, false);
         } catch (IOException ex) {
            log.fatalexception(ex, "openAppend() %s", path);
         }
      } else {
         openWrite();
         buffer.offset = 0;
      }
   }

   @Override
   public OutputStream getOutputStream() {
      return fsout;
   }
}
