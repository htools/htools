package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.FileIntegrityException;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This internal helping class stores compressed offsets to tiny records
 * identified by a sequential ID (starting from 0). The average size of 
 * a record * loadfactor should not exceed 256 bytes (though it is ok if that
 * occasionally happens). There should be no gas in the ID's starting with
 * 0, and the Records must be written in order of their ID.
 * <p>
 * The records are grouped by loadfactor (default=10) Records.
 * The groups are divided into sections of (subpointers + 1) groups.
 * With default subpointers=9, this means group 0 contains ID 0-9, group 2
 * ID code 10-19, and section 0 ID 0-99. For every section, the lookup
 * data consists of a full offset that points to the first Record of the first group
 * followed by a byte (0-255) that is used to point to the first Record with the next group. With
 * default settings, this means that for every 100 records, 13 bytes of lookup
 * code is used, which we call a 'jump table'. 
 * In the rare event too many Records share the same hashcode and their combined size
 * exceeds 256 bytes, the subpointer will point to the start of the furthest record
 * it can reach. If a Record is retrieved it will start with a Record that has a
 * hashcode smaller than what it is looking for, and should continue to read until
 * a Record is found, or a hashcode is encountered that is greater than the 
 * search key.
 * <p>
 * @author jeroen
 */
class StructuredFileByteJumptableInternal extends StructuredFile {

   public static Log log = new Log(StructuredFileByteJumptableInternal.class);
   protected byte currenttable[];
   protected BufferReaderWriter residenttable;
   protected int loadfactor = 10;
   protected int subpointers = 9;
   public int lastoffset;
   protected IntField offset = this.addInt("offset");
   protected FixedMemField length = this.addFixedMem("suboffset", subpointers);

   public StructuredFileByteJumptableInternal(Datafile df) {
      super(df);
      setLoadFactor(10);
   }

   /**
    * The offset file is intended to be read into memory, to quickly resolve the
    * offsets of records.
    */
   @Override
   public void openRead() throws FileIntegrityException {
      if (residenttable == null) {
          try {
              super.openRead();
              residenttable = new BufferReaderWriter(getDatafile().readFully());
              super.closeRead();
          } catch (EOCException  ex) {
              log.fatalexception(ex, "openRead() %s", getDatafile().getCanonicalPath());
          }
      }
   }

   public void closeRead() {
      residenttable = null;
      super.closeRead();
   }

   @Override
   public void openWrite() {
      super.openWrite();
      //log.info("openWrite() %s", this.getDatafile().getFullPath());
      lastoffset = 0;
      currenttable = new byte[subpointers];
   }

   @Override
   public void closeWrite() {
      if (this.nextField == length) {
         this.length.write(currenttable);
      }
      super.closeWrite();
   }

   /**
    * Writes the next id with the offset at which the record starts. The caller
    * should sequentially write all id's starting with 0, also the id's that do
    * not contain a record.
    * <p>
    * @param id
    * @param recordstart
    */
   public void write(int id, StructuredFile rec) {
      //log.info("id %d offset %d ", id, rec.getOffsetTupleStart());
      if ((id % ((subpointers + 1) * loadfactor)) == 0) {
         writeMarker(id, rec);
      } else if (id % loadfactor == 0) {
         writeJump(id, rec);
      }
   }

   protected void writeMarker(int id, StructuredFile rec) {
      //log.info("Marker %d %d %d hashfile %d offset %d", currenthash, lastnojumphash, subpointers, this.getOffset(), offset);
      lastoffset = (int) rec.getOffsetTupleStart();
      this.offset.write(lastoffset);
   }

   protected void writeJump(int id, StructuredFile rec) {
      int jumpindex = getJumpIndex(id);
      //log.info("%s %s", currenttable, rec);
      currenttable[ jumpindex - 1] = (byte) (rec.getOffsetTupleStart() - lastoffset);
      lastoffset = (int) rec.getOffsetTupleStart();
      if (jumpindex == subpointers) {
         this.length.write(currenttable);
      }
   }

   protected int getJumpIndex(int id) {
      return (id % ((subpointers + 1) * loadfactor)) / loadfactor;
   }

   protected int getMarkerOffset(int id) {
      int markers = id / ((subpointers + 1) * loadfactor);
      return markers * (4 + subpointers);
   }

   /**
    * If loadfactor > 1, a group of multiple records will share the same offset
    * pointer. {@link #getOffset(int)} will give the file offset of the group
    * and {@link #getSkip(int)} will indicate how many record to skip before the
    * required record is read.
    * <p>
    * @param id
    * @return
    */
   public int getSkip(int id) {
      int inmarker = id % ((subpointers + 1) * loadfactor);
      return inmarker % loadfactor;
   }

   /**
    * @param id
    * @return the offset in the datafile of the record(group) that contains the
    * record with the given ID.
    */
   public long getOffset(int id) {
      long offset = -1;
      try {
         int marker = getMarkerOffset(id);
         int jumpindex = getJumpIndex(id);
         residenttable.setOffset(marker);
         offset = residenttable.readInt();
         for (int i = 0; i < jumpindex; i++) {
            offset += residenttable.readByte();
         }
         //log.info("getOffset() bucketindex %d marker %d jumpindex %d offset %d", bucketindex, marker, jumpindex, offset);
      } catch (EOCException ex) {
         log.fatalexception(ex, "getOffset( %d ) residenttable %s", id, residenttable);
      }
      return offset;
   }
}
