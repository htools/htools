package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import java.lang.reflect.Constructor;
import org.apache.hadoop.io.LongWritable;

/**
 * @author jeroen
 */
public abstract class InputFormat<F extends StructuredRecordFile, V extends org.apache.hadoop.io.Writable>
        extends FileInputFormat<LongWritable, V> {

    public InputFormat(Class fileclass) {
        super(fileclass);
    }

    protected F getFile(Datafile datafile) throws ClassNotFoundException {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
    }

}
