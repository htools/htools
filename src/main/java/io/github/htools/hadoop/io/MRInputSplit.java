package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static io.github.htools.lib.PrintTools.sprintf;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p>
 * @author jeroen
 */
public abstract class MRInputSplit<KEY, VALUE> extends InputSplit implements Writable, Comparable<MRInputSplit> {

   public static Log log = new Log(MRInputSplit.class);
   public ArrayList<VALUE> list = new ArrayList<VALUE>();
   String hosts[] = new String[0]; // preferred node to execute the mapper
   public KEY partition; 

   public MRInputSplit() {
   }

   public MRInputSplit(KEY partition) {
      // if index creation was done properly, a single reducer was used to write all
      // files for a single partition. These files have probably been replicated,
      // so the intersection of hosts indicates the best node to map the split.
      this.partition = partition;
   }

   /**
    * @param value The value to add to this Split
    */
   public void add(VALUE value) {
      this.list.add(value);
   }

   public void addAll(Collection<VALUE> values) {
      this.list.addAll(values);
   }

   /**
    * @return the number of Query requests in this split
    */
   @Override
   public long getLength() throws IOException, InterruptedException {
      return list.size();
   }
   
   public int size() {
      return list.size();
   }
   
   public VALUE get(int i) {
      return list.get(i);
   }

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write(hosts);
      writeKey(writer, partition);
      writer.write(list.size());
      for (VALUE key : list) {
         writeValue(writer, key);
      }
      writer.writeBuffer(out);
   }
   
   public abstract void writeValue(BufferDelayedWriter out, VALUE value);

   public abstract void writeKey(BufferDelayedWriter out, KEY key);

   @Override
   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter reader = new BufferReaderWriter(in);
      hosts = reader.readStringArray();
      partition = readKey(reader);
      int listsize = reader.readInt();
      list = new ArrayList<VALUE>();
      for (int q = 0; q < listsize; q++) {
         list.add(readValue(reader));
      }
   }
   
   public abstract VALUE readValue(BufferReaderWriter reader);

   public abstract KEY readKey(BufferReaderWriter reader);

   @Override
   public String[] getLocations() throws IOException, InterruptedException {
      return hosts;
   }

   // enables sorting of splits, so that bigger splits can be processed first
   @Override
   public int compareTo(MRInputSplit o) {
      int comp = o.list.size() - list.size();
      return comp != 0 ? comp : 1; // cannot be 0!
   }
   
   public String toString() {
       return sprintf("InputSplit( %s, %s )", partition, list);
   }
}
