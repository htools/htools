package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;

public interface RecordIdentity {

   public void openRead();

   public void closeRead();

   public void closeWrite();

   public void openWrite();

   public void read(int id) throws EOFException;

   public void find(int id) throws EOFException;

   public void setBufferSize(int size);

   public long getFilesize();

   public boolean hasNext();

   public boolean next();

   public boolean skip();
   
   public long getOffset();
   
   public long getCeiling();
   
   public void readResident(int id) throws EOFException;
   
   public boolean isReadResident();
   
   public void reset();
}
