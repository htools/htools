package io.github.repir.tools.hadoop.tsv;

import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.hadoop.io.ConstInputFormat;
import io.github.repir.tools.hadoop.io.FileInputFormat;
import io.github.repir.tools.hadoop.io.IntStringInputSplit;
import io.github.repir.tools.hadoop.io.MRInputSplit;
import io.github.repir.tools.hadoop.io.InputFormatPerSplit;
import io.github.repir.tools.hadoop.io.StringStringInputSplit;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.Text;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. This class should be used as static, using
 * {@link #setRepository(Repository.Repository)} to initialize and 
 * {@link #add(Repository.Repository, IndexReader.Query) }
 * to add Query requests to the MapReduce job. Internally, a separate InputSplit
 * is created for each repository partition. Whenever a Query request is added,
 * it is added to each Split.
 * <p/>
 * When cansplit==true, then the InputSplits are divided over 2 * nodes in
 * cluster (as defined in cluster.nodes), to divide the workload more evenly.
 *
 * @author jeroen
 */
public abstract class StringFileInputFormat extends InputFormatPerSplit<String> {

    public static Log log = new Log(StringFileInputFormat.class);
    
    public StringFileInputFormat() {
    }

    public static void add(Job job, int size, HDFSPath path) throws IOException {
        StringFileInputFormat.setMaxSplitSize(size);
        for (Datafile df : path.getFiles())
            add(job, df);
    }
    
    public static void add(Job job, HDFSPath path) throws IOException {
        for (Datafile df : path.getFiles())
            add(job, df);
    }
    
    public static void add(Job job, int size, Datafile df) {
        StringFileInputFormat.setMaxSplitSize(size);
        add(job, df);
    }
    
    public static void add(Job job, Datafile df) {
        String readAsString = df.readAsString();
        if (readAsString != null && readAsString.length() > 0) {
            String lines[] = readAsString.split("\n+");
            for (String line : lines) {
                add(job, line);
            }
        }
    }
}
