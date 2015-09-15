package io.github.htools.hadoop.io;

import io.github.htools.hadoop.Job;
import io.github.htools.io.Datafile;
import io.github.htools.io.DirComponent;
import io.github.htools.lib.ClassTools;
import io.github.htools.io.struct.StructuredRecordFile;
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
        return conf.getInt(BUFFERSIZE, 10000000);
    }
}
