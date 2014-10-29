package io.github.repir.tools.hadoop.IO;

import io.github.repir.tools.Buffer.BufferDelayedWriter;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public abstract class MRInputSplit<PWRITABLE extends Writable, P> extends InputSplit implements Writable, Comparable<MRInputSplit> {

   public static Log log = new Log(MRInputSplit.class);
   public ArrayList<P> list = new ArrayList<P>();
   String hosts[] = new String[0]; // preferred node to execute the mapper
   public int partition; 

   public MRInputSplit() {
   }

   public MRInputSplit(int partition) {
      // if index creation was done properly, a single reducer was used to write all
      // files for a single partition. These files have probably been replicated,
      // so the intersection of hosts indicates the best node to map the split.
      this.partition = partition;
   }

   /**
    * @param q The Query request to add to this Split
    */
   public void add(P term) {
      this.list.add(term);
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
   
   public PWRITABLE get(int i) {
      return convert(list.get(i));
   }
   
   public abstract PWRITABLE convert(P p);

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write(hosts);
      writer.write(partition);
      writer.write(list.size());
      for (P key : list) {
         writeKey(writer, key);
      }
      out.write(writer.getAsByteBlock());
   }
   
   public abstract void writeKey(BufferDelayedWriter out, P key);

   @Override
   public void readFields(DataInput in) throws IOException {
      int length = in.readInt();
      byte b[] = new byte[length];
      in.readFully(b);
      BufferReaderWriter reader = new BufferReaderWriter(b);
      hosts = reader.readStringArray();
      partition = reader.readInt();
      int listsize = reader.readInt();
      list = new ArrayList<P>();
      for (int q = 0; q < listsize; q++) {
         list.add(readKey(reader));
      }
   }
   
   public abstract P readKey(BufferReaderWriter reader);

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
}
