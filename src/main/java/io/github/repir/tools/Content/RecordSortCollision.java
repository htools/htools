 package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class RecordSortCollision extends RecordSort {

   public static Log log = new Log(RecordSortCollision.class);
   protected IntField bucketindex = this.addInt("hashcode");
   BufferReaderWriter residenttable;
   int tablesize;
   RecordCollision hashfile;

   public RecordSortCollision(Datafile df) {
      super(df);
      setLoadFactor(3);
   }

   public abstract RecordBinary clone();
   
   public void setTableSize(int tablesize) {
      this.tablesize = tablesize;
      this.setCapacity(tablesize);
   }

   public int getTableSize() {
      return tablesize;
   }
   
   @Override
   public void closeWrite() {
      super.closeWrite();
      if (hashfile != null) {
         hashfile.closeWrite(bucketcapacity);
      }
   }

   @Override
   public void openWriteFinal() {
      hashfile = new RecordCollision(new Datafile(this.destfile.getSubFile(".hash")));
      hashfile.openWrite();
      this.remove(bucketindex);
      super.openWriteFinal();
      this.datafile.write(bucketcapacity);
   }

   @Override
   public void openRead() {
      this.remove(bucketindex);
      super.openRead();
      residenttable = new BufferReaderWriter(this.datafile.readFully());
      super.closeRead();
      try {
         bucketcapacity = residenttable.readInt();
      } catch (EOFException ex) {
         log.fatalexception(ex, "openRead() bucketindex %d residenttable %s", bucketindex, residenttable);
      }
      hashfile = new RecordCollision(new Datafile(this.destfile.getSubFile(".hash")));
      hashfile.openRead();
   }

   @Override
   public int compare(RecordSort o1, RecordSort o2) {
      int comp = ((RecordSortCollision) o1).bucketindex.value - ((RecordSortCollision) o2).bucketindex.value;
      if (comp == 0) {
         comp = secondaryCompare(o1, o2);
      }
      return (comp != 0) ? comp : 1;
   }

   public abstract int secondaryCompare(RecordSort o1, RecordSort o2);

   @Override
   public int compare(RecordSortRecord o1, RecordSortRecord o2) {
      int comp = ((RecordSortCollisionRecord) o1).getBucketIndex() - ((RecordSortCollisionRecord) o2).getBucketIndex();
      if (comp == 0) {
         comp = secondaryCompare(o1, o2);
      }
      return (comp != 0) ? comp : 1;
   }

   public abstract int secondaryCompare(RecordSortRecord o1, RecordSortRecord o2);

   public RecordSortCollisionRecord find(RecordSortCollisionRecord r) {
      //log.info("bucketindex %d", r.bucketindex);
      long offset = hashfile.getOffset(r.getBucketIndex());
      residenttable.setOffset(offset);
      return find(residenttable, r);
   }

   public abstract RecordSortCollisionRecord find(BufferReaderWriter table, RecordSortCollisionRecord r);
}
