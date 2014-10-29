package io.github.repir.tools.hadoop.TSV;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Structure.StructuredRecordFile;

/**
 * @author jeroen
 */
public abstract class InputFormat<F extends StructuredRecordFile, V extends Writable> extends io.github.repir.tools.hadoop.InputFormat<F, V> {

    public static Log log = new Log(InputFormat.class);

    public InputFormat(Class fileclass) {
        super(fileclass);
    }
}
