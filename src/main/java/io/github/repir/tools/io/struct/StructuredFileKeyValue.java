package io.github.repir.tools.io.struct;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.FileIntegrityException;
import java.util.Collection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import io.github.repir.tools.lib.RandomTools;
import java.util.HashMap;

/**
 * Implementation for a file with relatively small headers, to be loaded into
 * memory that contain the location of a larger data record in a different file.
 *
 * @author jer
 */
public abstract class StructuredFileKeyValue<R extends StructuredFileKeyValueRecord> extends StructuredFile implements StructuredFileKeyInterface<R> {

   public Log log = new Log(StructuredFileKeyValue.class);
   public HashMap<R, R> residenttable;

   public StructuredFileKeyValue(Datafile basefile) {
      super(basefile);
   }

   @Override
   public abstract R newRecord();

   @Override
   public void openRead() throws FileIntegrityException {
      if (residenttable == null) {
         if (super.getLength() > 0) {
            R closingrecord = closingRecord();
            boolean isproperlyclosed = false;
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
            if (!isproperlyclosed) {
               throw new FileIntegrityException(PrintTools.sprintf("File integrity, file not ended properly: %s", this.getDatafile().getCanonicalPath()));
            }
         } else {
            residenttable = new HashMap<R, R>();
         }
      }
   }

   @Override
   public void closeRead() {
      residenttable = null;
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
      throw new RuntimeException("Cannot use setKeyBufferSize on a StucturedFileKeyValue, because Key and Value are integrated in one file. Use setDataBufferSize instead.");
   }

   @Override
   public void setDataBufferSize(int size) {
      setBufferSize(size);
   }

   @Override
   public int getKeyBufferSize() {
      throw new RuntimeException("Cannot use getKeyBufferSize on a StucturedFileKeyValue, because Key and Value are integrated in one file. Use setDataBufferSize instead.");
   }

   @Override
   public int getDataBufferSize() {
      return getBufferSize();
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
      openRead();
      return residenttable.get(r);
   }

   @Override
   public boolean exists(R r) {
      openRead();
      return residenttable.get(r) != null;
   }

   @Override
   public void write(R r) {
      residenttable.put(r, r);
   }

   @Override
   public void closeWrite() {
      super.openWrite();
      getDatafile().setReplication(1);
      if (residenttable != null) {
         for (R r : residenttable.values()) {
            r.write(this);
         }
      }
      closingRecord().write(this);
      super.closeWrite();
      residenttable = null;
   }

   @Override
   public void openAppend() {
      if (!getDatafile().hasLock())
         throw new RuntimeException(PrintTools.sprintf("Should lock file before append %s", getDatafile().getName()));
      openRead();
      this.resetNextField();
   }

   @Override
   public void openWrite() {
      residenttable = new HashMap<R, R>();
      this.resetNextField();
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
