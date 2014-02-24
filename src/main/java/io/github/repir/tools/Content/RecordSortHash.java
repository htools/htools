package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class RecordSortHash<R extends RecordSortHashRecord> extends RecordSort {

   public static Log log = new Log(RecordSortHash.class);
   protected IntField hashcode = this.addInt("hashcode");
   public HashMap<RecordSortHashRecord, RecordSortHashRecord> memorytable;
   int tablesize;
   public RecordHash hashfile;

   public RecordSortHash(Datafile df, int tablesize) {
      super(df);
      setTableSize(tablesize);
   }

   @Override
   public abstract RecordBinary clone();

   public HashMap<RecordSortHashRecord, RecordSortHashRecord> getMemoryTable() {
      if (memorytable == null) {
         memorytable = new HashMap<RecordSortHashRecord, RecordSortHashRecord>(tablesize);
         setOffset(0);
         openRead();
         while (next()) {
            RecordSortHashRecord record = (RecordSortHashRecord) createRecord();
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
      hashfile = new RecordHash(new Datafile(this.destfile.getSubFile(".hash")), tablesize);
      hashfile.openWrite();
      this.remove(hashcode);
      super.openWriteFinal();
   }

   @Override
   public void openRead() {
      if (datafile.isClosed() && datafile.exists()) {
         this.remove(hashcode);
         this.setBufferSize(1000);
         super.openRead();
         hashfile = new RecordHash(new Datafile(this.destfile.getSubFile(".hash")), tablesize);
         hashfile.setBufferSize(8);
         hashfile.openRead();
      }
   }

   @Override
   public int compare(RecordSort o1, RecordSort o2) {
      int comp = ((RecordSortHash) o1).hashcode.value - ((RecordSortHash) o2).hashcode.value;
      return (comp != 0) ? comp : 1;
   }

   @Override
   public int compare(RecordSortRecord o1, RecordSortRecord o2) {
      int comp = ((RecordSortHashRecord) o1).getBucketIndex() - ((RecordSortHashRecord) o2).getBucketIndex();
      return (comp != 0) ? comp : 1;
   }

   public final ArrayList<R> listHashCode(R record) {
      ArrayList<R> list = new ArrayList<R>();
      if (gotoBucket(record.getBucketIndex())) {
         while (next()) {
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
      if (datafile.isClosed()) {
         openRead();
      }
      if (datafile.isReadOpen()) {
         try {
            RecordSortHash.this.hashfile.setOffset(bucketindex * 8);
            //log.info("gotoHash( %d ) offset %d", bucketindex, RecordSortHash.this.hashfile.getOffset());
            long offset = RecordSortHash.this.hashfile.offset.read();
            if (offset >= 0) {
               RecordSortHash.this.setOffset(offset);
               return true;
            }
         } catch (EOFException ex) {
            log.exception(ex, "gotoHash(%d) could not read offset", bucketindex);
         }
      }
      return false;
   }
}
