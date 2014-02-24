package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

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
 * <p/>
 * @author jbpvuurens
 */
public class FSFileInBuffer extends FSFile implements DataIn {

   public static Log log = new Log(FSFileInBuffer.class);
   public BufferReaderWriter buffer;
   private long offset = 0;

   public FSFileInBuffer(String fullpathname) {
      super(fullpathname);
      reopen();
   }

   private void reopen() {
      this.offset = 0;
      this.close();
      getInputStream();
   }

   public void mustMoveBack() {
      reopen();
   }

   public void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   public void setBufferSize(int size) {
      //log.info("setbuffersize( %s bufferoffset %d bufferpos %d read %d fileoffset %d newbuffersize %d )",
      //        this.getFilename(), bufferoffset, buffer.bufferpos, buffer.end, getOffset(), size);
      buffer.offset += buffer.resize(size);
   }

   public int getBufferSize() {
      if (buffer == null || buffer.buffer == null) {
         return 0;
      }
      return buffer.buffer.length;
   }

   public void fillBuffer(BufferReaderWriter buffer) throws EOFException {
      //log.info("fillBuffer");
      buffer.shift();
      if (!buffer.hasmore) {
         log.fatal("Trying to read past Ceiling (offset %d pos %d end %d ceiling %d)", buffer.offset, buffer.bufferpos, buffer.end, buffer.ceiling);
      }
      int newread, maxread = buffer.buffer.length - buffer.end;
      if (maxread > buffer.ceiling - buffer.offset - buffer.end) {
         maxread = (int) (buffer.ceiling - buffer.offset - buffer.end);
      }
      if (maxread == 0) {
         maxread = buffer.buffer.length - buffer.end;
         buffer.hasmore = false;
      }
      int read = readBytes(buffer.offset + buffer.end, buffer.buffer, buffer.end, maxread);
      //log.info("fillBuffer() %s bufferoffset %d bufferpos %d bufferend %d ceiling %d maxread %d read %d",
      //        this.getFullPathName(), buffer.offset, buffer.bufferpos, buffer.end, buffer.ceiling, maxread, read);
      if (read > 0) {
         buffer.setEnd(buffer.end + read);
      } else {
         buffer.hasmore = false;
         //log.info("EOF reached");
         throw new EOFException("EOF reached");
      }
   }

   @Override
   public long getLength() {
      return this.file.length();
   }

   private void setOffset(long offset) {
      if (offset < this.offset) {
         mustMoveBack();
      }
      if (offset > this.offset) {
         try {
            inputstream.skip(offset - this.offset);
            this.offset = offset;
         } catch (IOException ex) {
            log.exception(ex, "setOffset( %d ) inputstream %s", offset, inputstream);
         }
      }
   }

   public int readBytes(long offset, byte[] b, int pos, int length) {
      //log.info("readBytes() offset=%d pos=%d length=%d", offset, pos, length);
      try {
         if (offset != this.offset) {
            setOffset(offset);
         }
         int read = inputstream.read(b, pos, length);
         if (read > -1) {
            for (int i = pos + read; i < length; i++) {
               b[i] = 0;
            }
            this.offset += read;
         }
         return read;
      } catch (IOException ex) {
         log.exception(ex, "readBytes( %d, %s, %d, %d ) inputstream %s", offset, b, pos, length, inputstream);
      }
      return 0;
   }

   public void openRead() {
   }
}
