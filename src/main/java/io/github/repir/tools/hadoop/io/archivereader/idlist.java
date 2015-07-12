package io.github.repir.tools.hadoop.io.archivereader;
import io.github.repir.tools.lib.Log; 

/**
 * Internally used memory array by {@link EntityReaderCW} and {@link EntityReaderCW12}, 
 * that indicates which documents are part of the collection. This is mostly used
 * to construct idlists that contain less spam or a subset of the entire collection.
 * @author Jeroen Vuurens
 */
public class idlist {
   static Log log = new Log(idlist.class);
   long[] spam = new long[10000063 / 64]; 

  public void set(String cwid) {
      int id = Integer.parseInt(cwid.substring(20)) + 100000 * Integer.parseInt(cwid.substring(17, 19));
      int pos = id / 64;
      int bit = id % 64;
      spam[pos] |= (1l << bit);
  }

   public boolean get(String cwid) {
      if (cwid.length() != 25)
         return false;
      int id = Integer.parseInt(cwid.substring(20)) + 100000 * Integer.parseInt(cwid.substring(17, 19));
      int pos = id / 64;
      int bit = id % 64;
      return (spam[pos] & (1l << bit)) != 0;
   }
}
