package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;

/**
 * @author jeroen
 */
public abstract class FileClassInputFormat<K, V>
        extends FileInputFormat<K, V> {

    public static Log log = new Log(FileClassInputFormat.class);
    protected final Class fileclass;

    public FileClassInputFormat(Class fileclass) {
        this.fileclass = fileclass;
    }
}
