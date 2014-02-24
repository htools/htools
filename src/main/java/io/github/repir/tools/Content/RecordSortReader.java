package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;
import java.io.EOFException;

public class RecordSortReader implements Comparable<Object> {

   public Log log = new Log(RecordSortReader.class);
   RecordSort index;
   public int segment;

   public RecordSortReader(RecordSort index, int segment) {
      this.index = index;
      this.segment = segment;
   }

   public void openRead() {
      index.openReadTemp();
   }

   public boolean next() {
      return index.hasNext() && index.next();
   }

   @Override
   public int compareTo(Object o) {
      return index.compareKeys(index, ((RecordSortReader) o).index);
   }
}
