package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;

/**
 * This internal helping class stores compressed offsets to tiny records
 * identified by a sequential ID (starting form 0). The records should be
 * written in sequence. The default setting (loadfactor=10) and lengths=9 are
 * used to store the offsets in 'jumpsets'. Of each group of #loadfactor records
 * only the first offset is stored, and the others are skipped. In each jumpset,
 * the full offset of the first group is stored, followed by the length of the
 * #lengths groups. For example, if 200 records are stored, this requires 2
 * jumpsets. jumpset#0.offset = record#0.offset jumpset#0.length#0 = {
 * sum(record#0.length + ... + record#9.length), sum(record#10.length + ... +
 * record#19.length), ... }.
 * <p/>
 * When retrieving, {@link #getOffset(int)} is used to obtain the offset in the
 * datafile at which record with the given id starts. For example, the record
 * with id=123 can be found by incrementing the offset of jumpset#1, with the
 * lengths of subgroups #0 and #1. From that position, the record with id=123
 * will be the 4th record. You can use {@link #getSkip(int)}
 * <p/>
 * @author jeroen
 */
public class RecordJump extends RecordBinary {

   public static Log log = new Log(RecordJump.class);
   protected byte currenttable[];
   protected BufferReaderWriter residenttable;
   protected int loadfactor = 10;
   protected int lengths = 9;
   public int lastoffset;
   protected IntField offset = this.addInt("offset");
   protected FixedMemField length = this.addFixedMem("suboffset", lengths);

   public RecordJump(Datafile df) {
      super(df);
      setLoadFactor(10);
   }

   /**
    * The offset file is intended to be read into memory, to quickly resolve the
    * offsets of records.
    */
   @Override
   public void openRead() {
      if (residenttable == null) {
         super.openRead();
         residenttable = new BufferReaderWriter(datafile.readFully());
         super.closeRead();
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
      currenttable = new byte[lengths];
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
    * <p/>
    * @param id
    * @param recordstart
    */
   public void write(int id, RecordBinary rec) {
      //log.info("id %d offset %d ", id, rec.getOffsetTupleStart());
      if ((id % ((lengths + 1) * loadfactor)) == 0) {
         writeMarker(id, rec);
      } else if (id % loadfactor == 0) {
         writeJump(id, rec);
      }
   }

   protected void writeMarker(int id, RecordBinary rec) {
      //log.info("Marker %d %d %d hashfile %d offset %d", currenthash, lastnojumphash, lengths, this.getOffset(), offset);
      lastoffset = (int) rec.getOffsetTupleStart();
      this.offset.write(lastoffset);
   }

   protected void writeJump(int id, RecordBinary rec) {
      int jumpindex = getJumpIndex(id);
      //log.info("%s %s", currenttable, rec);
      currenttable[ jumpindex - 1] = (byte) (rec.getOffsetTupleStart() - lastoffset);
      lastoffset = (int) rec.getOffsetTupleStart();
      if (jumpindex == lengths) {
         this.length.write(currenttable);
      }
   }

   protected int getJumpIndex(int id) {
      return (id % ((lengths + 1) * loadfactor)) / loadfactor;
   }

   protected int getMarkerOffset(int id) {
      int markers = id / ((lengths + 1) * loadfactor);
      return markers * (4 + lengths);
   }

   /**
    * If loadfactor > 1, a group of multiple records will share the same offset
    * pointer. {@link #getOffset(int)} will give the file offset of the group
    * and {@link #getSkip(int)} will indicate how many record to skip before the
    * required record is read.
    * <p/>
    * @param id
    * @return
    */
   public int getSkip(int id) {
      int inmarker = id % ((lengths + 1) * loadfactor);
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
      } catch (EOFException ex) {
         log.fatalexception(ex, "getOffset( %d ) residenttable %s", id, residenttable);
      }
      return offset;
   }
}
