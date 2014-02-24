package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class RecordSortJumpArray extends RecordSort {

   public static Log log = new Log(RecordSortJumpArray.class);
   public BufferReaderWriter residenttable;
   int tablesize;
   RecordJump idfile;
   protected int id = 0;

   public RecordSortJumpArray(Datafile df) {
      super(df);
   }

   public void setTableSize(int tablesize) {
      this.tablesize = tablesize;
   }

   @Override
   public void closeWrite() {
      super.closeWrite();
      if (idfile != null) {
         idfile.closeWrite();
      }
   }

   @Override
   public void openWriteFinal() {
      log.info("openWriteFinal()");
      id = 0;
      idfile = new RecordJump(new Datafile(this.destfile.getSubFile(".jumparray")));
      idfile.openWrite();
      super.openWriteFinal();
      this.datafile.write(tablesize);
   }

   @Override
   public void openRead() {
      super.openRead();
      residenttable = new BufferReaderWriter(this.datafile.readFully());
      super.closeRead();
      try {
         tablesize = residenttable.readInt();
      } catch (EOFException ex) {
         log.fatalexception(ex, "openRead() residenttable %s datafile %s", residenttable, datafile);
      }
      idfile = new RecordJump(new Datafile(this.destfile.getSubFile(".jumparray")));
      idfile.openRead();
   }

   public RecordSortJumpArrayRecord find(int id) {
      //log.info("bucketindex %d", r.bucketindex);
      RecordSortJumpArrayRecord record = (RecordSortJumpArrayRecord) this.createRecord();
      long offset = idfile.getOffset(id);
      residenttable.setOffset(offset);
      int skip = idfile.getSkip(id);
      for (int i = 0; i <= skip; i++) {
         record.read();
      }
      return record;
   }

   @Override
   public RecordBinary clone() {
      RecordSortJumpArray tuple = null;
      try {
         Constructor<? extends RecordSortJumpArray> declaredConstructor = this.getClass().getDeclaredConstructor(Datafile.class);
         tuple = declaredConstructor.newInstance(new Datafile(this.getDatafile()));
         tuple.setTableSize(tablesize);
      } catch (Exception ex) {
         log.exception(ex, "clone() tablesize %d", tablesize);
      }
      return tuple;
   }
}
