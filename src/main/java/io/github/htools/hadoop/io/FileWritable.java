package io.github.htools.hadoop.io;

import io.github.htools.io.struct.StructuredFileRecord;
import io.github.htools.io.struct.StructuredRecordFile;

/**
 * Generic FileWritable interface for Structured TSV files. Subclasses must implement 
 readFields and write for Hadoop serialization and read/write to a File. Although
 the read/write code for Hadoop/File are usually redundant, thsi is faster than
 generic serialization through Avro/Json/TSV.FileWritable.
 * @author jeroen
 * @param <F> 
 */
public interface FileWritable<F extends StructuredRecordFile> extends StructuredFileRecord<F>, org.apache.hadoop.io.Writable {
}
