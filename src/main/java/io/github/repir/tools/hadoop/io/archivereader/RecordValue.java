package io.github.repir.tools.hadoop.io.archivereader;

import io.github.repir.tools.io.buffer.BufferDelayedWriter;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.io.EOCException;
import io.github.repir.tools.lib.Log;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.BytesWritable;

/**
 * During extraction of a Repository (phase 2), the Mapper extracts the source
 * entities to TermEntityKey,TermEntityValue pairs. The mapper segments the
 * collection into partitions that are reduced separately. In a Reducer, all
 * {@link ReduciblePartitionedFeature}s are created by calling their {@link ReducibleFeature#reduceInput( 
 * TermEntityKey, java.lang.Iterable)}
 * <
 * p/>
 * During the PRELOAD phases, {@link EntityStoredFeatures} are consructed, which
 * can only have one value per Entity per Feature! Although this value can be of
 * a complex type, e.g. DocLiteral (also used for collectionid), DocTF,
 * DocForward.
 * <p/>
 * In the CHANNEL phase, features that extend {@link AutoTermDocumentFeature}
 * are constructed, for which the data is sorted first by Term and then by
 * Document.
 *
 * @author jer
 */
public class RecordValue extends BytesWritable {

   public static Log log = new Log(RecordValue.class);
   public BufferReaderWriter reader = new BufferReaderWriter();
   public BufferDelayedWriter writer = new BufferDelayedWriter();

   public RecordValue() {
   }

   @Override
   public void readFields(DataInput in) throws IOException {
      try {
         int length = in.readInt();
         byte b[] = new byte[length];
         in.readFully(b);
         reader.setBuffer(b);
      } catch (EOCException ex) {
         throw new IOException(ex);
      }
   }

   @Override
   public void write(DataOutput out) throws IOException {
      byte b[] = writer.getAsByteBlock();
      out.write(b);
   }
}
