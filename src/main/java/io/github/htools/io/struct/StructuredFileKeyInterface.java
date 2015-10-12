package io.github.htools.io.struct;

import io.github.htools.io.EOCException;
import java.io.IOException;
import java.util.Collection;

public interface StructuredFileKeyInterface<R extends StructuredFileKeyValueRecord> {
   
   public R newRecord();
   
   public void openRead()  throws IOException;

   public void closeRead() throws IOException;

   public void setKeyBufferSize(int size);

   public void setDataBufferSize(int size);

   public int getKeyBufferSize();
   
   public int getDataBufferSize();
   
   public boolean lock();
   
   public void unlock();
   
   public boolean hasLock();
   
   public void fillBuffer() throws EOCException;
   
   public long getOffset();
   
   /**
    * Internally used mechanism to prevent integrity failure. On HDFS files cannot
    * be locked, so to prevent files from being read or written while another
    * process is writing the file, a closing record is added to the end. If the
    * closing record is not found, reading is retried a few times, after that a
    * file integrity fatal error is giving.
    * @return just some record with fixed values that will never 
    */
   public R closingRecord();
   
   public long getCeiling();

   public R find(R r) throws IOException;
   
   public boolean exists(R r) throws IOException;
   
   public void write(R r) throws IOException;

   public void remove(Iterable<R> r) throws IOException;

   public void closeWrite() throws IOException;
   
   public void openAppend() throws IOException;
   
   public void openWrite() throws IOException;
   
   public Collection<R> getKeys() throws IOException;
}
