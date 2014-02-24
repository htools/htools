package io.github.repir.tools.Content;
import UnitTest.RecordSortCollisionTest;
import java.lang.reflect.Method;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.MathTools;

public abstract class RecordSortCollisionRecord extends RecordSortRecord {
   public static Log log = new Log(RecordSortCollisionRecord.class);
   private int bucketindex = -1;

   public RecordSortCollisionRecord( RecordSortCollision t) {
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
      ((RecordSortCollision)file).bucketindex.write(getBucketIndex());
      writeTempRecordData();
   }

   @Override
   protected void writeFinal() {
      int oldoffset = (int) file.getOffset();
      ((RecordSortCollision)file).hashfile.writeHash(getBucketIndex(), oldoffset, (int) file.getOffset());
      writeRecordData();
   }

   protected void writeTempRecordData() {
      writeRecordData();
   }

   protected abstract void writeRecordData();

   public abstract boolean equals(RecordSortCollisionRecord r);
}
