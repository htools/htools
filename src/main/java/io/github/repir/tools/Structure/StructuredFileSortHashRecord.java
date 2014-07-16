package io.github.repir.tools.Structure;

import java.lang.reflect.Method;
import java.util.HashMap;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

public abstract class StructuredFileSortHashRecord extends StructuredFileSortRecord {
   public static Log log = new Log(StructuredFileSortHashRecord.class);
   private int bucketindex = -1;

   public StructuredFileSortHashRecord(StructuredFileSortHash t) {
      super(t);
   }

   public void setFile(StructuredFileSort file) {
      super.setFile(file);
      bucketindex = -1;
   }
   
   @Override
   public final void write() {
      ((StructuredFileSortHash) file).hashcode.write(getBucketIndex());
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
      //if (getBucketIndex() < ((StructuredFileSortHash) file).hashfile.currentbucketindex)
      //   log.info("%s", this.toString());
      ((StructuredFileSortHash) file).hashfile.writeHash(getBucketIndex(), file.getOffset());
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
   
   public final StructuredFileSortHashRecord clone(StructuredFile t) {
      StructuredFileSortHashRecord record = null;
      try {
         Method declaredConstructor = file.getClass().getDeclaredMethod("createRecord");
         record = (StructuredFileSortHashRecord) declaredConstructor.invoke(file);
      } catch (Exception ex) {
         log.exception(ex, "clone( %s )", t);
      }
      return record;
   }


   public abstract boolean equals(Object r);

   public final StructuredFileSortHashRecord findMem() {
      HashMap table = ((StructuredFileSortHash)file).getMemoryTable();
      return (StructuredFileSortHashRecord)table.get(this);
   }
   
   public final StructuredFileSortHashRecord find() {
      ((StructuredFileSortHash) file).resetNextField();
      //log.info("file %d %b %d", getBucketIndex(), ((StructuredFileSortHash) file).gotoBucket(getBucketIndex()), file.getOffset());
      if (((StructuredFileSortHash) file).gotoBucket(getBucketIndex())) {
         while (((StructuredFileSortHash) file).next()) {
            StructuredFileSortHashRecord r = (StructuredFileSortHashRecord) file.createRecord();
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
