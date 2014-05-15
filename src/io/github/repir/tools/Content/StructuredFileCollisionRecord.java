package io.github.repir.tools.Content;
import io.github.repir.tools.Lib.Log;

public abstract class StructuredFileCollisionRecord extends StructuredFileSortRecord {
   public static Log log = new Log(StructuredFileCollisionRecord.class);
   private int bucketindex = -1;

   public StructuredFileCollisionRecord( StructuredFileCollision t) {
      super(t);
   }

   public int getBucketIndex() {
      if (bucketindex == -1)
          bucketindex = hashCode() & (file.getBucketCapacity() - 1);
      return bucketindex;
   }
   
   public abstract int hashCode();
   
   @Override
   public void write() {
      ((StructuredFileCollision)file).bucketindex.write(getBucketIndex());
      writeTempRecordData();
   }

   @Override
   protected void writeFinal() {
      int oldoffset = (int) file.getOffset();
      ((StructuredFileCollision)file).hashfile.writeHash(getBucketIndex(), oldoffset, (int) file.getOffset());
      writeRecordData();
   }

   protected void writeTempRecordData() {
      writeRecordData();
   }

   protected abstract void writeRecordData();

   public abstract boolean equals(StructuredFileCollisionRecord r);
}
