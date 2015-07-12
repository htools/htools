package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.DirComponent;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;

/**
 * @author jeroen
 */
public abstract class InputFormat<F extends StructuredRecordFile, V extends org.apache.hadoop.io.Writable>
        extends FileInputFormat<LongWritable, V> {
    public static final String BUFFERSIZE = "inputformat.buffersize"; 
    
    public InputFormat(Class fileclass) {
        super(fileclass);
    }

    protected F getFile(Datafile datafile) throws ClassNotFoundException {
        Constructor constructor = ClassTools.getAssignableConstructor(fileclass, StructuredRecordFile.class, Datafile.class);
        return (F) ClassTools.construct(constructor, datafile);
    }

    public static void setBufferSize(Configuration conf, int buffersize) {
        conf.setInt(BUFFERSIZE, buffersize);
    }
    
    public static int getBufferSize(Configuration conf) {
        return conf.getInt(BUFFERSIZE, 1000000);
    }
}
