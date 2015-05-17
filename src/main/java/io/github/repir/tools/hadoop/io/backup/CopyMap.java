package io.github.repir.tools.hadoop.io.backup;

import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.ClassTools;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.NullWritable;

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
    public void setup(Context context) {
        if (fs == null) {
            fs = HDFSPath.getFS(context.getConfiguration());
        }
    }
    
    @Override
    public void map(String key, String value, Context context) throws IOException, InterruptedException {
        HDFSPath.copy(fs, value, value);
    }
}
