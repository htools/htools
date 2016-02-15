package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;

import java.io.File;
import java.io.IOException;

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
public class FSFileOutBuffer extends FSFile implements DataOut {

   private static Log log = new Log(FSFileOutBuffer.class);
   public BufferReaderWriter buffer;

   public FSFileOutBuffer(String fullpathname) {
      super(fullpathname);
      this.getOutputStream(); // opens the output stream
   }

   public void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   public void flushBuffer(BufferReaderWriter buffer) {
      //log.info("flushBuffer() %s %s offset %d length %d", outputstream, this.getFullPathName(), buffer.offset, buffer.bufferpos);
      try {
         outputstream.write(buffer.buffer, 0, buffer.bufferpos);
         buffer.offset += buffer.bufferpos;
         buffer.bufferpos = 0;
      } catch (IOException ex) {
         log.exception(ex, "flushBuffer( %s ) outputstream %s", buffer, outputstream);
      }
   }

   public void setBufferSize(int buffersize) {
      buffer.setBufferSize(buffersize);
   }

   @Override
   public void close() {
      flushBuffer();
      super.close();
   }

   public void flushBuffer() {
      try {
         outputstream.write(buffer.buffer, 0, buffer.bufferpos);
         buffer.offset += buffer.bufferpos;
         //log.info("flushed %d", buffer.bufferpos);
         buffer.bufferpos = 0;
         if (buffer.getRequestedBufferSize() != buffer.getBufferSize()) {
            buffer.resize(buffer.getRequestedBufferSize());
         }
      } catch (IOException ex) {
         log.exception(ex, "flushBuffer() outputstream %s buffer %s", outputstream, buffer);
      }
   }

   public void flushFile() {
      try {
         flushBuffer();
         outputstream.flush();
      } catch (IOException ex) {
         log.exception(ex, "flushFile() outputstream %s", outputstream);
      }
   }

   public long getOffset() {
      return buffer.offset + buffer.bufferpos;
   }

   public void openWrite() {
   }

   public void openAppend() {
   }

   public static boolean lock(String file, String lockfile) {
      File filepath = new File(file);
      File lockfilepath = new File(lockfile);
      long filesize = 0;
      int attempt = 0;
      if (filepath.exists() || lockfilepath.exists()) {
         do {
            try {
               if (!lockfilepath.exists()) {
                  lockfilepath.createNewFile();
                  return true;
               }
               long currentsize = filepath.length();
               if (currentsize != filesize) {
                  filesize = currentsize;
                  attempt = 0;
               }
            } catch (IOException ex) {
            }
            log.sleep(1000);
         } while (attempt++ < 20);
      } else {
         try {
            return lockfilepath.createNewFile();
         } catch (IOException ex) {
         }
      }
      return false;
   }

   public static void unlock(String lockfile) {
      File lockfilepath = new File(lockfile);
      if (lockfilepath.exists()) {
         lockfilepath.delete();
      }
   }

   public static boolean waitForUnlock(String file, String lockfile) {
      File lockfilepath = new File(lockfile);
      File filepath = new File(file);
      long newsize = 0, filesize = 0;
      int attempt = 0;
      if (!filepath.exists() && !lockfilepath.exists()) {
         return true;
      }
      do {
         if (!lockfilepath.exists()) {
            return true;
         }
         long currentsize = lockfilepath.exists() ? lockfilepath.length() : 0;
         if (currentsize != filesize) {
            filesize = currentsize;
            attempt = 0;
         }
         log.sleep(1000);
      } while (attempt++ < 20);
      return false;
   }
}
