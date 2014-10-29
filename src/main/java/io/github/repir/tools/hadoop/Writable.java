package io.github.repir.tools.hadoop;

import io.github.repir.tools.Structure.StructuredFileRecord;
import io.github.repir.tools.Structure.StructuredRecordFile;

/**
 * Generic Writable interface for Structured TSV files. Subclasses must implement 
 * readFields and write for Hadoop serialization and read/write to a File. Although
 * the read/write code for Hadoop/File are usually redundant, thsi is faster than
 * generic serialization through Avro/Json/TSV.Writable.
 * @author jeroen
 * @param <F> 
 */
public interface Writable<F extends StructuredRecordFile> extends StructuredFileRecord<F>, org.apache.hadoop.io.Writable {
}
