package io.github.htools.hadoop.tsv;

import io.github.htools.hadoop.Job;
import io.github.htools.hadoop.io.ConstInputFormat;
import io.github.htools.hadoop.io.FileInputFormat;
import io.github.htools.hadoop.io.IntStringInputSplit;
import io.github.htools.hadoop.io.MRInputSplit;
import io.github.htools.hadoop.io.InputFormatPerSplit;
import io.github.htools.hadoop.io.StringStringInputSplit;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.Text;

/**
 * A custom implementation of Hadoop's InputFormat, that holds the InputSplits
 * that are to be retrieved. 
 * <p>
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
