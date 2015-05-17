package io.github.repir.tools.hadoop.multivalue;

import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author jeroen
 */
public class Job extends io.github.repir.tools.hadoop.Job {

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
