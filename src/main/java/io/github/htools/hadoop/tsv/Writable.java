package io.github.htools.hadoop.tsv;

/**
 * Generic FileWritable interface for Structured TSV files. Subclasses must implement 
 readFields and write for Hadoop serialization and read/write to a File. Although
 the read/write code for Hadoop/File are usually redundant, thsi is faster than
 generic serialization through Avro/Json/TSV.FileWritable.
 * @author jeroen
 * @param <F> 
 */
public abstract class Writable<F extends File> extends io.github.htools.hadoop.io.buffered.Writable implements io.github.htools.hadoop.io.FileWritable<F> {

}
