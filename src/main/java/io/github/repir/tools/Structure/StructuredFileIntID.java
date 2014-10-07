package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Lib.Log;

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
