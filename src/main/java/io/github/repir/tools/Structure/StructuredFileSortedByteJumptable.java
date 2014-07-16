package io.github.repir.tools.Structure;

import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.EOCException;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class StructuredFileSortedByteJumptable extends StructuredFileSort {

   public static Log log = new Log(StructuredFileSortedByteJumptable.class);
   public BufferReaderWriter residenttable;
   int tablesize;
   StructuredFileByteJumptableInternal idfile;
   protected int id = 0;

   public StructuredFileSortedByteJumptable(Datafile df) {
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
      idfile = new StructuredFileByteJumptableInternal(new Datafile(this.destfile.getSubFile(".jumparray")));
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
      } catch (EOCException ex) {
         log.fatalexception(ex, "openRead() residenttable %s datafile %s", residenttable, datafile);
      }
      idfile = new StructuredFileByteJumptableInternal(new Datafile(this.destfile.getSubFile(".jumparray")));
      idfile.openRead();
   }

   public StructuredFileSortJumptableRecord find(int id) {
      //log.info("bucketindex %d", r.bucketindex);
      StructuredFileSortJumptableRecord record = (StructuredFileSortJumptableRecord) this.createRecord();
      long offset = idfile.getOffset(id);
      residenttable.setOffset(offset);
      int skip = idfile.getSkip(id);
      for (int i = 0; i <= skip; i++) {
         record.read();
      }
      return record;
   }

   @Override
   public StructuredFile clone() {
      StructuredFileSortedByteJumptable tuple = null;
      try {
         Constructor<? extends StructuredFileSortedByteJumptable> declaredConstructor = this.getClass().getDeclaredConstructor(Datafile.class);
         tuple = declaredConstructor.newInstance(new Datafile(this.getDatafile()));
         tuple.setTableSize(tablesize);
      } catch (Exception ex) {
         log.exception(ex, "clone() tablesize %d", tablesize);
      }
      return tuple;
   }
}
