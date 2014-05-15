package io.github.repir.tools.Content;

import java.util.Collection;

public interface StructuredFileKeyInterface<R extends StructuredFileKeyValueRecord> {
   
   public R newRecord();
   
   public void openRead()  throws FileIntegrityException;

   public void closeRead();

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

   public R find(R r);
   
   public boolean exists(R r);
   
   public void write(R r);

   public void remove(Iterable<R> r);

   public void closeWrite();
   
   public void openAppend();
   
   public void openWrite();
   
   public Collection<R> getKeys();
}
