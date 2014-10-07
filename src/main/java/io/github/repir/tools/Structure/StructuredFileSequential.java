package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.EOCException;

/**
 * Records in this structure are identified by a sequential ID, which is automatically
 * used in an index, that allows fast random access. This StructuredFile is 
 * intended for storage of large records.
 * @author jer
 */
public class StructuredFileSequential extends StructuredFile implements StructuredFileIntID {

   public StructuredFileSequentialIndex index;

   public StructuredFileSequential(Datafile df) {
      super(df);
      index = new StructuredFileSequentialIndex(this);
   }

   /**
    * The offset file is intended to be read into memory, to quickly resolve the
    * offsets of records.
    */
   @Override
   public void openRead() {
      super.openRead();
      index.openRead();
   }

   public void closeRead() {
      super.closeRead();
      index.closeRead();
   }

   @Override
   public void openWrite() {
      super.openWrite();
      index.openWrite();
   }

   @Override
   public void closeWrite() {
      super.closeWrite();
      index.closeWrite();
   }

   @Override
   public void hookRecordWritten() {
      index.writeRecordOffset(this);
   }

   @Override
   public void read(int id) throws EOCException {
      find(id);
      nextRecord();
   }

   @Override
   public void find(int id) throws EOCException {
      index.openRead();
      index.find(id);
      super.openRead();
   }

   public long findOffset(int id) throws EOCException {
      index.openRead();
      return index.findOffset(id);
   }

   @Override
   public void readResident(int id) throws EOCException {
      find(id);
      setBufferSize((int)(this.getCeiling() - this.getOffset()));
      super.openRead();
      reader.fillBuffer();
   }
   
   @Override
   public void readResident() throws EOCException {
      setBufferSize((int)(this.getLength()));
      super.openRead();
      reader.fillBuffer();
   }
   
   @Override
   public boolean isReadResident() {
      return getBufferSize() == (int)(this.getCeiling() - this.getOffset());
   }

   @Override
   public void reuseBuffer() {
      reader.reuseBuffer();
   }
}
