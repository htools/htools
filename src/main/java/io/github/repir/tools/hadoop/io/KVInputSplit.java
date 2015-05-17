package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.collection.ArrayMap;
import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

/**
 * A custom implementation of Hadoop's InputSplit used by RetrieverMR. 
 * Each Split holds a set of Queries tasks that must all belong to the same partition. 
 * <p/>
 * @author jeroen
 */
public abstract class KVInputSplit<KEY, VALUE> extends InputSplit implements Writable, Comparable<KVInputSplit> {

   public static Log log = new Log(KVInputSplit.class);
   public ArrayMap<KEY,VALUE> list = new ArrayMap();
   String hosts[] = new String[0]; // preferred node to execute the mapper

   public KVInputSplit() {
   }

   /**
    * @param q The Query request to add to this Split
    */
   public void add(KEY key, VALUE term) {
      list.add(key, term);
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
   
   public Map.Entry<KEY, VALUE> get(int i) {
      return list.get(i);
   }

   @Override
   public void write(DataOutput out) throws IOException {
      BufferDelayedWriter writer = new BufferDelayedWriter();
      writer.write(hosts);
      writer.write(list.size());
      for (Map.Entry<KEY, VALUE> entry : list) {
         writeKey(writer, entry.getKey());
         writeValue(writer, entry.getValue());
      }
      writer.writeBuffer(out);
   }
   
   public abstract void writeValue(BufferDelayedWriter out, VALUE value);

   public abstract void writeKey(BufferDelayedWriter out, KEY key);

   @Override
   public void readFields(DataInput in) throws IOException {
      BufferReaderWriter reader = new BufferReaderWriter(in);
      hosts = reader.readStringArray();
      int listsize = reader.readInt();
      list = new ArrayMap();
      log.info("readFields %s %d", hosts, listsize);
      for (int q = 0; q < listsize; q++) {
         list.add(readKey(reader), readValue(reader));
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
   public int compareTo(KVInputSplit o) {
      int comp = o.list.size() - list.size();
      return comp != 0 ? comp : 1; // cannot be 0!
   }
}
