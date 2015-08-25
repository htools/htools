package io.github.htools.io.struct;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Abstract class for implementations of StructuredFiles that will automatically 
 * physically sort the data written.
 * @author jer
 * @param <R> 
 */
public abstract class StructuredFileSort<R extends StructuredFileSortRecord> extends StructuredFile {

   public static Log log = new Log(StructuredFileSort.class);
   protected int segments = 0;
   protected Datafile destfile, tempfile;
   protected int spillThreshold = 100000;
   protected TreeSet<R> records;
   protected ArrayList<StructuredFileSortReader> readers = new ArrayList<StructuredFileSortReader>();

   public StructuredFileSort(Datafile df) {
      super(df);
      spillThreshold = spillThreshold();
      destfile = df;
   }

   @Override
   public void hookRecordWritten() {
      if (writer == null) {
         //log.info("hookRecordWritten %s %d", writer, records.size());
         records.add(createRecord());
         checkSpill();
      }
   }

   private void checkSpill() {
      if (records.size() >= spillThreshold) {
         spillSegment();
      }
   }
   
   protected abstract int spillThreshold();

   public abstract R createRecord();

   @Override
   public void openWrite() {
      records = new TreeSet<R>();
      tempfile = this.getTempfile();
      tempfile.openWrite();
      tempfile.setBufferSize(10000000);
      super.setDatafile(null);
      super.openWrite();
   }

   public void openWriteFinal() {
      this.setDatafile(destfile);
      this.setBufferSize(10000000);
      super.openWrite();
   }

   public void openReadTemp() {
      super.openRead();
   }

   @Override
   public void closeWrite() {
      spillSegment();
      setDatafile(tempfile);
      super.closeWrite();
      merge();
   }

   public void spillSegment() {
      if (records.size() > 0) {
         setDatafile(tempfile);
         long segmentoffset = getOffset();
         for (R r : records) {
            r.setFile(this);
            r.write();
         }
         StructuredFileSort segment = (StructuredFileSort) this.clone();
         segment.setOffset(segmentoffset);
         segment.setCeiling(getOffset());
         log.info("segment %d offset %d ceiling %d", segments, segment.getOffset(), segment.getCeiling());
         readers.add(new StructuredFileSortReader(segment, segments++));
         setDatafile(null);
         records.clear();
      }
   }

   public final void merge() {
      this.openWriteFinal();
      TreeSet<StructuredFileSortReader> orderedreaders = new TreeSet<StructuredFileSortReader>();
      int show = 0;
      for (StructuredFileSortReader r : readers) {
         r.index.setBufferSize(50000000 / readers.size());
         //log.info("new datafile status %s %s %d %d", r.index.datafile.status, r.index.datafile.getFullPath(), r.index.datafile.getOffset(), r.index.datafile.getCeiling());
         r.openRead();
         //log.info("new datafile status %s %s %d %d", r.index.datafile.status, r.index.datafile.getFullPath(), r.index.datafile.getOffset(), r.index.datafile.getCeiling());
         if (r.next()) {
            orderedreaders.add(r);
         }
      }
      StructuredFileSortReader r;
      while (orderedreaders.size() > 0) {
         r = orderedreaders.pollFirst();
         StructuredFileSortRecord record = r.index.createRecord();
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

   public abstract int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2);

   protected int compareKeys(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
      int comp = compare(o1, o2);
      return (comp != 0) ? comp : 1;
   }

   public abstract int compare(StructuredFileSort o1, StructuredFileSort o2);

   protected int compareKeys(StructuredFileSort o1, StructuredFileSort o2) {
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
