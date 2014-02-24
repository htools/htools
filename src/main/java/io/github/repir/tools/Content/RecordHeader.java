package io.github.repir.tools.Content;

import java.util.Collection;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * Implementation for a file with relatively small headers, to be loaded into memory
 * that contain the location of a larger data record in a different file.
 * @author jer
 */
public abstract class RecordHeader<R extends RecordHeaderRecord, D extends RecordBinary> extends RecordBinary implements RecordHeaderInterface<R> {

   public Log log = new Log(RecordHeader.class);
   public D datastorage;
   public LongField offset = this.addLong("offset");
   public IntField length = this.addInt("length");
   public HashMap<R,R> residenttable;

   public RecordHeader(Datafile basefile) {
      super(basefile);
   }
   
   protected abstract D createDatafile(Datafile df);
   
   @Override
   public abstract R newRecord();
   
   @Override
   public void openRead() {
      if (residenttable == null) {
      residenttable = new HashMap<R,R>();
      setOffset(0);
      super.openRead();
      while (next()) {
         R r = newRecord();
         r.read(this);
         residenttable.put(r, r);
      }
      datastorage = createDatafile(new Datafile(datafile.getSubFile(".data")));
      }
   }

   @Override
   public void closeRead() {
      residenttable = null;
      datastorage = null;
      super.closeRead();
   }
   
   @Override
   public Collection<R> getKeys() {
      return residenttable.values();
   }
   
   @Override
   public R find(R r) {
      r = residenttable.get(r);
      if (r != null) {
         read(r);
      }
      return r;
   }
   
   protected void read(R r) {
      datastorage.setOffset(r.offset);
      datastorage.setCeiling(r.offset + r.length);
      datastorage.openRead();
      r.getData(datastorage);
   }

   public D setPos(R r) {
      datastorage.setOffset(r.offset);
      datastorage.setCeiling(r.offset + r.length);
      datastorage.openRead();
      return datastorage;
   }

   public void write(R r) {
      r.write( datastorage );
      residenttable.put(r, r);
   }

   @Override
   public void closeWrite() {
      datastorage.closeWrite();
      super.openWrite();
      for (R r : residenttable.values()) {
         r.write(this);
      }
      super.closeWrite();
   }
   
   @Override
   public void openWriteAppend() {
      if (residenttable == null)
         openRead();
      this.resetNextField();
      datastorage.openAppend();
   }
   
   @Override
   public void openWriteNew() {
      residenttable = new HashMap<R,R>();
      this.resetNextField();
      datastorage.openAppend();
   }
   
   @Override
   public void remove(Iterable<R> records) {
      openWriteAppend();
      for (R r : records)
         residenttable.remove(r);
      closeWrite();
   }

}
