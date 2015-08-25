package io.github.htools.io.struct;

import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;

public interface StructuredFileIntID {

   public void openRead();

   public void closeRead();

   public void closeWrite();

   public void openWrite();

   public void read(int id) throws EOCException;

   public void find(int id) throws EOCException;

   public void setBufferSize(int size);

   public long getLength();

   public boolean hasNext();

   public boolean nextRecord();

   public boolean skipRecord();
   
   public long getOffset();
   
   public long getCeiling();
   
   public void readResident(int id) throws EOCException;
   
   public void readResident() throws EOCException;
   
   public boolean isReadResident();
   
   public void reuseBuffer();
}
