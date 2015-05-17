package io.github.repir.tools.io;

import com.google.gson.JsonObject;
import io.github.repir.tools.io.struct.StructureWriter;
import io.github.repir.tools.io.struct.StructureData;
import io.github.repir.tools.io.buffer.BufferReaderWriter;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.io.ByteSearchReader;
import io.github.repir.tools.lib.Const;
import static io.github.repir.tools.lib.Const.NULLLONG;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.PrintTools;
import static io.github.repir.tools.lib.PrintTools.sprintf;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * This class wraps three ways to read/write data to a file: read the whole file
 * buffered in sequence, write the whole file buffered in sequence or random
 * access (go to a position and start reading or writing from there).
 * <p/>
 * @author Jeroen
 */
public class Datafile implements StructureData, Comparable<Datafile>, ByteSearchReader, DirComponent {

    public static Log log = new Log(Datafile.class);

    public enum STATUS {

        CLOSED,
        READ,
        WRITE,
        APPEND
    }

    public enum TYPE {

        FS,
        HDFS,
        TEMPFS,
        TEMPHDFS,
        IS
    }
    private TYPE type;
    private String filename;
    private final String lockfile;
    private boolean lockIsMine = false;
    protected String tempdir;
    protected boolean intemp = false;
    protected FileSystem fs;
    public STATUS status = STATUS.CLOSED;
    protected boolean hasbeenopened = false;
    public final BufferReaderWriter rwbuffer = new BufferReaderWriter();

    protected Datafile(String filename, STATUS status) {
        this(filename);
        open(status);
    }

    public Datafile(String filename) {
        type = TYPE.FS;
        this.filename = filename;
        lockfile = filename + ".lock";
    }

    protected Datafile(String filename, String dir) {
        this(filename);
        tempdir = dir;
    }

    public Datafile(FileSystem fs, String filename) {
        this(filename);
        type = TYPE.HDFS;
        this.fs = fs;
    }

    public Datafile(Configuration conf, String filename) {
        this(HDFSPath.getFS(conf), filename);
    }

    public Datafile(FileSystem fs, Path path) {
        this(fs, path.toString());
    }

    public Datafile(Configuration conf, Path path) {
        this(HDFSPath.getFS(conf), path.toString());
    }

    public Datafile(DataIn is) {
        type = TYPE.IS;
        rwbuffer.setDataIn(is);
        status = STATUS.READ;
        lockfile = null;
    }

    public Datafile(Datafile df) {
        this(df.filename);
        this.type = df.type;
        this.fs = df.fs;
        this.setOffset(df.getOffset());
        this.setCeiling(df.getCeiling());
        this.setBufferSize(df.getBufferSize());
    }

    public Datafile(Datafile df, String suffix) {
        this(df.filename + "." + suffix);
        this.type = df.type;
        this.fs = df.fs;
    }

    protected Datafile(FSDataInputStream in, long offset, long end) {
        this(new ISDataIn(in));
        rwbuffer.setOffset(offset);
        rwbuffer.setCeiling(end);
    }

    public void readBuffer(DataInput in) {
        rwbuffer.readBuffer(in);
    }

    public void writeBuffer(DataOutput out) {
        rwbuffer.writeBuffer(out);
    }

    public void writeBuffer(StructureWriter writer) {
        rwbuffer.writeBuffer(writer);
    }

//    public void write(byte[] b, byte[] esc, byte escape) {
//        rwbuffer.write(b, esc, escape);
//    }
//
//    public void write(byte[] b, byte[] esc, byte esc2[], byte escape) {
//        rwbuffer.write(b, esc, esc2, escape);
//    }
    public int[] readCIntIncr() throws EOCException {
        return rwbuffer.readCIntIncr();
    }

    public void writeIncr(int[] s) {
        rwbuffer.writeIncr(s);
    }

    public void writeIncr(ArrayList<Integer> list) {
        rwbuffer.writeIncr(list);
    }

