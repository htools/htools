package io.github.repir.tools.Content;

import java.io.EOFException;

/**
 * The same as {@link RecordJump}, but this class uses two bytes for the length
 * of records, allowing a record group to contain max 65536 bytes.
 * <p/>
 * @author jeroen
 */
public class RecordJump2 extends RecordJump {

   int lengthsize = 2;
   BufferReaderWriter rw;

   public RecordJump2(Datafile df) {
      super(df);
   }

   @Override
   public void openWrite() {
      super.openWrite();
      currenttable = new byte[lengths * lengthsize];
      rw = new BufferReaderWriter(currenttable);
   }

   @Override
   protected void writeJump(int id, RecordBinary rec) {
      int jumpindex = getJumpIndex(id);
      rw.bufferpos = (jumpindex - 1) * lengthsize;
      int oldoffset = lastoffset;
      lastoffset = (int) rec.getOffsetTupleStart();
      rw.write2((int) (lastoffset - oldoffset));
      if (jumpindex == lengths) {
         this.length.write(currenttable);
      }
   }

   @Override
   protected int getMarkerOffset(int id) {
      int markers = id / ((lengths + 1) * loadfactor);
      return markers * (4 + lengths * lengthsize);
   }

   public long getOffset(int id) {
      long offset = -1;
      try {
         int marker = getMarkerOffset(id);
         int jumpindex = getJumpIndex(id);
         residenttable.setOffset(marker);
         offset = residenttable.readInt();
         for (int i = 0; i < jumpindex; i++) {
            offset += residenttable.readInt2();
         }
         //log.info("getOffset() marker %d jumpindex %d offset %d skip %d", marker, jumpindex, offset, getSkip(id));
      } catch (EOFException ex) {
         log.fatalexception(ex, "getOffset( %d ) residenttable %s", id, residenttable);
      }
      return offset;
   }
}
