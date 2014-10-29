package io.github.repir.tools.hadoop.HashJoin;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;

/**
 *
 * @author jeroen
 */
public class Job extends io.github.repir.tools.hadoop.Job {

    public static final Log log = new Log(Job.class);
    public final InputFormat iformat;

    public Job(Configuration configuration) throws IOException {
        // Jars need to be added to the Configuration before construction 
        super(configuration);
        iformat = new InputFormat(this);

        super.setMapOutputKeyClass(TextType.class);
        super.setMapOutputValueClass(BytesWritable.class);

        super.setGroupingComparatorClass(TextType.Comparator.class);
        super.setSortComparatorClass(TextType.SecondarySort.class);
        super.setPartitionerClass(HashPartitioner.class);
    }

    public Job(Configuration configuration, String jobname, Object... params) throws IOException {
        this(configuration);
        setJobName(PrintTools.sprintf(jobname, params));
    }
    
    public void addFileClass(String pattern, Class fileclass) {
        iformat.add(pattern, fileclass);
    }
    
    public void addInputPath(String inputpath) throws IOException {
        InputFormat.addInputPaths(this, inputpath);
    }
}
