package io.github.repir.tools.hadoop.hashjoin;

import io.github.repir.tools.lib.Log;
import static io.github.repir.tools.lib.PrintTools.*;
import io.github.repir.tools.hadoop.io.MultiReduceWritable;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author jeroen
 */
public class Job extends io.github.repir.tools.hadoop.Job {

    public static final Log log = new Log(Job.class);
    public final InputFormat iformat;
    boolean setPartitionerClass = false;

    public Job(Configuration configuration, Object... params) throws IOException {
        // Jars need to be added to the Configuration before construction 
        super(configuration, params);
        iformat = new InputFormat(this);
        super.setMapOutputValueClass(MultiReduceWritable.class);
    }

    @Override
    public boolean waitForCompletion(boolean verbose) throws IOException, InterruptedException, ClassNotFoundException {
        if (!this.setPartitionerClass) {
            super.setPartitionerClass(HashPartitioner.class);
        }
        return super.waitForCompletion(verbose);
    }

    @Override
    public final void setMapOutputValueClass(Class c) {
        log.warn("Are you sure you want to set a different OutputValueClass? %s instead of %s", c.getCanonicalName(), this.getMapOutputValueClass().getCanonicalName());
        super.setMapOutputValueClass(c);
    }
    
    public final void setPartitionerClass(Class c) {
        super.setPartitionerClass(c);
        this.setPartitionerClass = true;
    }

    public void addFileClass(String pattern, Class fileclass, Class writableclass) {
        iformat.add(pattern, fileclass, writableclass);
    }

    public void addInputPath(String inputpath) throws IOException {
        InputFormat.addInputPaths(this, inputpath);
    }
}
