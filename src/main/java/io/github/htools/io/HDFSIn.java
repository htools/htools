package io.github.htools.io;

import io.github.htools.lib.Log;
import java.io.IOException;
import java.io.InputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;

/**
 * This class is intended to remove all the Java fuzz regarding files. There is
 * just one class RFile that provides methods to read a line, read the entire
 * thing, write stuff to it, without having bother about which stream to use.
 * However, Java objects like properly opened FileInputStream and FileChannel.
 * <br><br> Some methods are provided that will more easily allow to get
 * information on the file, such as the parent Dir object, the filename,
 * extension, etc. <br><br> Some static methods are provided to do big file
 * operations, such as copying, moving, running and converting a File to a
 * primitive.
 * <p>
 * @author jbpvuurens
 */
public class HDFSIn extends ISDataIn {

    private static Log log = new Log(HDFSIn.class);
    public FileSystem fs;
    public Path path;

    public HDFSIn(FileSystem fs, Path path, boolean open) {
        this.path = path;
        this.fs = fs;
    }

    public HDFSIn(FileSystem fs, Path path) {
        this(fs, path, true);
    }

    public HDFSIn(FileSystem fs, String filename) {
        this(fs, new Path(filename));
    }

    @Override
    public long getLength() throws IOException {
        return getLength(fs, path);
    }

    public static long getLength(FileSystem fs, Path path) throws IOException {
        return fs.getFileStatus(path).getLen();
    }

    public static long getLengthNoExc(FileSystem fs, Path path) {
        try {
            return fs.getFileStatus(path).getLen();
        } catch (IOException ex) {
            log.fatalexception(ex, "getLengthNoExc() %s", path);
            return -1;
        }
    }

    public void mustMoveBack() {
        try {
            close();
            resetOffset();
            getInputStream();
        } catch (IOException ex) {
            log.fatalexception(ex, "mustMoveBack() %s", this.path.toString());
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (inputstream == null) {
            inputstream = fs.open(path, 4096);
            CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(fs.getConf());
            CompressionCodec codec = compressionCodecs.getCodec(path);
            if (codec != null) {
                inputstream = codec.createInputStream(inputstream);
            }
        }
        return inputstream;
    }
}
