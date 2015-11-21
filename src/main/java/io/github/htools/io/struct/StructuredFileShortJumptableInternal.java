package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import java.io.IOException;

/**
 * The same as {@link StructuredFileByteJumptableInternal}, but this class uses two bytes for the length
 * of records, allowing for record groups of 65536 bytes.
 * <p>
 * @author jeroen
 */
 class StructuredFileShortJumptableInternal extends StructuredFileByteJumptableInternal {

   int lengthsize = 2;
   BufferReaderWriter rw;

   public StructuredFileShortJumptableInternal(Datafile df) {
      super(df);
   }

   @Override
   public void openWrite() {
      super.openWrite();
      currenttable = new byte[subpointers * lengthsize];
      rw = new BufferReaderWriter(currenttable);
   }

   @Override
   protected void writeJump(int id, StructuredFile rec) {
      int jumpindex = getJumpIndex(id);
      rw.bufferpos = (jumpindex - 1) * lengthsize;
      int oldoffset = lastoffset;
      lastoffset = (int) rec.getOffsetTupleStart();
      rw.write2((int) (lastoffset - oldoffset));
      if (jumpindex == subpointers) {
         this.length.write(currenttable);
      }
   }

   @Override
   protected int getMarkerOffset(int id) {
      int markers = id / ((subpointers + 1) * loadfactor);
      return markers * (4 + subpointers * lengthsize);
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
      } catch (EOCException ex) {
         log.fatalexception(ex, "getOffset( %d ) residenttable %s", id, residenttable);
      }
      return offset;
   }
}
