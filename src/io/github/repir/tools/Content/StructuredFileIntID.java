package io.github.repir.tools.Content;

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

   public boolean next();

   public boolean skip();
   
   public long getOffset();
   
   public long getCeiling();
   
   public void readResident(int id) throws EOCException;
   
   public void readResident() throws EOCException;
   
   public boolean isReadResident();
   
   public void reset();
}
