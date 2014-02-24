package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.util.ArrayList;
import java.util.TreeSet;
import io.github.repir.tools.Content.RecordSortRecord;

public abstract class RecordSort<R extends RecordSortRecord> extends RecordBinary {

   public static Log log = new Log(RecordSort.class);
   protected int segments = 0;
   protected Datafile destfile, tempfile;
   protected int cacherecords = 1000000;
   protected TreeSet<R> records;
   protected ArrayList<RecordSortReader> readers = new ArrayList<RecordSortReader>();

   public RecordSort(Datafile df) {
      super(df);
      destfile = df;
   }

   @Override
   public void hookRecordWritten() {
      if (writer == null) {
         //log.info("hookRecordWritten %s %d", writer, records.size());
         records.add(createRecord());
         checkFlush();
      }
   }

   private void checkFlush() {
      if (records.size() >= cacherecords) {
         flushSegment();
      }
   }

   public void setCacheRecords(int segmentsize) {
      if (this.cacherecords > segmentsize) {
         this.cacherecords = segmentsize;
         checkFlush();
      } else {
         this.cacherecords = segmentsize;
      }
   }

   public abstract R createRecord();

   @Override
   public void openWrite() {
      records = new TreeSet<R>();
      tempfile = this.getTempfile();
      tempfile.openWrite();
      tempfile.setBufferSize(1000000);
      super.setDatafile(null);
      super.openWrite();
   }

   public void openWriteFinal() {
      this.setDatafile(destfile);
      this.setBufferSize(1000000);
      super.openWrite();
   }

   public void openReadTemp() {
      super.openRead();
   }

   @Override
   public void closeWrite() {
      flushSegment();
      setDatafile(tempfile);
      super.closeWrite();
      merge();
   }

   public void flushSegment() {
      if (records.size() > 0) {
         setDatafile(tempfile);
         long segmentoffset = getOffset();
         for (R r : records) {
            r.setFile(this);
            r.write();
         }
         RecordSort segment = (RecordSort) this.clone();
         segment.setOffset(segmentoffset);
         segment.setCeiling(getOffset());
         //log.info("segment %d offset %d ceiling %d", segments, segment.getOffset(), segment.getCeiling());
         readers.add(new RecordSortReader(segment, segments++));
         setDatafile(null);
         records.clear();
      }
   }

   public final void merge() {
      this.openWriteFinal();
      TreeSet<RecordSortReader> orderedreaders = new TreeSet<RecordSortReader>();
      int show = 0;
      for (RecordSortReader r : readers) {
         r.index.setBufferSize(50000000 / readers.size());
         //log.info("new datafile status %s %s %d %d", r.index.datafile.status, r.index.datafile.getFullPath(), r.index.datafile.getOffset(), r.index.datafile.getCeiling());
         r.openRead();
         //log.info("new datafile status %s %s %d %d", r.index.datafile.status, r.index.datafile.getFullPath(), r.index.datafile.getOffset(), r.index.datafile.getCeiling());
         if (r.next()) {
            orderedreaders.add(r);
         }
      }
      RecordSortReader r;
      while (orderedreaders.size() > 0) {
         r = orderedreaders.pollFirst();
         RecordSortRecord record = r.index.createRecord();
//         if (record instanceof RecordSortHashRecord && orderedreaders.size() > 0) {
//            RecordSortReader r2 = orderedreaders.first();
//            RecordSortHashRecord rec2 = (RecordSortHashRecord) r2.index.createRecord();
//            if (((RecordSortHashRecord)record).getBucketIndex() > rec2.getBucketIndex()) {
//               log.crash("unsoted %d %d %d %d", ((RecordSortHashRecord)record).getBucketIndex(), rec2.getBucketIndex(), record.file.datafile.getOffset(), rec2.file.datafile.getOffset());
//            }
//         }
         record.setFile(this);
         record.writeFinal();
         if (r.next()) {
            orderedreaders.add(r);
         }
      }
      destfile.closeWrite();
      //tempfile.delete();
   }

   public boolean initSearchKey() {
      this.openRead();
      return true;
   }

   public abstract int compare(RecordSortRecord o1, RecordSortRecord o2);

   protected int compareKeys(RecordSortRecord o1, RecordSortRecord o2) {
      int comp = compare(o1, o2);
      return (comp != 0) ? comp : 1;
   }

   public abstract int compare(RecordSort o1, RecordSort o2);

   protected int compareKeys(RecordSort o1, RecordSort o2) {
      int comp = compare(o1, o2);
      return (comp != 0) ? comp : 1;
   }

   public String getFilename(String filename) {
      return filename + ".tree";
   }

   public static int compareBytes(byte[] b1, int s1, int l1,
           byte[] b2, int s2, int l2) {
      int end1 = s1 + l1;
      int end2 = s2 + l2;
      for (int i = s1, j = s2; i < end1 && j < end2; i++, j++) {
         int a = (b1[i] & 0xff);
         int b = (b2[j] & 0xff);
         if (a != b) {
            return a - b;
         }
      }
      //log.info("compareBytes %s %s", Lib.Array.toString(b1), Lib.Array.toString(b2));
      return l1 - l2;
   }
}
