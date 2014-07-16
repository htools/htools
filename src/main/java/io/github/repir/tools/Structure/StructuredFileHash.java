package io.github.repir.tools.Structure;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;

/**
 * Internal helper class, that stores offsets to a Records in a StructuredFile 
 * in a hash table. The offsets should be written in order of the hashcodes,
 * writing -1 for hashcodes that have no records, and writing exactly as many 
 * offsets as there is space in the hashtable.
 * @author jer
 */
public class StructuredFileHash extends StructuredFile {
   public static Log log = new Log( StructuredFileHash.class );
   public int currentbucketindex = -1;
   public LongField offset = this.addLong("offset");

   public StructuredFileHash(Datafile df, int tablesize) {
      super(df);
      this.setCapacity(tablesize);
   }

   @Override
   public void openWrite() {
      super.openWrite();
      currentbucketindex = -1;
   }

   @Override
   public void closeWrite() {
      for (; currentbucketindex < bucketcapacity - 1; currentbucketindex++) {
         this.offset.write(0l);
      }
      super.closeWrite();
   }

   public void writeHash(int bucketindex, long offset) {
      for (; currentbucketindex < bucketindex - 1; currentbucketindex++) {
         this.offset.write(-1l);
      }
      if (currentbucketindex < bucketindex) {
         this.offset.write(offset);
         currentbucketindex++;
      } else if (bucketindex < currentbucketindex) {
         log.info("warning trying to write overdue file %s current %d bucket %d offset %d", datafile.getFilename(), currentbucketindex, bucketindex, offset);
      }
   }
}
