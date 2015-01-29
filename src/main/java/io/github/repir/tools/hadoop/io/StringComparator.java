package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparator;
/**
 *
 * @author jeroen
 */
public class StringComparator extends WritableComparator {
   public static final Log log = new Log( StringComparator.class );
    BufferReaderWriter reader1 = new BufferReaderWriter();
    BufferReaderWriter reader2 = new BufferReaderWriter();
     
    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        reader1.setBuffer(b1, s1 + 4, s1 + l1 - 4);
        reader2.setBuffer(b2, s2 + 4, s2 + l2 - 4);
        int end1 = reader1.bufferpos + reader1.readInt();
        int end2 = reader2.bufferpos + reader2.readInt();
        return IntWritable.Comparator.compareBytes(b1, s1 + 8, reader1.readInt(), b2, s2 + 8, reader2.readInt());
    }
}
