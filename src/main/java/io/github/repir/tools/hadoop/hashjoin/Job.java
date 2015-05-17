package io.github.repir.tools.hadoop.hashjoin;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.io.MultiReduceWritable;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author jeroen
 */
public class Job extends io.github.repir.tools.hadoop.multivalue.Job {

    public static final Log log = new Log(Job.class);
    boolean setPartitionerClass = false;

    public Job(Configuration configuration, Object... params) throws IOException {
        // Jars need to be added to the Configuration before construction 
        super(configuration, params);
        super.setMapOutputValueClass(MultiReduceWritable.class);
    }

    @Override
    public boolean waitForCompletion(boolean verbose) throws ClassNotFoundException, InterruptedException {
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
}
