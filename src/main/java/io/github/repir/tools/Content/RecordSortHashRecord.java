package io.github.repir.tools.Content;

import java.lang.reflect.Method;
import java.util.HashMap;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

public abstract class RecordSortHashRecord extends RecordSortRecord {
   public static Log log = new Log(RecordSortHashRecord.class);
   private int bucketindex = -1;

   public RecordSortHashRecord(RecordSortHash t) {
      super(t);
   }

   public void setFile(RecordSort file) {
      super.setFile(file);
      bucketindex = -1;
   }
   
   @Override
   public final void write() {
      ((RecordSortHash) file).hashcode.write(getBucketIndex());
      writeRecordData();
   }

   
   /**
    * Removes the value part of a record while keeping the key intact. This is
    * useful for features with large records, for which a collection may not
    * fit into memory. When appending such records, the records can be appended
    * while iterating, destroying the value and only keeping the keys to compare
    * existing records against.
    */
   public void dumpValue() {
      // an implementation is only needed for very large hashrecords
   }
   
   @Override
   public final void writeFinal() {
      //log.info("writeHash file %s bucket %d offset %d", file.datafile.getFilename(), getBucketIndex(), file.getOffset());
      //if (getBucketIndex() < ((RecordSortHash) file).hashfile.currentbucketindex)
      //   log.info("%s", this.toString());
      ((RecordSortHash) file).hashfile.writeHash(getBucketIndex(), file.getOffset());
      writeRecordData();
   }

   protected abstract void writeRecordData();

   @Override
   public abstract int hashCode();
   
   public int getBucketIndex() {
      if (bucketindex == -1) {
         bucketindex = hashCode() & (file.getBucketCapacity() - 1);
      }
      return bucketindex;
   }
   
   public final RecordSortHashRecord clone(RecordBinary t) {
      RecordSortHashRecord record = null;
      try {
         Method declaredConstructor = file.getClass().getDeclaredMethod("createRecord");
         record = (RecordSortHashRecord) declaredConstructor.invoke(file);
      } catch (Exception ex) {
         log.exception(ex, "clone( %s )", t);
      }
      return record;
   }


   public abstract boolean equals(Object r);

   public final RecordSortHashRecord findMem() {
      HashMap table = ((RecordSortHash)file).getMemoryTable();
      return (RecordSortHashRecord)table.get(this);
   }
   
   public final RecordSortHashRecord find() {
      ((RecordSortHash) file).resetNextField();
      //log.info("file %d %b %d", getBucketIndex(), ((RecordSortHash) file).gotoBucket(getBucketIndex()), file.getOffset());
      if (((RecordSortHash) file).gotoBucket(getBucketIndex())) {
         while (((RecordSortHash) file).next()) {
            RecordSortHashRecord r = (RecordSortHashRecord) file.createRecord();
            equals(r);
            if (r.getBucketIndex() != getBucketIndex()) {
               break;
            }
            if (equals(r)) {
               return r;
            }
         }
      }
      return null;
   }
}
