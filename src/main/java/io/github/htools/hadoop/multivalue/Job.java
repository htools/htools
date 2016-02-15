package io.github.htools.hadoop.multivalue;

import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 *
 * @author jeroen
 */
public class Job extends io.github.htools.hadoop.Job {

    public static final Log log = new Log(Job.class);
    public final InputFormat iformat;

    public Job(Configuration configuration, Object... params) throws IOException {
        // Jars need to be added to the Configuration before construction 
        super(configuration, params);
        iformat = new InputFormat(this);
    }

    public void addFileClass(String pattern, Class fileclass, Class writableclass) {
        iformat.add(pattern, fileclass, writableclass);
    }

    public void addInputPath(String inputpath) throws IOException {
        InputFormat.addInputPaths(this, inputpath);
    }
}
