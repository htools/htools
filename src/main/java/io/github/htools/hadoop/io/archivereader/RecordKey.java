package io.github.htools.hadoop.io.archivereader;

import io.github.htools.hadoop.io.archivereader.RecordKey.Type;
import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 *
 * @author jer
 */
public class RecordKey implements WritableComparable<RecordKey> {

   public static Log log = new Log(RecordKey.class);
   public Type type;
   public int partition;
   public String collectionid;
   public int docid; // not passed over MR
   public int termid;
   public int feature;

   public static enum Type {

      ENTITYFEATURE,
      LOOKUPFEATURE,
      TERMDOCFEATURE
   }

   public RecordKey() {
   }

   public static RecordKey createTermDocKey(int partition, int feature, int term, String docname) {
      RecordKey t = new RecordKey();
      t.type = Type.TERMDOCFEATURE;
      t.partition = partition;
      t.termid = term;
      t.feature = feature;
      t.collectionid = docname;
      return t;
   }

   public Type getType() {
      return type;
   }

   public int getPartition() {
      return partition;
   }

   public int getChannelID() {
      return feature;
   }

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write((byte) type.ordinal());
      writer.write((short) this.partition);
      writer.writeUB(feature);
      writer.write(termid);
      writer.write0(collectionid);
      out.write(writer.getAsByteBlock());
   }

   public byte[] writeBytes() {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.writeUB((byte) type.ordinal()); // byte 4: type
      writer.write((short) this.partition); // byte 5..6: partition
      writer.writeUB(feature); // byte 7: feature
      writer.write(termid); // byte 8..11 termID
      writer.write0(collectionid); //byte 12.. : collectionID of document
      return writer.getAsByteBlock();
   }

   // type:byte partition:short bucketindex:long termid:String feature:byte
   @Override
   public void readFields(DataInput in) throws IOException {
      try {
         int length = in.readInt();
         byte b[] = new byte[length];
         in.readFully(b);
         BufferReaderWriter reader = new BufferReaderWriter(b);
         type = Type.values()[ reader.readByte()];
         partition = reader.readShort();
         feature = reader.readByte();
         termid = reader.readInt();
         collectionid = reader.readString0();
      } catch (EOCException ex) {
         throw new IOException(ex);
      }
   }

   // for testing only
   public void readFields(byte b[], int offset) throws EOCException {
      BufferReaderWriter reader = new BufferReaderWriter(b);
      reader.bufferpos = offset;
      type = Type.values()[ reader.readByte()];
      partition = reader.readShort();
      feature = reader.readByte();
      termid = reader.readInt();
      collectionid = reader.readString0();
      //log.info("readFields() end ");
   }

   @Override
   public int compareTo(RecordKey o) { // never used
      log.crash();
      return 0;
   }

   public static class partitioner extends Partitioner<RecordKey, Writable> {

      @Override
      public int getPartition(RecordKey key, Writable value, int i) {
         return key.partition;
      }
   }

   public static class FirstGroupingComparator
           extends WritableComparator {

      protected FirstGroupingComparator() {
         super(RecordKey.class);
      }

      @Override
      public int compare(byte[] b1, int ss1, int l1, byte[] b2, int ss2, int l2) {
         int comp = 0;
         byte b = b1[ss1+4];
         if (b == Type.ENTITYFEATURE.ordinal() || b == Type.LOOKUPFEATURE.ordinal()) { // make sure docs are reduced before tokens
            if (b2[ss2 + 4] == b) {
               return compareBytes(b1, ss1 + 7, l1 - 7, b2, ss2 + 7, l2 - 7);
            } else {
               return -1;
            }
         } else {
            if (b2[ss2 + 4] == b) {
               return compareBytes(b1, ss1 + 7, 5, b2, ss2 + 7, 5);
            } else {
               return 1;
            }
         }
      }
   }

   public static class SecondarySort
           extends WritableComparator {

      RecordKey t1 = new RecordKey();
      RecordKey t2 = new RecordKey();

      protected SecondarySort() {
         super(RecordKey.class);
      }

      @Override
      public int compare(byte[] b1, int ss1, int l1, byte[] b2, int ss2, int l2) {
         return compareBytes(b1, ss1 + 4, l1 - 4, b2, ss2 + 4, l2 - 4);

      }
   }
}