    public void delete() {
        if (fs != null) {
            HDFSPath.delete(fs, new Path(filename));
        } else {
            new FSFile(filename).delete();
        }
    }

    public void trash() {
        if (fs != null) {
            try {
                HDFSPath.trash(fs, new Path(filename));
            } catch (IOException ex) {
                log.exception(ex, "delete() when deleting file %s using FileSystem %s", filename, fs);
            }
        } else {
            new FSFile(filename).delete();
        }
    }

    public long getLastModified() {
        if (fs != null) {
            try {
                return HDFSPath.getLastModified(fs, filename);
            } catch (IOException ex) {
                log.exception(ex, "delete() when deleting file %s using FileSystem %s", filename, fs);
            }
        } else {
            return FSPath.getLastModified(filename);
        }
        return NULLLONG;
    }

    /**
     * @param df
     * @return true if new than df, false if either path does not exist
     */
    public boolean newerThan(Datafile df) {
        long modificationtime = getLastModified();
        if (modificationtime == NULLLONG) {
            return false;
        }
        long modificationtime_df = df.getLastModified();
        if (modificationtime_df == NULLLONG) {
            return false;
        }
        return modificationtime > modificationtime_df;
    }

    public void closeRead() {
        close();
    }

    public void closeWrite() {
        this.close();
    }

    public boolean exists() {
        if (this.fs != null) {
            try {
                return fs.exists(new Path(lockfile))
                        || fs.exists(new Path(filename));
            } catch (IOException ex) {
                log.exception(ex, "exists() %s %s", filename, lockfile);
                return true;
            }
        } else {
            return FSPath.exists(lockfile) || FSFile.exists(filename);
        }
    }

    public boolean isLocked() {
        if (this.fs != null) {
            try {
                return fs.exists(new Path(lockfile));
            } catch (IOException ex) {
                log.exception(ex, "exists() %s %s", filename, lockfile);
                return false;
            }
        } else {
            return FSPath.exists(lockfile);
        }
    }

    public boolean hasLock() {
        return lockIsMine;
    }

    public boolean lock() {
        if (lockIsMine) {
            throw new RuntimeException(PrintTools.sprintf("Cannot Double lock a file: %s", this.filename));
        }
        if (this.fs != null) {
            lockIsMine = HDFSOut.lock(fs, filename, lockfile);
        } else {
            lockIsMine = FSFileOutBuffer.lock(filename, lockfile);
        }
        return lockIsMine;
    }

    public void unlock() {
        if (!lockIsMine) {
            throw new RuntimeException(PrintTools.sprintf("Cannot unlock a file I havnt locked: %s", this.filename));
        }
        if (this.fs != null) {
            HDFSOut.unlock(fs, lockfile);
        } else {
            FSFileOutBuffer.unlock(lockfile);
        }
        lockIsMine = false;
    }

    public void waitForUnlock() {
        if (lockIsMine) {
            throw new RuntimeException(PrintTools.sprintf("Cannot wait for unlock I have: %s", this.filename));
        }
        if (this.fs != null) {
            HDFSOut.waitForUnlock(fs, filename, lockfile);
        } else {
            FSFileOutBuffer.waitForUnlock(filename, lockfile);
        }
    }

