package io.github.htools.io;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

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
public class HDFSOut implements DataOut {

    private static Log log = new Log(HDFSOut.class);
    public final FileSystem fs;
    public final Path path;
    public int buffersize;
    public FSDataOutputStream fsout;
    public BufferReaderWriter buffer;
    public int lc = 0;

    protected HDFSOut(FileSystem fs, Path path, int buffersize) {
        this.fs = fs;
        this.path = path;
        this.buffersize = buffersize;
    }

    protected HDFSOut(FileSystem fs, String filename, int buffersize) {
        this(fs, new Path(filename), buffersize);
    }

    public void setBuffer(BufferReaderWriter buffer) {
        this.buffer = buffer;
    }

    public static void delete(FileSystem fs, Path path) {
        try {
            fs.delete(path, false);
        } catch (IOException ex) {
            log.exception(ex, "delete( %s, %s )", fs, path);
        }
    }

    public static void setReplication(FileSystem fs, Path path, short replication) {
        try {
            fs.setReplication(path, replication);
        } catch (IOException ex) {
            log.exception(ex, "delete( %s, %s )", fs, path);
        }
    }

    public static void delete(FileSystem fs, String filename) {
        delete(fs, new Path(filename));
    }

    public static void delete(HDFSOut out) {
        delete(out.fs, out.path);
    }

    public static void delete(HDFSIn in) {
        delete(in.fs, in.path);
    }

    @Override
    public void close() {
        log.info("close(%s)", this.path.toString());
        flushBuffer(buffer);
        try {
            fsout.close();
        } catch (Exception ex) {
            log.fatalexception(ex, "close() buffer %s fsout %s", buffer, fsout);
        }
        fsout = null;
    }

    @Override
    public void flushBuffer(BufferReaderWriter buffer) {
        try {
            if (buffer.buffer != null) {
                //log.info("flushBuffer %d %d", buffer.getOffset() - buffer.bufferpos, buffer.getOffset());

                fsout.write(buffer.buffer, 0, buffer.bufferpos);
                buffer.offset += buffer.bufferpos;
                buffer.bufferpos = 0;
                if (buffer.getRequestedBufferSize() != buffer.getBufferSize()) {
                    buffer.resize(buffer.getRequestedBufferSize());
                }
            }
        } catch (IOException ex) {
            log.fatal(ex);
        }
    }

    public void flushFile() {
        try {
            flushBuffer(buffer);
            fsout.sync();
        } catch (IOException ex) {
            log.fatal(ex);
        }
    }

    public long getOffset() {
        return buffer.offset + buffer.bufferpos;
    }

    @Override
    public void openWrite() {
        log.info("openWrite(%s)", this.path.toString());
        buffer.offset = 0;
        try {
            fsout = fs.create(path, true, buffersize);
        } catch (IOException ex) {
            log.exception(ex, "openWrite( %s %d )", path.toString(), buffersize);
        }
    }

    public static boolean lock(FileSystem fs, String file, String lockfile) {
        Path filepath = new Path(file);
        Path lockfilepath = new Path(lockfile);
        long filesize = 0;
        int attempt = 0;
        try {
            if (!fs.exists(filepath) && !fs.exists(lockfilepath)) {
                if (fs.mkdirs(lockfilepath)) {
                    return true;
                }
            }
            do {
                if (!fs.exists(lockfilepath)) {
                    if (fs.mkdirs(lockfilepath)) {
                        return true;
                    }
                }

                long currentsize = 0;
                try {
                    currentsize = HDFSIn.getLength(fs, filepath);
                } catch (IOException ex) {
                }

                if (currentsize != filesize) {
                    filesize = currentsize;
                    attempt = 0;
                }
                log.sleep(500);
            } while (attempt++ < 40);
        } catch (IOException ex) {
        }
        return false;
    }

    public static void unlock(FileSystem fs, String lockfile) {
        Path lockfilepath = new Path(lockfile);
        try {
            if (fs.exists(lockfilepath)) {
                fs.delete(lockfilepath, true);
            }
        } catch (IOException ex) {
        }
    }

    public static boolean waitForUnlock(FileSystem fs, String file, String lockfile) {
        Path lockfilepath = new Path(lockfile);
        Path filepath = new Path(file);
        long newsize = 0, filesize = 0;
        int attempt = 0;
        try {
            do {
                if (!fs.exists(lockfilepath)) {
                    return true;
                }
                long currentsize = fs.exists(lockfilepath) ? HDFSIn.getLength(fs, filepath) : 0;
                if (currentsize != filesize) {
                    filesize = currentsize;
                    attempt = 0;
                }
                log.sleep(1000);
            } while (attempt++ < 20);
        } catch (IOException ex) {
        }
        return false;
    }

    @Override
    public OutputStream getOutputStream() {
        return fsout;
    }
}
