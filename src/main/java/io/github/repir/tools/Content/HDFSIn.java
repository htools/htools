package io.github.repir.tools.Content;

import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionInputStream;

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
public class HDFSIn implements DataIn {

   private static Log log = new Log(HDFSIn.class);
   public FileSystem fs;
   public Path path;
   private boolean lockIsMine = false;
   public FSDataInputStream fsdin;
   public InputStream cin;
   public BufferReaderWriter buffer;
   //public int BUFFERMAX = 4 * 1024;

   public HDFSIn(FileSystem fs, Path path, boolean open) {
      try {
         this.path = path;
         this.fs = fs;
         fsdin = fs.open(path, 4096);
         CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(fs.getConf());
         CompressionCodec codec = compressionCodecs.getCodec(path);
         if (codec != null) {
            cin = codec.createInputStream(fsdin);
         }
//         log.info("codec %s cin %s", codec, cin);
//         log.info("open %s", path.getName());
//         log.info("created %s", path.toString());
      } catch (IOException ex) {
         log.fatalexception(ex, "HDFSIn( %s )", path);
      }
   }

   public HDFSIn(FileSystem fs, Path path) {
      this(fs, path, true);
   }

   public HDFSIn(FileSystem fs, String filename) {
      this(fs, new Path(filename));
   }

   public HDFSIn(FSDataInputStream in) {
      fsdin = in;
   }

   public HDFSIn(CompressionInputStream in) {
      cin = in;
   }

   public void mustMoveBack() {
   }

   public void setBuffer(BufferReaderWriter buffer) {
      this.buffer = buffer;
   }

   @Override
   public long getLength() throws IOException {
      if (cin != null) {
         return Long.MAX_VALUE;
      } else {
         return getLength(fs, path);
      }
   }

   public static long getLength(FileSystem fs, Path path) throws IOException {
      return fs.getFileStatus(path).getLen();
   }

   public static long getLengthNoExc(FileSystem fs, Path path) {
      try {
         return fs.getFileStatus(path).getLen();
      } catch (IOException ex) {
         log.fatalexception(ex, "getLengthNoExc() %s", path);
         return -1;
      }
   }

   public void fillBuffer(BufferReaderWriter buffer) throws EOCException {
      buffer.shift();
      if (!buffer.hasmore) {
         log.fatal("Trying to read past Ceiling (%s offset %d pos %d end %d ceiling %d)", this.path.toString(), buffer.offset, buffer.bufferpos, buffer.end, buffer.ceiling);
      }
      int newread, maxread = buffer.buffer.length - buffer.end;
      if (maxread > buffer.ceiling - buffer.offset - buffer.end) {
         maxread = (int) (buffer.ceiling - buffer.offset - buffer.end);
      }
      if (maxread == 0) {
         maxread = buffer.buffer.length - buffer.end;
         buffer.hasmore = false;
      }
      //log.info("buffer offset %d ceiling %d pos %d end %d length %d maxread %d", buffer.offset, buffer.ceiling, buffer.bufferpos, buffer.end, buffer.buffer.length, maxread);
      int read = readBytes(buffer.offset + buffer.end, buffer.buffer, buffer.end, maxread);
      //log.info("readStringFillBuffer() %s bufferoffset %d bufferpos %d bufferend %d ceiling %d buffersize %d maxread %d read %d",
      //        this.path.toString(), buffer.offset, buffer.bufferpos, buffer.end, buffer.ceiling, buffer.buffer.length, maxread, read);
      if (read > 0) {
         buffer.setEnd(buffer.end + read);
      } else {
         buffer.hasmore = false;
         throw new EOCException("EOF reached");
      }
   }

   public void close() {
      try {
         if (cin != null) {
            cin.close();
         }
         fsdin.close();
         //log.info("closed(%s)", this.path.toString());
      } catch (IOException ex) {
         log.exception(ex, "close() cin %s fsdin %s", cin, fsdin);
      }
   }

   public int getBufferSize() {
      return buffer.buffer.length;
   }

   public void setBufferSize(int buffersize) {
      buffer.setBufferSize(buffersize);
   }

   public void readFully(byte[] b) throws EOCException {
      buffer.readBytes(b, 0, b.length);
   }

   public void readFully(byte[] b, int off, int len) throws EOCException {
      buffer.readBytes(b, off, len);
   }

   public int skipBytes(int n) throws IOException {
      if (n < buffer.end - buffer.bufferpos) {
         buffer.bufferpos += n;
      } else {
         int fileskip = n - (buffer.end - buffer.bufferpos);
         if (cin != null) {
            cin.skip(fileskip);
         } else {
            fsdin.skip(fileskip);
         }
         buffer.offset += fileskip;
         buffer.discard();
      }
      return n;
   }

   public int readBytes(long offset, byte[] b, int pos, int length) {
      try {
         int read = 0;
         if (cin != null) {
            //log.info("readBytes Compressed");
            if (offset != buffer.getOffset()) {
               log.fatal("cannot use different offset on compressed file");
            }
            read = cin.read(b, pos, length);
            //log.info("read %d", read );
         } else {
            //log.info("readBytes file %s offset %d bufferlength %d pos %d length %d", this.path.toString(), offset, b.length, pos, length);
            read = fsdin.read(offset, b, pos, length);
         }
         if (read < 1) {
            return 0;
         }
         for (int i = pos + ((read == -1) ? 0 : read); i < b.length; i++) {
            b[i] = 0;
         }
         return read;
      } catch (IOException ex) {
         log.exception(ex, "readBytes( %d, %s, %d, %d ) cin %s fsdin %s", offset, b, pos, length, cin, fsdin);
      }
      return 0;
   }

   @Override
   public void openRead() {
      try {
         fsdin = fs.open(path, 4096);
         CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(fs.getConf());
         CompressionCodec codec = compressionCodecs.getCodec(path);
         if (codec != null) {
            cin = codec.createInputStream(fsdin);
         }
      } catch (IOException ex) {
         log.fatalexception(ex, "HDFSIn( %s )", path);
      }
   }

   public InputStream getInputStream() {
      return (cin != null) ? cin : fsdin;
   }
}