    public String getFilename() {
        return filename.substring(Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\')) + 1);
    }

    public String getCanonicalPath() {
        return filename;
    }

    @Override
    public String toString() {
        return sprintf("Datafile(%s)", filename);
    }

    public void setFileSystem(FileSystem fs) {
        this.fs = fs;
    }

    public Datafile getSubFile(String ext) {
        Datafile df = new Datafile(fs, this.filename + ext);
        return df;
    }

    public Datafile otherDir(io.github.repir.tools.io.Path dir) {
        Datafile df = dir.getFile(this.getFilename());
        return df;
    }

    public FileSystem getFileSystem() {
        return fs;
    }

    public io.github.repir.tools.io.Path getDir() {
        int dirpos = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (fs != null) {
            if (HDFSPath.isDir(fs, new Path(this.filename))) {
                return new HDFSPath(fs, filename);
            } else {
                return new HDFSPath(fs, filename.substring(0, dirpos));
            }
        } else {
            if (FSPath.isDir(filename)) {
                return new FSPath(filename);
            } else {
                return new FSPath(filename.substring(0, dirpos));
            }
        }
    }

    @Override
    public void setCeiling(long ceiling) {
        //openRead();
        rwbuffer.setCeiling(ceiling);
    }

    @Override
    public void setOffset(long offset) {
        //openRead();
        rwbuffer.setOffset(offset);
    }

    public void setAppendOffset(long offset) {
        rwbuffer.setAppendOffset(offset);
    }

    public void close() {
        switch (status) {
            case WRITE:
                rwbuffer.closeWrite();
                break;
            case READ:
                rwbuffer.closeRead();
                break;
        }
        //rwbuffer.setDataIn( null );
        status = STATUS.CLOSED;
        hasbeenopened = false;
    }

    public void openReadKeepOffset() {
        long offset = rwbuffer.offset;
        long ceiling = rwbuffer.ceiling;
        openRead();
        setOffset(offset);
        setCeiling(ceiling);
    }

    public void resetStart() {
        rwbuffer.offset = rwbuffer.bufferpos = 0;
        rwbuffer.setEnd(0);
        rwbuffer.ceiling = Long.MAX_VALUE;
        rwbuffer.hasmore = true;
    }

    public void openWrite() {
        open(STATUS.WRITE);
    }

    public void openAppend() {
        open(STATUS.APPEND);
    }

    public boolean isClosed() {
        return status == STATUS.CLOSED;
    }

    public boolean isReadOpen() {
        return status == STATUS.READ;
    }

    public boolean isWriteOpen() {
        return status == STATUS.WRITE;
    }

    public void openRead() {
        if (status == STATUS.READ) {
            return;
        }
        if (status == STATUS.WRITE) {
            close();
        }
        //log.info("open() %b %b offset %d ceiling %d", this.getOffset(), this.getCeiling());
        open(STATUS.READ);
        //log.info("open() offset %d ceiling %d", this.getOffset(), this.getCeiling());
    }

    @Override
    public void setBufferSize(int buffersize) {
        rwbuffer.setBufferSize(buffersize);
    }

    public void setReplication(int replication) {
        if (fs != null) {
            HDFSOut.setReplication(fs, new Path(filename), (short) replication);
        }
    }

    @Override
    public int getBufferSize() {
        return rwbuffer.getBufferSize();
    }

    @Override
    public void fillBuffer() throws EOCException {
        this.rwbuffer.fillBuffer();
    }

    @Override
    public void reuseBuffer() {
        this.rwbuffer.reuseBuffer();
    }

    protected DataIn getDataIn() {
        if (this.isReadOpen()) {
            return rwbuffer.datain;
        }
        if (type == TYPE.FS) {
            if (intemp) {
                return new FSFileInBuffer(FSFile.tempfilename(filename, tempdir));
            } else {
                return new FSFileInBuffer(filename);
            }
        } else if (type == TYPE.HDFS) {
            return new HDFSIn(fs, filename);
        } else {
            log.fatal("attempt to open Datafile type %s", type.toString());
        }
        return null;
    }

    protected void open(STATUS newstatus) {
        //log.info("openFirst %s %s", newstatus, this.getfilename());
        if (status == newstatus) {
            return;
        }
        if (status != STATUS.CLOSED) {
            close();
        }
        switch (newstatus) {
            case CLOSED:
                status = STATUS.CLOSED;
                break;
            case READ:
                if (!lockIsMine && isLocked()) {
                    waitForUnlock();
                }
                rwbuffer.setDataIn(getDataIn());
                status = newstatus;
                break;
            case WRITE:
                delete();
                if (type == TYPE.FS) {
                    if (intemp) {
                        rwbuffer.setDataOut(new FSFileOutBuffer(FSFile.tempfilename(filename, tempdir)));
                    } else {
                        rwbuffer.setDataOut(new FSFileOutBuffer(filename));
                    }
                } else if (type == TYPE.HDFS) {
                    rwbuffer.setDataOut(new HDFSOut(fs, filename, rwbuffer.getBufferSize()));
                } else {
                    log.fatal("attempt to close Datafile type %s", type.toString());
                }
                rwbuffer.openWrite();
                status = STATUS.WRITE;
                break;
            case APPEND:
                if (type == TYPE.FS) {
                    if (intemp) {
                        rwbuffer.setDataOut(new FSFileOutBuffer(FSFile.tempfilename(filename, tempdir)));
                    } else {
                        rwbuffer.setDataOut(new FSFileAppendBuffer(filename));
                    }
                } else if (type == TYPE.HDFS) {
                    rwbuffer.setDataOut(new HDFSAppend(fs, filename, rwbuffer.getBufferSize()));
                } else {
                    log.fatal("attempt to append Datafile type %s", type.toString());
                }
                status = STATUS.WRITE;
                rwbuffer.openWrite();
                break;
        }
        hasbeenopened = true;
    }

    public boolean move(Datafile df) throws IOException {
        boolean result = false;
        if (status != STATUS.CLOSED) {
            log.info("File %s must be closed before you can move");
        } else {
            if (type == TYPE.FS) {
                if (df.type == TYPE.FS) {
                    result = FSPath.rename(getCanonicalPath(), df.getCanonicalPath());
                } else if (df.type == TYPE.HDFS) {
                    copy(df);
                    this.delete();
                }
            } else if (type == TYPE.HDFS) {
                if (df.type == TYPE.HDFS) {
                    if (df.exists()) {
                        df.delete();
                    }
                } else if (df.type == TYPE.FS) {
                    copy(df);
                    this.delete();
                }
                result = HDFSPath.rename(fs, getCanonicalPath(), df.getCanonicalPath());
            } else {
                log.fatal("attempt to rename Datafile type %s", type.toString());
            }
            if (result) {
                filename = df.getCanonicalPath();
            }
        }
        return result;
    }

    public boolean copy(Datafile df) throws IOException {
        switch (type) {
            case FS:
                switch (df.type) {
                    case FS:
                        FSPath.copy(this.getCanonicalPath(), df.getCanonicalPath());
                        return true;
                    case HDFS:
                        HDFSPath.copyFromLocal(df.fs, this.getCanonicalPath(), df.getCanonicalPath());
                        return true;
                }
                break;
            case HDFS:
                switch (df.type) {
                    case FS:
                        HDFSPath.copyToLocal(fs, this.getCanonicalPath(), df.getCanonicalPath());
                        return true;
                    case HDFS:
                        HDFSPath.copy(fs, this.getCanonicalPath(), df.getCanonicalPath());
                        return true;
                }
        }
        return false;
    }

    public boolean setTimestamp(long timestamp) throws IOException {
        if (type == TYPE.FS) {
            return FSPath.setLastModified(filename, timestamp);
        } else if (type == TYPE.HDFS) {
            HDFSPath.setLastModified(fs, filename, timestamp);
            return true;
        }
        return false;
    }

    /**
     * determines if end of file has not been reached in either buffered reading
     * or random access mode.
     * <p/>
     * @return true if EOF has not been reached false if end of file has been
     * reached, in case of an exception, or if the file was not in buffered
     * reading or random reading mode
     */
    public boolean hasMore() {
        //log.info("hasmore %s", status);
        if (status == STATUS.READ) {
            return rwbuffer.hasMore();
        }
        return false;
    }

    public void flush() {
        rwbuffer.flushBuffer();
    }

    public void write(Map<String, String> map) {
        rwbuffer.write(map);
    }

    public Map<String, String> readStringPairMap() throws EOCException {
        return rwbuffer.readStringPairMap();
    }

    public void skip(int bytes) {
        rwbuffer.skip(bytes);
    }

    public void skipInt() {
        rwbuffer.skipInt();
    }

    public void skipLong() {
        rwbuffer.skipInt();
    }

    public void skipDouble() {
        rwbuffer.skipDouble();
    }

    public void skipByte() {
        rwbuffer.skipByte();
    }

    public void skipByteBlock() throws EOCException {
        rwbuffer.skipByteBlock();
    }

    public void skipCInt() throws EOCException {
        rwbuffer.skipCInt();
    }

    public void skipCLong() throws EOCException {
        rwbuffer.skipCLong();
    }

    public void skipCDouble() throws EOCException {
        rwbuffer.skipCDouble();
    }

    public void skipString() throws EOCException {
        rwbuffer.skipString();
    }

    public void skipJson() throws EOCException {
        rwbuffer.skipJson();
    }

    public void skipStringBuilder() throws EOCException {
        rwbuffer.skipStringBuilder();
    }

    public void skipString0() throws EOCException {
        rwbuffer.skipString0();
    }

    public void skipJson0() throws EOCException {
        rwbuffer.skipJson0();
    }

    public void skipIntArray() throws EOCException {
        rwbuffer.skipIntArray();
    }

    public void skipBoolArray() throws EOCException {
        rwbuffer.skipBoolArray();
    }

    public void skipSquaredIntArray2() throws EOCException {
        rwbuffer.skipSquaredIntArray2();
    }

    public void skipLongArray() throws EOCException {
        rwbuffer.skipLongArray();
    }

    public void skipCLongArray2() throws EOCException {
        rwbuffer.skipCLongArray2();
    }

    public void skipDoubleArray() throws EOCException {
        rwbuffer.skipDoubleArray();
    }

    public void skipDoubleSparse() throws EOCException {
        rwbuffer.skipDoubleSparse();
    }

    public void skipStringArray() throws EOCException {
        rwbuffer.skipStringArray();
    }

    public void skipCIntArray() throws EOCException {
        rwbuffer.skipCIntArray();
    }

    public void skipCLongArray() throws EOCException {
        rwbuffer.skipCLongArray();
    }

    public void skipLongSparse() throws EOCException {
        rwbuffer.skipLongSparse();
    }

    public void skipIntSparse() throws EOCException {
        rwbuffer.skipIntSparse();
    }

    public void skipStringPairMap() throws EOCException {
        rwbuffer.skipStringPairMap();
    }

    public void setDataIn(DataIn in) {
        this.rwbuffer.setDataIn(in);
    }

    public void setDataOut(DataOut out) {
        this.rwbuffer.setDataOut(out);
    }

    public ArrayList<Integer> readCIntArrayList() throws EOCException {
        return this.rwbuffer.readCIntArrayList();
    }

    public ArrayList<Integer> readIntArrayList() throws EOCException {
        return this.rwbuffer.readIntArrayList();
    }

    public ArrayList<String> readStrArrayList() throws EOCException {
        return this.rwbuffer.readStrArrayList();
    }

    public void writeC(ArrayList<Integer> s) {
        rwbuffer.writeC(s);
    }

    public void writeStr(Collection<String> s) {
        rwbuffer.writeStr(s);
    }

    public int compareTo(Datafile o) {
        return this.getCanonicalPath().compareTo(o.getCanonicalPath());
    }

    public int[][] readIntSparse2() throws EOCException {
        return rwbuffer.readIntSparse2();
    }

    public void skipIntSparse2() throws EOCException {
        rwbuffer.skipIntSparse2();
    }

    public void writeSparse(int[][] i) {
        rwbuffer.writeSparse(i);
    }

    public int[][][] readIntSparse3() throws EOCException {
        return rwbuffer.readIntSparse3();
    }

    public void skipIntSparse3() throws EOCException {
        rwbuffer.skipIntSparse3();
    }

    public void writeSparse(int[][][] i) {
        rwbuffer.writeSparse(i);
    }

    public int[][][] readSquaredIntArray3() throws EOCException {
        return rwbuffer.readSquaredIntArray3();
    }

    public void skipSquaredIntArray3() throws EOCException {
        rwbuffer.skipSquaredIntArray3();
    }

    public void writeSquared(int[][][] i) {
        rwbuffer.writeSquared(i);
    }

    public long[][] readLongSparse2() throws EOCException {
        return rwbuffer.readLongSparse2();
    }

    public void skipLongSparse2() throws EOCException {
        rwbuffer.skipLongSparse2();
    }

    public void writeSparse(long[][] l) {
        rwbuffer.writeSparse(l);
    }

    public int[][] readCIntArray2() throws EOCException {
        return rwbuffer.readCIntArray2();
    }

    public void skipCIntArray2() throws EOCException {
        rwbuffer.skipCIntArray2();
    }

    public void writeC(int[][] i) {
        rwbuffer.writeC(i);
    }

    public int[][][] readCIntArray3() throws EOCException {
        return rwbuffer.readCIntArray3();
    }

    public void skipCIntArray3() throws EOCException {
        rwbuffer.skipCIntArray3();
    }

    public void writeC(int[][][] i) {
        rwbuffer.writeC(i);
    }

    /**
     * reads a 4 byte int from the current file position. The file position
     * advances by 4. If the file was not in buffered reading or random access
     * mode, the file is closed and opened in buffered reading mode.
     * <p/>
     * @return int
     */
    public int readInt() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt();
    }

