package io.github.repir.tools.Content;

import java.io.EOFException;
import io.github.repir.tools.Lib.Log;

public abstract class RecordJump2Array extends RecordBinary implements RecordIdentity {

   public static Log log = new Log(RecordJump2Array.class);
   public RecordJump2 jumparray;
   boolean loaded = false;
   private int id = 0;

   public RecordJump2Array(Datafile df) {
      super(df);
   }

   @Override
   public void closeWrite() {
      super.closeWrite();
      if (jumparray != null) {
         jumparray.closeWrite();
      }
   }

   @Override
   public void setDatafile(Datafile df) {
      super.setDatafile(df);
      jumparray = new RecordJump2(new Datafile(this.getDatafile().getSubFile(".jumparray")));
      id = 0;
      loaded = false;
   }

   @Override
   public void openWrite() {
      super.openWrite();
      //log.info("openWrite()");
      id = 0;
      jumparray.openWrite();
   }

   @Override
   public void hookRecordWritten() {
      jumparray.write(id++, this);
   }

   @Override
   public void openRead() {
      super.openRead();
      jumparray.openRead();
   }

   @Override
   public void closeRead() {
      super.closeRead();
      jumparray.closeRead();
      unloadMem();
   }

   @Override
   public void read(int id) {
      find(id);
      //log.info("read offset %d", this.datafile.getOffset());
      next();
   }

   @Override
   public void find(int id) {
      jumparray.openRead();
      long offset = jumparray.getOffset(id);
      //log.info("find id %d offset %d", id, offset);
      this.setOffset(offset);
      super.openRead();
      int skip = jumparray.getSkip(id);
      for (int s = 0; s < skip; s++) {
         skip();
      }
   }
   
   public void loadMem() {
      if (!loaded) {
         datafile.openRead();
         this.reader = new BufferReaderWriter(datafile.readFully());
         //datafile.closeRead();
         loaded = true;
      }
   }

   public boolean isLoadedInMem() {
      return loaded;  
   }
   
   public void unloadMem() {
      if (loaded) {
         this.reader = null;
         loaded = false;
      }
   }
   
   @Override
   public void readResident(int id) throws EOFException {
      loadMem();
   }
   
   public boolean isReadResident() {
      return loaded;
   }
   
   @Override
   public void reset() {
   }
}
