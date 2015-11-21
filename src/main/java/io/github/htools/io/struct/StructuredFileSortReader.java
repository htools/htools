package io.github.htools.io.struct;

import io.github.htools.io.FileIntegrityException;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * Internal helper class for the construction of StructureFileSort
 * @author jer
 */
public class StructuredFileSortReader implements Comparable<Object> {

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
      return index.hasNext() && index.nextRecord();
   }

   @Override
   public int compareTo(Object o) {
      return index.compareKeys(index, ((StructuredFileSortReader) o).index);
   }
}
