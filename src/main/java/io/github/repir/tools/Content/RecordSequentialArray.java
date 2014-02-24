package io.github.repir.tools.Content;

import java.io.EOFException;

public class RecordSequentialArray extends RecordBinary implements RecordIdentity {

   public RecordSequentialIndex index;

   public RecordSequentialArray(Datafile df) {
      super(df);
      index = new RecordSequentialIndex(this);
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
   public void read(int id) throws EOFException {
      find(id);
      next();
   }

   @Override
   public void find(int id) throws EOFException {
      index.openRead();
      index.find(id);
      super.openRead();
   }

   @Override
   public void readResident(int id) throws EOFException {
      find(id);
      setBufferSize((int)(this.getCeiling() - this.getOffset()));
      super.openRead();
      reader.fillBuffer();
   }
   
   @Override
   public boolean isReadResident() {
      return getBufferSize() == (int)(this.getCeiling() - this.getOffset());
   }

   @Override
   public void reset() {
      reader.reset();
   }
}
