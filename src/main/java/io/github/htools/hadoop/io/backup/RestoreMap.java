package io.github.htools.hadoop.io.backup;

import io.github.htools.hadoop.ContextTools;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;

/**
 * Clusters the titles of one single day, starting with the clustering results
 * at the end of yesterday,
 *
 * @author jeroen
 */
public class RestoreMap extends CopyMap {

    public static final Log log = new Log(RestoreMap.class);
    FileSystem fs;

    @Override
    public void setup(Context context) throws IOException {
        if (fs == null) {
            fs = ContextTools.getFileSystem(context);
        }
    }

    @Override
    public void map(String key, String value, Context context) {
        try {
            log.info("%s -> %s", key, value);
            long intime = HDFSPath.getLastModified(fs, key);
            long outtime = 0;
            outtime = HDFSPath.getLastModified(fs, value);
            if (outtime > intime) {
                HDFSPath.copy(fs, key, value);
            }
        } catch (IOException ex) {
            log.exception(ex, "copy %s %s", key, value);
        }
    }
}
