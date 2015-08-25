package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;

public abstract class StructuredFileShortJumpTable extends StructuredFile implements StructuredFileIntID {

   public static Log log = new Log(StructuredFileShortJumpTable.class);
   public StructuredFileShortJumptableInternal jumparray;
   boolean loaded = false;
   private int id = 0;

   public StructuredFileShortJumpTable(Datafile df) {
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
      jumparray = new StructuredFileShortJumptableInternal(new Datafile(this.getDatafile().getSubFile(".jumparray")));
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
      nextRecord();
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
         skipRecord();
      }
   }
   
   public void loadMem() throws EOCException {
      if (!loaded) {
              this.reader = new BufferReaderWriter(getDatafile().readFully());
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
   public void readResident(int id) throws EOCException {
      loadMem();
   }
   
   @Override
   public void readResident() throws EOCException {
      loadMem();
   }
   
   public boolean isReadResident() {
      return loaded;
   }
   
   @Override
   public void reuseBuffer() {
   }
}