    public int readInt2() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt2();
    }

    public int readInt3() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt3();
    }

    public double readDouble() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readDouble();
    }

    /**
     * reads an 8 byte long from the current file position. The file position
     * advances by 8. If the file was not in buffered reading or random access
     * mode, the file is closed and opened in buffered reading mode.
     * <p/>
     * @return
     */
    public long readLong() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLong();
    }

    public long readCLong() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLong();
    }

    public long[] readLongArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLongArray();
    }

    public long[][] readCLongArray2() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLongArray2();
    }

    public String[] readStringArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readStringArray();
    }

    public long[] readCLongArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLongArray();
    }

    public long[] readLongSparse() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLongSparse();
    }

    public int[] readIntSparse() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readIntSparse();
    }

    public double[] readDoubleSparse() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readDoubleSparse();
    }

    public HashMap<Integer, Long> readSparseLongMap() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSparseLongMap();
    }

    public HashMap<Integer, Integer> readSparseIntMap() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSparseIntMap();
    }

    public double readCDouble() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCDouble();
    }

    public int readCInt() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCInt();
    }

    public int[] readIntArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readIntArray();
    }

    public int[][] readSquaredIntArray2() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSquaredIntArray2();
    }

    public int[] readCIntArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCIntArray();
    }

    /**
     * reads 1 byte from the current file position. The file position advances
     * by 1. If the file was not in buffered reading or random access mode, the
     * file is closed and opened in buffered reading mode.
     */
    public int readByte() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readByte();
    }

    public byte[] readByteArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readByteArray();
    }

    public boolean[] readBoolArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readBoolArray();
    }

    public boolean readBoolean() throws EOCException {
        return readByte() == 0 ? false : true;
    }

    public byte[] readBytes(int count) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readBytes(count);
    }

    public int readBytes(long offset, byte[] b, int pos, int len) {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readBytes(offset, b, pos, len);
    }

    public int readBytes(byte[] b, int pos, int len) {
        switch (status) {
            case READ:
                return rwbuffer.readBytes(rwbuffer.getOffset(), b, pos, len);
        }
        return 0;
    }

    /**
     * reads a 4 byte int that indicates the String length from the current file
     * position. Then reads as many bytes as the length dictates into a String.
     * The file position advances by 4 + length of the String. If the file was
     * not in buffered reading or random access mode, the file is closed and
     * opened in buffered reading mode.
     */
    public String readString() throws EOCException {
        return rwbuffer.readString();
    }

    public <T> T read(Type type) throws EOCException {
        return rwbuffer.read(type);
    }

    public void write(Object o, Type type) throws EOCException {
        rwbuffer.write(o, type);
    }

    public JsonObject readJson() throws EOCException {
        return rwbuffer.readJson();
    }

    public JsonObject readJson0() throws EOCException {
        return rwbuffer.readJson0();
    }

    public StringBuilder readStringBuilder() throws EOCException {
        return rwbuffer.readStringBuilder();
    }

    public String readString(int length) throws EOCException {
        return rwbuffer.readString(length);
    }

    /**
     * writes a 4 byte int to the file at the current file position. The file
     * position advances by 4. This only works in buffered writing or random
     * access mode.
     * <p/>
     * @param i
     */
    public void write(int i) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write2(int i) {
        if (status == STATUS.WRITE) {
            rwbuffer.write2(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write3(int i) {
        if (status == STATUS.WRITE) {
            rwbuffer.write3(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    /**
     * writes 1 byte to the file at the current file position. The file position
     * advances by 1. This only works in buffered writing or random access mode.
     * <p/>
     * @param b
     */
    public void write(byte b) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(b);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(boolean b) {
        write((byte) (b ? -1 : 0));
    }

    public void writeUB(int b) {
        write((byte) ((b > 255) ? 255 : (b & 0xFF)));
    }

    /**
     * writes an 8 byte long to the file at the current file position. The file
     * position advances by 8. This only works in buffered writing or random
     * access mode.
     * <p/>
     * @param l
     */
    public void write(long l) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(double l) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(long l[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l[][]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSquared(int i[][]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeSquared(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(String l[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparse(long l[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeSparse(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparse(double l[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeSparse(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparseLong(Map<Integer, Long> l) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeSparseLong(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparseInt(Map<Integer, Integer> l) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeSparseInt(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(int i) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeC(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(int i[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(boolean i[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(Collection<Integer> al) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(al);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(int i[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeC(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(double d) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(d);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    /**
     * writes the string length as a 4 byte int to the file at the current file
     * position. Then writes the amount of bytes the string consists of. Every
     * character is written as a single byte. The file position advances by 4 +
     * string length. This only works in buffered writing or random access mode.
     * <p/>
     * @param s
     */
    public void write(String s) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getCanonicalPath());
        }
    }

    public void write(JsonObject o) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(o);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getCanonicalPath());
        }
    }

    public void write0(JsonObject o) {
        if (status == STATUS.WRITE) {
            rwbuffer.write0(o);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getCanonicalPath());
        }
    }

    /**
     * writes the StringBuilder as {@link #write(java.lang.String) }
     * <p/>
     * @param s
     */
    public void write(StringBuilder s) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getCanonicalPath());
        }
    }

    @Override
    public String readString0() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readString0();
    }

    @Override
    public void write0(String s) {
        if (status == STATUS.WRITE) {
            rwbuffer.write0(s);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public double[] readDoubleArray() throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readDoubleArray();
    }

    public void write(double[] s) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeRaw(byte bytes[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.writeRaw(bytes);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(byte bytes[]) {
        if (status == STATUS.WRITE) {
            rwbuffer.write(bytes);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(byte bytes[], int pos, int endpos) {
        if (status == STATUS.WRITE) {
            for (; pos < endpos; pos++) {
                rwbuffer.write(bytes[pos]);
            }
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparse(int[] l) {
        rwbuffer.writeSparse(l);
    }

    public void writeSparse(int[] l, int offset, int length) {
        rwbuffer.writeSparse(l, offset, length);
    }

    /**
     * returns the current file position of the DataFile.
     * <p/>
     * @return current file position or -1 if closed or an exception occurred.
     */
    public long getOffset() {
        return rwbuffer.getOffset();
    }

    public long getCeiling() {
        return rwbuffer.getCeiling();
    }

    public long getLength() {
        if (fs == null) {
            if (intemp) {
                return new FSFile(FSFile.tempfilename(filename, tempdir)).getLength();
            } else {
                return new FSFile(filename).getLength();
            }
        } else {
            try {
                return HDFSIn.getLength(fs, new Path(filename));
            } catch (IOException ex) {
                return -1;
            }
        }
    }

    public void printf(String format, Object... args) {
        if (!isWriteOpen()) {
            this.openWrite();
        }
        String s = PrintTools.sprintf(format, args);
        //log.info("printf( %s )", s);
        rwbuffer.print(s);
    }

    public void print(String s) {
        if (!isWriteOpen()) {
            this.openWrite();
        }
        rwbuffer.print(s);
    }

    public String readAsString() {
        return new String(readFully());
    }

    public byte[] readFully() {
        try {
            DataIn dataIn = getDataIn();
            return dataIn.readFully();
        } catch (EOCException | IOException ex) {
            log.fatalexception(ex, "readFully() %s", this.getCanonicalPath());
        }
        return null;
    }

    public InputStream getInputStream() throws IOException {
        if (!isReadOpen()) {
            this.openRead();
        }
        return this.rwbuffer.datain.getInputStream();
    }

    public OutputStream getOutputStream() {
        if (!isWriteOpen()) {
            this.openWrite();
        }
        return this.rwbuffer.dataout.getOutputStream();
    }

    public boolean skipUntil(ByteSearch regex) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.skipUntil(regex);
    }

    public boolean skipPast(ByteSearch regex) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.skipPast(regex);
    }

    @Override
    public String readStringUntil(ByteSearch eof) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readStringUntil(eof);
    }

    static ByteSearch EOL = ByteSearch.create("\n");
    public String readLine() throws EOCException {
        return readStringUntil(EOL);
    }

    @Override
    public boolean match(ByteSearch field) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.match(field);
    }

    @Override
    public String readString(ByteSearch needle) throws EOCException, FileClosedOnReadException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readString(needle);
    }

    @Override
    public String readMatchingString(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readMatchingString(needle);
    }

    @Override
    public String readMatchingTrimmedString(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readMatchingTrimmedString(needle);
    }

    @Override
    public String readTrimmedString(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readTrimmedString(needle);
    }

    @Override
    public String readFullTrimmedString(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readFullTrimmedString(needle);
    }

    @Override
    public String readMatchingFullTrimmedString(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readMatchingFullTrimmedString(needle);
    }

    @Override
    public ByteSearchPosition matchPos(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.matchPos(needle);
    }

    @Override
    public ByteSearchPosition readPos(ByteSearch needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readPos(needle);
    }

    @Override
    public ByteSearchSection readSection(ByteSection needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSection(needle);
    }

    @Override
    public ByteSearchSection readSectionStart(ByteSection needle) throws EOCException {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSectionStart(needle);
    }

    @Override
    public void movePast(ByteSearchPosition section) {
        if (status != STATUS.READ) {
            throw new FileClosedOnReadException(this);
        }
        rwbuffer.movePast(section);
    }

    public Iterable<String> readLines() {
        return new LineIterator(this);
    }
    
    static class LineIterator implements Iterator<String>, Iterable<String> {
        Datafile df;
        String next;
        
        public LineIterator(Datafile df) {
            this.df = new Datafile(df);
            this.df.openRead();
        }
        
        @Override
        public boolean hasNext() {
            return df.getOffset() < df.getLength();
        }

        @Override
        public String next() {
            return df.readLine();
        }

        @Override
        public Iterator<String> iterator() {
            return this;
        }
    }
    
}
