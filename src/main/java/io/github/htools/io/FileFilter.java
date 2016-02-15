package io.github.htools.io;

import org.apache.hadoop.fs.Path;

/**
 * Created by iloen on 30-01-16.
 */
public interface FileFilter {
    public static FileFilter exceptSuccess = new FileFilter() {
        public boolean allow(Path path) {
            return !path.getName().equals("_SUCCESS");
        }
    };

    public static FileFilter acceptAll = new FileFilter() {
        public boolean allow(Path path) {
            return true;
        }
    };

    boolean allow(Path path);
}
