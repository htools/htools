package io.github.repir.tools.io.struct;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

public abstract class StructuredFileOffsetLength extends StructuredFile {

   public Log log = new Log(StructuredFileOffsetLength.class);
   public LongField offset = this.addLong("offset");
   public IntField length = this.addInt("length");

   public StructuredFileOffsetLength(Datafile basefile) {
      super(basefile);
   }

   protected abstract StructuredFile getSource();

   public void find(int pos) throws EOCException {
      int newceiling = 12 * (pos+1);
      if (getCeiling() != newceiling) {
         StructuredFile source = getSource();
         if (pos >= 0) {
            this.setOffset(12 * pos);
            this.setCeiling(newceiling);
            //log.info("find() pos %d offset %d ceiling %d", pos, this.getOffset(), this.getCeiling());
            long o = offset.read();
            int l = length.read();
            //log.info("find() offset %d length %d", o, l);
            source.setOffset(o);
            source.setCeiling(o + l);
         } else {
            throw new EOCException("find(%d)", pos);
         }
      }
   }

   public long findOffset(int pos) throws EOCException {
      int newceiling = 12 * (pos+1);
      if (getCeiling() != newceiling) {
         StructuredFile source = getSource();
         if (pos >= 0) {
            this.setOffset(12 * pos);
            this.setCeiling(newceiling);
            //log.info("find() pos %d offset %d ceiling %d", pos, this.getOffset(), this.getCeiling());
            long o = offset.read();
            int l = length.read();
            //log.info("find() offset %d length %d", o, l);
            return o;
         } else {
            throw new EOCException("find(%d)", pos);
         }
      }
      return -1;
   }

   public void writeRecordOffset(StructuredFile record) {
      //if (getOffset() / 12 < 500000)
      //   log.info("write id %d offset %d length %d", getOffset() / 12, record.getOffsetTupleStart(), (record.getOffetTupleEnd() - record.getOffsetTupleStart()));
      offset.write(record.getOffsetTupleStart());
      length.write((int) (record.getOffetTupleEnd() - record.getOffsetTupleStart()));
   }

   public void mergeIndexSegments() throws IOException {
      long offsets[] = getSource().mergeSegments();
      HDFSPath dir = (HDFSPath) getDatafile().getDir();
      TreeSet<Datafile> sortedfiles = new TreeSet(dir.getFilesStartingWith(getDatafile().getName()));
      StructuredFileOffsetLength in = (StructuredFileOffsetLength) this.clone();
      int offsetpos = 0;
      this.openWrite();
      for (Datafile df : sortedfiles) {
         in.setDatafile(df);
         df.setBufferSize((int) Math.min(10000000, df.getLength()));
         in.openRead();
         while (in.nextRecord()) {
            offset.write(in.offset.value + offsets[ offsetpos]);
            length.write(in.length.value);
            //log.info("%d %d", offset.value, length.value);
         }
         //log.info("%s %d", in.datafile.getFullPath(), offsets[offsetpos]);
         in.closeRead();
         offsetpos++;
      }
      this.closeWrite();
   }
}
