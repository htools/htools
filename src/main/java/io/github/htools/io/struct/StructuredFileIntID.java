package io.github.htools.io.struct;

import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.IOException;

public interface StructuredFileIntID {

   public void openRead() throws IOException ;

   public void closeRead() throws IOException;

   public void closeWrite() throws IOException;

   public void openWrite() throws IOException;

   public void read(int id) throws EOCException, IOException;

   public void find(int id) throws EOCException, IOException;

   public void setBufferSize(int size);

   public long getLength();

   public boolean hasNext();

   public boolean nextRecord() throws IOException;

   public boolean skipRecord();
   
   public long getOffset();
   
   public long getCeiling();
   
   public void readResident(int id) throws EOCException, IOException;
   
   public void readResident() throws EOCException, IOException;
   
   public boolean isReadResident();
   
   public void reuseBuffer() throws IOException;
}
