package io.github.htools.hadoop.io.backup;

import io.github.htools.hadoop.ContextTools;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Clusters the titles of one single day, starting with the clustering results
 * at the end of yesterday,
 *
 * @author jeroen
 */
public class CopyMap extends Mapper<String, String, NullWritable, NullWritable> {

    public static final Log log = new Log(CopyMap.class);
    FileSystem fs;
    
    @Override
    public void setup(Context context) throws IOException {
        if (fs == null) {
            fs = ContextTools.getFileSystem(context);
        }
    }
    
    @Override
    public void map(String key, String value, Context context) throws IOException, InterruptedException {
        HDFSPath.copy(fs, key, value);
    }
}
