package io.github.repir.tools.hadoop.TSV;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Structure.StructuredRecordFile;

/**
 * @author jeroen
 */
public abstract class OutputFormat<F extends StructuredRecordFile, V extends Writable> extends io.github.repir.tools.hadoop.OutputFormat<F, V> {

    public static Log log = new Log(OutputFormat.class);

    public OutputFormat(Class fileclass) {
        super(fileclass);
    }
}
