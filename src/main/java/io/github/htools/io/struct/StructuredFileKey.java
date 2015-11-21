package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.io.FileIntegrityException;
import java.util.Collection;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.RandomTools;
import java.util.HashMap;

/**
 * Implementation for a file with relatively small headers, to be loaded into
 * memory that contain the location of a larger data record in a different file.
 *
 * @author jer
 */
public abstract class StructuredFileKey<R extends StructuredFileKeyRecord, D extends StructuredFile> extends StructuredFile implements StructuredFileKeyInterface<R> {

   public Log log = new Log(StructuredFileKey.class);
   public D datastorage;
   public LongField offset = this.addLong("offset");
   public IntField length = this.addInt("length");
   public HashMap<R, R> residenttable;

   public StructuredFileKey(Datafile basefile) {
      super(basefile);
   }

   protected abstract D createDatafile(Datafile df);

   @Override
   public abstract R newRecord();

   @Override
   public void openRead() {
      if (residenttable == null) {
         if (super.getLength() > 0) {
            R closingrecord = closingRecord();
            boolean isproperlyclosed = false;
            boolean containsdata = false;
            //for (int attempt = 0; !isclosed && attempt < 10; attempt++) {
            int attempt = 0;
            do {
               if (residenttable != null) {
                  log.sleep(RandomTools.getInt(10000));
               }
               residenttable = new HashMap<R, R>();
               super.openRead();
               if (isReadOpen()) {
                  setOffset(0);
                  while (nextRecord()) {
                     containsdata = true;
                     R r = newRecord();
                     r.read(this);
                     if (r.equals(closingrecord)) {
                        isproperlyclosed = true;
                     } else {
                        residenttable.put(r, r);
                        isproperlyclosed = false;
                     }
                  }
               }
               super.closeRead();
            } while (attempt++ <= 20 && !isproperlyclosed);
            if (containsdata && !isproperlyclosed) {
               throw new FileIntegrityException(PrintTools.sprintf("File integrity, file not ended properly: %s", this.getDatafile().getCanonicalPath()));
            }
         } else {
            residenttable = new HashMap<R, R>();
         }
         datastorage = createDatafile(new Datafile(getDatafile().getSubFile(".data")));
      }
   }

   @Override
   public void closeRead() {
      residenttable = null;
      datastorage = null;
      super.closeRead();
   }

   @Override
   public boolean lock() {
      return getDatafile().lock();
   }
   
   @Override
   public boolean hasLock() {
      return getDatafile().hasLock();
   }
   
   @Override
   public void unlock() {
      getDatafile().unlock();
   }
   
   @Override
   public void setKeyBufferSize(int size) {
      super.setBufferSize(size);
   }

   @Override
   public void setBufferSize(int size) {
      throw new RuntimeException("Cannot use setBufferSize() on a StructuredKeyFile, specifically use setKeyBufferSize() and setDataBufferSize() to set the buffer sizes of the key and data files.");
   }

   @Override
   public void setDataBufferSize(int size) {
      datastorage.setBufferSize(size);
   }

   @Override
   public int getKeyBufferSize() {
      return getBufferSize();
   }

   @Override
   public int getBufferSize() {
      throw new RuntimeException("Cannot use getBufferSize() on a StructuredKeyFile, specifically use getKeyBufferSize() and getDataBufferSize() to get the buffer sizes of the key and data files.");
   }

   @Override
   public int getDataBufferSize() {
      return datastorage.getBufferSize();
   }

   @Override
   public Collection<R> getKeys() {
      if (residenttable == null) {
         openRead();
      }
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

   @Override
   public boolean exists(R r) {
      r = residenttable.get(r);
      return (r != null);
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
      r.write(datastorage);
      residenttable.put(r, r);
   }

   @Override
   public void closeWrite() {
      datastorage.closeWrite();
      datastorage.getDatafile().setReplication(1);
      super.openWrite();
      getDatafile().setReplication(1);
      for (R r : residenttable.values()) {
         r.write(this);
      }
      closingRecord().write(this);
      super.closeWrite();
   }

   @Override
   public void openAppend() {
      if (!getDatafile().hasLock())
         throw new RuntimeException(PrintTools.sprintf("Should lock file before append: %s", getDatafile().getName()));
      if (residenttable == null) {
         openRead();
      }
      this.resetNextField();
      datastorage.openAppend();
   }

   @Override
   public void openWrite() {
      residenttable = new HashMap<R, R>();
      this.resetNextField();
      datastorage.openAppend();
   }

   @Override
   public void remove(Iterable<R> records) {
      openAppend();
      for (R r : records) {
         residenttable.remove(r);
      }
      closeWrite();
   }
}
