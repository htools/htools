package io.github.repir.tools.Content;

import java.io.EOFException;
import java.util.Collection;

public interface RecordHeaderInterface<R extends RecordHeaderDataRecord> {
   
   public R newRecord();
   
   public void openRead();

   public void closeRead();

   public void setBufferSize(int size);

   public int getBufferSize();
   
   public void fillBuffer() throws EOFException;
   
   public long getOffset();
   
   public long getCeiling();

   public R find(R r);
   
   public void write(R r);

   public void remove(Iterable<R> r);

   public void closeWrite();
   
   public void openWriteAppend();
   
   public void openWriteNew();
   
   public Collection<R> getKeys();
}
