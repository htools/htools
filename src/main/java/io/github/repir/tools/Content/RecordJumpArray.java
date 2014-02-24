package io.github.repir.tools.Content;

import java.io.EOFException;
import static io.github.repir.tools.Content.RecordJump2Array.log;
import io.github.repir.tools.Lib.Log;

/**
 * Stores an array of variable size records, so that a record can be found
 * relatively fast using a relatively small {@link RecordJump} table, that is
 * kept in a separate file. On {@link #openRead()}, the {@link RecordJump} table
 * is read into memory for fast access.
 * <p/>
 * @author jeroen
 */
public abstract class RecordJumpArray extends RecordBinary implements RecordIdentity {

   public static Log log = new Log(RecordJumpArray.class);
   public RecordJump jumparray;
   private int id;
   private boolean loaded;

   public RecordJumpArray(Datafile df) {
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
      jumparray = new RecordJump(new Datafile(this.getDatafile().getSubFile(".jumparray")));
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
      //log.info("hookRecordWrittern id %d offset %d", id, getOffset());
      jumparray.write(id++, this);
   }

   @Override
   public void openRead() {
      super.openRead();
      jumparray.openRead();
   }

   public void loadMem() {
      if (!loaded) {
         datafile.openRead();
         this.reader = new BufferReaderWriter(datafile.readFully());
         //datafile.closeRead();
         loaded = true;
      }
   }

   public void unloadMem() {
      if (loaded) {
         this.reader = null;
         loaded = false;
      }
   }

   @Override
   public void closeRead() {
      super.closeRead();
      jumparray.closeRead();
   }

   public void read(int id) {
      find(id);
      //log.info("read offset %d", this.datafile.getOffset());
      next();
   }

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
   
   @Override
   public void readResident(int id) throws EOFException {
      loadMem();
   }
   
   public boolean isReadResident() {
      return loaded;
   }
   
   @Override
   public void reset() {
      log.fatal("reset() not implemented");      
   }
}
