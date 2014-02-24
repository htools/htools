package io.github.repir.tools.Content;

import java.util.Collection;
import io.github.repir.tools.Lib.Log;
import java.util.HashMap;

/**
 * Implementation for a file with relatively small headers, to be loaded into
 * memory that contain the location of a larger data record in a different file.
 *
 * @author jer
 */
public abstract class RecordHeaderData<R extends RecordHeaderDataRecord> extends RecordBinary implements RecordHeaderInterface<R> {

   public Log log = new Log(RecordHeaderData.class);
   public HashMap<R, R> residenttable;

   public RecordHeaderData(Datafile basefile) {
      super(basefile);
   }

   @Override
   public abstract R newRecord();

   @Override
   public void openRead() {
      if (residenttable == null) {
         residenttable = new HashMap<R, R>();
         super.openRead();
         if (isReadOpen()) {
            setOffset(0);
            while (next()) {
               R r = newRecord();
               r.read(this);
               residenttable.put(r, r);
            }
         }
         super.closeRead();
      }
   }

   @Override
   public void closeRead() {
      residenttable = null;
   }

   @Override
   public Collection<R> getKeys() {
      return residenttable.values();
   }

   @Override
   public R find(R r) {
      openRead();
      return residenttable.get(r);
   }

   @Override
   public void write(R r) {
      residenttable.put(r, r);
   }

   @Override
   public void closeWrite() {
      super.openWrite();
      if (residenttable != null) {
         for (R r : residenttable.values()) {
            r.write(this);
         }
      }
      super.closeWrite();
      residenttable = null;
   }

   @Override
   public void openWriteAppend() {
      openRead();
      this.resetNextField();
   }

   @Override
   public void openWriteNew() {
      residenttable = new HashMap<R, R>();
      this.resetNextField();
   }

   @Override
   public void remove(Iterable<R> records) {
      openWriteAppend();
      for (R r : records) {
         residenttable.remove(r);
      }
      closeWrite();
   }
}
