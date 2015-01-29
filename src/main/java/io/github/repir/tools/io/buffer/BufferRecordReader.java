package io.github.repir.tools.io.buffer;

import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * This is a general class to read and write binary data to an in memory buffer
 * that can optionally be connected to an input stream.
 * <p/>
 * @author jbpvuurens
 */
public class BufferRecordReader extends BufferReaderWriter {

   public static Log log = new Log(BufferRecordReader.class);
   ByteRecordReader reader;

   public BufferRecordReader(ByteRecordReader reader) {
      super();
      this.reader = reader;
      reader.setMaxSize(getRequestedBufferSize());
   }

   @Override
   public void fillBuffer() throws EOCException {
      if (reader != null) {
         if (!hasmore || !softFillBuffer()) {
            throw getEOF();
         }
      } else {
         hasmore = false;
         //log.info("fillBuffer exception2 pos %d end %d", bufferpos, end);
         throw getEOF();
      }
   }

   @Override
   public boolean softFillBuffer() throws EOCException {
      if (reader != null) {
         if (hasmore) {
            try {
               fill();
               return true;
            } catch (EOCException ex) {
               hasmore = false;
               this.eof = ex;
            }
         }
      }
      return false;
   }

   public void fill() throws EOCException {
      shift();
      if (!hasmore) {
         log.fatal("Trying to read past Ceiling (offset %d pos %d end %d ceiling %d)", offset, bufferpos, end, ceiling);
      }
      try {
         if (reader.nextKeyValue()) {
            BytesWritable currentValue = reader.getCurrentValue();
            int read = currentValue.getLength();
            while (end + read > buffer.length) {
               this.setBufferSize(getBufferSize() * 2);
            }
            System.arraycopy(currentValue.getBytes(), 0, buffer, end, read);
            setEnd(end + read);
            return;
         }
      } catch (IOException ex) {
      }
      hasmore = false;
      throw new EOCException("EOF reached");
   }

   public void setBufferSize(int buffersize) {
      super.setBufferSize(buffersize);
      
   }   
   
   @Override
   public boolean hasMore() {
      return getOffset() < getCeiling();
   }

   @Override
   public void readBytes(byte b[], int offset, int length) throws EOCException {
      if (length > buffer.length) {
         try {
            for (; bufferpos < end; bufferpos++, length--) {
               b[offset++] = buffer[bufferpos];
            }
            while (length > 0 && reader.nextKeyValue()) {
               BytesWritable currentValue = reader.getCurrentValue();
               byte read[] = currentValue.getBytes();
               int p = 0;
               for (; p < read.length && length > 0; p++, length--) {
                  b[offset++] = read[p];
               }
               if (length == 0) {
                  if (p < read.length) {
                     System.arraycopy(read, p, buffer, 0, read.length - p);
                     bufferpos = 0;
                     end = read.length - p;
                  } else {
                     bufferpos = 0;
                     end = 0;
                  }
               }
            }
         } catch (IOException ex) {
            throw new EOCException("readBytes offset %d length %d", offset, length);
         } 
      } else {
         checkIn(length);
         if (bufferpos <= end - length) {
            length += offset;
            while (offset < length) {
               b[offset++] = buffer[bufferpos++];
            }
         }
      }
   }

   @Override
   public int shift() {
      if (getRequestedBufferSize() != buffer.length) {
         return resize(getRequestedBufferSize());
      } else {
         int shift = bufferpos;
         for (int i = shift; i < end; i++) {
            buffer[ i - shift] = buffer[i];
         }
         end -= shift;
         bufferpos -= shift;
         offset += shift;
         return shift;
      }
   }

   @Override
   public int resize(int buffersize) {
      if (buffer == null) {
         buffer = new byte[buffersize];
      } else {
         int pos = bufferpos;
         int usedbuffersize = (buffersize >= end - pos) ? buffersize : end - pos;
         byte newbuffer[] = new byte[usedbuffersize];
         for (int i = pos; i < end; i++) {
            newbuffer[ i - pos] = buffer[i];
         }
         end -= pos;
         bufferpos -= pos;
         buffer = newbuffer;
         //log.info("resize() size %d end %d", buffer.length, end);
         this.offset += pos;
         return pos;
      }
      return 0;
   }

   public static class ByteInputFormat extends FileInputFormat<NullWritable, BytesWritable> {
      public static final Log log = new Log(ByteInputFormat.class);

      @Override
      public RecordReader<NullWritable, BytesWritable>
              createRecordReader(InputSplit split,
                      TaskAttemptContext context) {
         return new ByteRecordReader();
      }

      @Override
      protected boolean isSplitable(JobContext context, Path file) {
         return false;
      }
   }

   public static class ByteRecordReader extends RecordReader<NullWritable, BytesWritable> {
      public static final Log log = new Log(ByteRecordReader.class);
      private FSDataInputStream fileIn;
      long length;
      long pos = 0;
      private BytesWritable value = new BytesWritable();
      private int maxsize = 1000;

      public ByteRecordReader() {
      }

      public void setMaxSize( int maxsize ) {
         this.maxsize = maxsize;
      }
      
      @Override
      public void initialize(InputSplit genericSplit,
              TaskAttemptContext context) throws IOException {
         FileSplit split = (FileSplit) genericSplit;
         Configuration job = context.getConfiguration();
         long start = split.getStart();
         length = split.getLength();
         final Path file = split.getPath();

         // open the file and seek to the start of the split
         FileSystem fs = file.getFileSystem(job);
         fileIn = fs.open(split.getPath());
         if (start != 0) {
            //--start;
            fileIn.seek(start);
         }
      }

      @Override
      public boolean nextKeyValue() throws IOException {
         byte buffer[] = new byte[(int)(maxsize * 0.9)];
         int read = fileIn.read(buffer, 0, buffer.length);
         value.set(buffer, 0, (read > 0)?read:0);
         pos += read;
         if (read == 0) {
            return false;
         } else {
            return true;
         }
      }

      @Override
      public NullWritable getCurrentKey() {
         return NullWritable.get();
      }

      @Override
      public BytesWritable getCurrentValue() {
         return value;
      }

      /**
       * Get the progress within the split
       */
      public float getProgress() {
         if (length == 0) {
            return 0.0f;
         } else {
            return Math.min(1.0f, (pos) / (float) (length));
         }
      }

      public synchronized void close() throws IOException {
         if (fileIn != null) {
            fileIn.close();
         }
      }

   }
}
