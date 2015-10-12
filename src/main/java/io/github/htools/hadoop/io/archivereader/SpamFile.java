package io.github.htools.hadoop.io.archivereader;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFile;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * A file containing the document ID's and Waterloo Fusion Spam index, to allow
 * selective indexing of documents above a certain threshold.
 */
public class SpamFile extends StructuredFile {

   public static Log log = new Log(SpamFile.class);

   /**
    * This constructor does not open the file for read/write.
    * <p>
    * @param datafile
    */
   public SpamFile(Datafile datafile) throws IOException {
      super(datafile);
   }
   public StringField cluewebid = this.addString("cluewebid");
   public IntField spamindex = this.addInt("spamindex");

   public static idlist getIdList(Datafile df, int spamthreshold) throws IOException {
      idlist sl = new idlist();
      SpamFile sf = new SpamFile(df);
      sf.setBufferSize(10000000);
      sf.openRead();
      while (sf.nextRecord()) {
         if (sf.spamindex.value >= spamthreshold) {
            sl.set(sf.cluewebid.value);
         }
      }
      sf.closeRead();
      return sl;
   }
}
