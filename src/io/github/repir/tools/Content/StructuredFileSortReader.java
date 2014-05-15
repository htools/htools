package io.github.repir.tools.Content;

import io.github.repir.tools.Lib.Log;

/**
 * Internal helper class for the construction of StructureFileSort
 * @author jer
 */
class StructuredFileSortReader implements Comparable<Object> {

   public Log log = new Log(StructuredFileSortReader.class);
   StructuredFileSort index;
   public int segment;

   public StructuredFileSortReader(StructuredFileSort index, int segment) {
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
      return index.compareKeys(index, ((StructuredFileSortReader) o).index);
   }
}
