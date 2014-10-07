package io.github.repir.tools.hadoop;

import io.github.repir.tools.Structure.StructuredFileRecord;
import io.github.repir.tools.Structure.StructuredRecordFile;
import org.apache.hadoop.io.Writable;

public interface StructuredRecordWritable<F extends StructuredRecordFile> extends StructuredFileRecord<F>, Writable {
}
