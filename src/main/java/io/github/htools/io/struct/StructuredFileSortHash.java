package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract StructuredFile that contains a hashcode as a fixed field, which can be
 * extended with other field. The hashcode is used to automatically sort the data 
 * and generate a separate lookup table for fast random access. 
 * @author jer
 * @param <R> 
 */
public abstract class StructuredFileSortHash<R extends StructuredFileSortHashRecord> extends StructuredFileSort {

   public static Log log = new Log(StructuredFileSortHash.class);
   protected IntField hashcode = this.addInt("hashcode");
   public HashMap<StructuredFileSortHashRecord, StructuredFileSortHashRecord> memorytable;
   int tablesize;
   public StructuredFileHash hashfile;

   public StructuredFileSortHash(Datafile df, int tablesize) {
      super(df);
      setTableSize(tablesize);
   }

   @Override
   public abstract StructuredFile clone();

   public HashMap<StructuredFileSortHashRecord, StructuredFileSortHashRecord> getMemoryTable() {
      if (memorytable == null) {
         memorytable = new HashMap<StructuredFileSortHashRecord, StructuredFileSortHashRecord>(tablesize);
         setOffset(0);
         openRead();
         while (nextRecord()) {
            StructuredFileSortHashRecord record = (StructuredFileSortHashRecord) createRecord();
            memorytable.put(record, record);
         }
         closeRead();
      }
      return memorytable;
   }

   @Override
   public void closeWrite() {
      super.closeWrite();
      if (hashfile != null) {
         hashfile.closeWrite();
      }
   }

   public void setTableSize(int tablesize) {
      this.tablesize = tablesize;
      this.setCapacity(tablesize);
   }

   public int getTableSize() {
      return tablesize;
   }

   @Override
   public void openWriteFinal() {
      hashfile = new StructuredFileHash(new Datafile(this.destfile.getSubFile(".hash")), tablesize);
      hashfile.openWrite();
      this.remove(hashcode);
      super.openWriteFinal();
   }

   @Override
   public void openRead() {
      if (getDatafile().isClosed() && getDatafile().exists()) {
         this.remove(hashcode);
         this.setBufferSize(1000);
         super.openRead();
         hashfile = new StructuredFileHash(new Datafile(this.destfile.getSubFile(".hash")), tablesize);
         hashfile.setBufferSize(8);
         hashfile.openRead();
      }
   }

   @Override
   public int compare(StructuredFileSort o1, StructuredFileSort o2) {
      int comp = ((StructuredFileSortHash) o1).hashcode.value - ((StructuredFileSortHash) o2).hashcode.value;
      return (comp != 0) ? comp : 1;
   }

   @Override
   public int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
      int comp = ((StructuredFileSortHashRecord) o1).getBucketIndex() - ((StructuredFileSortHashRecord) o2).getBucketIndex();
      return (comp != 0) ? comp : 1;
   }

   public final ArrayList<R> listHashCode(R record) {
      ArrayList<R> list = new ArrayList<R>();
      if (gotoBucket(record.getBucketIndex())) {
         while (nextRecord()) {
            R r = (R) this.createRecord();
            if (r.getBucketIndex() != record.getBucketIndex()) {
               break;
            }
            list.add(r);
         }
      }
      return list;
   }

   public final boolean gotoBucket(int bucketindex) {
      if (getDatafile().isClosed()) {
         openRead();
      }
      if (getDatafile().isReadOpen()) {
         try {
            StructuredFileSortHash.this.hashfile.setOffset(bucketindex * 8);
            //log.info("gotoHash( %d ) offset %d", bucketindex, StructuredFileSortHash.this.hashfile.getOffset());
            long offset = StructuredFileSortHash.this.hashfile.offset.read();
            if (offset >= 0) {
               StructuredFileSortHash.this.setOffset(offset);
               return true;
            }
         } catch (EOCException ex) {
            log.exception(ex, "gotoHash(%d) could not read offset", bucketindex);
         }
      }
      return false;
   }
}
