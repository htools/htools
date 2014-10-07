package io.github.repir.tools.Content;

import com.google.gson.JsonObject;
import io.github.repir.tools.Structure.StructureWriter;
import io.github.repir.tools.Structure.StructureData;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.PrintTools;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
public class Datafile implements StructureData, Comparable<Object>, ByteSearchReader {

    public static Log log = new Log(Datafile.class);

    public enum Status {

        CLOSED,
        READ,
        WRITE,
        APPEND
    }
    private String filename;
    private final String lockfile;
    private boolean lockIsMine = false;
    protected String tempdir;
    protected boolean intemp = false;
    protected FileSystem fs;
    public Status status = Status.CLOSED;
    protected boolean hasbeenopened = false;
    public final BufferReaderWriter rwbuffer = new BufferReaderWriter();

    protected Datafile(String filename, Status status) {
        this(filename);
        open(status);
    }

    public Datafile(String filename) {
        this.filename = filename;
        lockfile = filename + ".lock";
    }

    protected Datafile(String filename, String dir) {
        this(filename);
        tempdir = dir;
        intemp = true;
    }

    public Datafile(FileSystem fs, String filename) {
        this(filename);
        this.fs = fs;
    }

    public Datafile(FileSystem fs, Path path) {
        this(path.toString());
        this.fs = fs;
    }

    public Datafile(Datafile df) {
        this(df.filename);
        this.fs = df.fs;
    }

    public Datafile(Datafile df, String suffix) {
        this(df.filename + "." + suffix);
        this.fs = df.fs;
    }

    protected Datafile(FSDataInputStream in, long offset, long end) {
        rwbuffer.setDataIn(new HDFSIn(in));
        rwbuffer.setOffset(offset);
        rwbuffer.setCeiling(end);
        status = Status.READ;
        lockfile = null;
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

    public void write(byte[] b, byte[] esc, byte escape) {
        rwbuffer.write(b, esc, escape);
    }

    public void write(byte[] b, byte[] esc, byte esc2[], byte escape) {
        rwbuffer.write(b, esc, esc2, escape);
    }

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
            try {
                fs.delete(new Path(filename), false);
            } catch (IOException ex) {
                log.exception(ex, "delete() when deleting file %s using FileSystem %s", filename, fs);
            }
        } else {
            new FSFile(filename).delete();
        }
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
            return FSDir.exists(lockfile) || FSFile.exists(filename);
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
            return FSDir.exists(lockfile);
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

    public String getFullPath() {
        return filename;
    }

    public void setFileSystem(FileSystem fs) {
        this.fs = fs;
    }

    public Datafile getSubFile(String ext) {
        Datafile df = new Datafile(fs, this.filename + ext);
        return df;
    }

    public Datafile otherDir(Dir dir) {
        Datafile df = new Datafile(fs, dir.getFilename(this.getFilename()));
        return df;
    }

    public FileSystem getFileSystem() {
        return fs;
    }

    public Dir getDir() {
        int dirpos = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        if (fs != null) {
            if (HDFSDir.isDir(fs, new Path(this.filename))) {
                return new HDFSDir(fs, filename);
            } else {
                return new HDFSDir(fs, filename.substring(0, dirpos));
            }
        } else {
            if (FSDir.isDir(filename)) {
                return new FSDir(filename);
            } else {
                return new FSDir(filename.substring(0, dirpos));
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
        status = Status.CLOSED;
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
        open(Status.WRITE);
    }

    public void openAppend() {
        open(Status.APPEND);
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public boolean isReadOpen() {
        return status == Status.READ;
    }

    public boolean isWriteOpen() {
        return status == Status.WRITE;
    }

    public void openRead() {
        if (status == Status.READ) {
            return;
        }
        if (status == Status.WRITE) {
            close();
        }
        //log.info("open() %b %b offset %d ceiling %d", this.getOffset(), this.getCeiling());
        open(Status.READ);
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

    protected void open(Status newstatus) {
        //log.info("openFirst %s %s", newstatus, this.getfilename());
        if (status == newstatus) {
            return;
        }
        if (status != Status.CLOSED) {
            close();
        }
        switch (newstatus) {
            case CLOSED:
                status = Status.CLOSED;
                break;
            case READ:
                if (!lockIsMine && isLocked()) {
                    waitForUnlock();
                }
                if (fs == null) {
                    if (intemp) {
                        rwbuffer.setDataIn(new FSFileInBuffer(FSFile.tempfilename(filename, tempdir)));
                    } else {
                        rwbuffer.setDataIn(new FSFileInBuffer(filename));
                    }
                } else {
                    rwbuffer.setDataIn(new HDFSIn(fs, filename));
                }
                status = newstatus;
                break;
            case WRITE:
                delete();
                if (fs == null) {
                    if (intemp) {
                        rwbuffer.setDataOut(new FSFileOutBuffer(FSFile.tempfilename(filename, tempdir)));
                    } else {
                        rwbuffer.setDataOut(new FSFileOutBuffer(filename));
                    }
                } else {
                    rwbuffer.setDataOut(new HDFSOut(fs, filename, rwbuffer.getBufferSize()));
                }
                rwbuffer.openWrite();
                status = Status.WRITE;
                break;
            case APPEND:
                if (fs == null) {
                    if (intemp) {
                        rwbuffer.setDataOut(new FSFileOutBuffer(FSFile.tempfilename(filename, tempdir)));
                    } else {
                        rwbuffer.setDataOut(new FSFileAppendBuffer(filename));
                    }
                } else {
                    rwbuffer.setDataOut(new HDFSAppend(fs, filename, rwbuffer.getBufferSize()));
                }
                status = Status.WRITE;
                rwbuffer.openWrite();
                break;
        }
        hasbeenopened = true;
    }

    public boolean rename(Datafile df) throws Exception {
        boolean result = false;
        if (status != Status.CLOSED) {
            log.info("File %s must be closed before you can move");
        } else {
            if (fs == null) {
                result = FSDir.rename(getFullPath(), df.getFullPath());
            } else {
                result = HDFSDir.rename(fs, getFullPath(), df.getFullPath());
            }
            if (result) {
                filename = df.getFullPath();
            }
        }
        return result;
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
        if (status == Status.READ) {
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

    public int compareTo(Object o) {
        return this.getFullPath().compareTo(((Datafile) o).getFullPath());
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
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt();
    }

    public int readInt2() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt2();
    }

    public int readInt3() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readInt3();
    }

    public double readDouble() throws EOCException {
        if (status != Status.READ) {
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
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLong();
    }

    public long readCLong() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLong();
    }

    public long[] readLongArray() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLongArray();
    }

    public long[][] readCLongArray2() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLongArray2();
    }

    public String[] readStringArray() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readStringArray();
    }

    public long[] readCLongArray() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCLongArray();
    }

    public long[] readLongSparse() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readLongSparse();
    }

    public int[] readIntSparse() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readIntSparse();
    }

    public double[] readDoubleSparse() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readDoubleSparse();
    }

    public HashMap<Integer, Long> readSparseLongMap() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSparseLongMap();
    }

    public HashMap<Integer, Integer> readSparseIntMap() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSparseIntMap();
    }

    public double readCDouble() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCDouble();
    }

    public int readCInt() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readCInt();
    }

    public int[] readIntArray() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readIntArray();
    }

    public int[][] readSquaredIntArray2() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readSquaredIntArray2();
    }

    public int[] readCIntArray() throws EOCException {
        if (status != Status.READ) {
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
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readByte();
    }

    public byte[] readByteBlock() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readByteBlock();
    }

    public boolean readBoolean() throws EOCException {
        return readByte() == 0 ? false : true;
    }

    public byte[] readBytes(int count) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readBytes(count);
    }

    public int readBytes(long offset, byte[] b, int pos, int len) {
        if (status != Status.READ) {
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
     * @return byte array with the contents of a special datafile, that uses the
     * first 4 bytes as the file's length.
     */
    public byte[] readFully() {
        openRead();
        byte[] b = new byte[(int) this.getLength()];
        rwbuffer.readBytes(0, b, 0, (int) this.getLength());
        return b;
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
        if (status == Status.WRITE) {
            rwbuffer.write(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write2(int i) {
        if (status == Status.WRITE) {
            rwbuffer.write2(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write3(int i) {
        if (status == Status.WRITE) {
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
        if (status == Status.WRITE) {
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
        if (status == Status.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(double l) {
        if (status == Status.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l) {
        if (status == Status.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(long l[]) {
        if (status == Status.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l[][]) {
        if (status == Status.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSquared(int i[][]) {
        if (status == Status.WRITE) {
            rwbuffer.writeSquared(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(String l[]) {
        if (status == Status.WRITE) {
            rwbuffer.write(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(long l[]) {
        if (status == Status.WRITE) {
            rwbuffer.writeC(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparse(long l[]) {
        if (status == Status.WRITE) {
            rwbuffer.writeSparse(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparse(double l[]) {
        if (status == Status.WRITE) {
            rwbuffer.writeSparse(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparseLong(Map<Integer, Long> l) {
        if (status == Status.WRITE) {
            rwbuffer.writeSparseLong(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeSparseInt(Map<Integer, Integer> l) {
        if (status == Status.WRITE) {
            rwbuffer.writeSparseInt(l);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(int i) {
        if (status == Status.WRITE) {
            rwbuffer.writeC(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(int i[]) {
        if (status == Status.WRITE) {
            rwbuffer.write(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(Collection<Integer> al) {
        if (status == Status.WRITE) {
            rwbuffer.write(al);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeC(int i[]) {
        if (status == Status.WRITE) {
            rwbuffer.writeC(i);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(double d) {
        if (status == Status.WRITE) {
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
        if (status == Status.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
        }
    }

    public void write(JsonObject o) {
       if (status == Status.WRITE) {
            rwbuffer.write(o);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
        }
    }

    public void write0(JsonObject o) {
       if (status == Status.WRITE) {
            rwbuffer.write0(o);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
        }
    }

    /**
     * writes the StringBuilder as {@link #write(java.lang.String) }
     * <p/>
     * @param s
     */
    public void write(StringBuilder s) {
        if (status == Status.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile %s has to be put in a specific write mode before writing", this.getFullPath());
        }
    }

    @Override
    public String readString0() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readString0();
    }

    @Override
    public void write0(String s) {
        if (status == Status.WRITE) {
            rwbuffer.write0(s);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public double[] readDoubleArray() throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readDoubleArray();
    }

    public void write(double[] s) {
        if (status == Status.WRITE) {
            rwbuffer.write(s);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(byte bytes[]) {
        if (status == Status.WRITE) {
            rwbuffer.write(bytes);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void writeByteBlock(byte bytes[]) {
        if (status == Status.WRITE) {
            rwbuffer.writeByteBlock(bytes);
        } else {
            log.fatal("DataFile has to be put in a specific write mode before writing");
        }
    }

    public void write(byte bytes[], int pos, int endpos) {
        if (status == Status.WRITE) {
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
        this.openWrite();
        String s = io.github.repir.tools.Lib.PrintTools.sprintf(format, args);
        //log.info("printf( %s )", s);
        rwbuffer.print(s);
    }

    public String readAsString() {
        this.openRead();
        byte b[] = new byte[(int) this.getLength()];
        this.readBytes(b, 0, (int) this.getLength());
        this.close();
        return new String(b);
    }

    public InputStream getInputStream() {
        openRead();
        return this.rwbuffer.datain.getInputStream();
    }

    public OutputStream getOutputStream() {
        openWrite();
        return this.rwbuffer.dataout.getOutputStream();
    }

    public boolean skipStart(ByteSearch regex) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.skipStart(regex);
    }

    public boolean skipEnd(ByteSearch regex) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.skipEnd(regex);
    }

    @Override
    public String readStringUntil(ByteSearch eof) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.readStringUntil(eof);
    }

    @Override
    public boolean match(ByteSearch field) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.match(field);
    }

    @Override
    public String findString(ByteSearch needle) throws EOCException, FileClosedOnReadException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findString(needle);
    }

    @Override
    public String matchString(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.matchString(needle);
    }

    @Override
    public String matchTrimmedString(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.matchTrimmedString(needle);
    }

    @Override
    public String findTrimmedString(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findTrimmedString(needle);
    }

    @Override
    public String findFullTrimmedString(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findFullTrimmedString(needle);
    }

    @Override
    public String matchFullTrimmedString(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.matchFullTrimmedString(needle);
    }

    @Override
    public ByteSearchPosition matchPos(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.matchPos(needle);
    }

    @Override
    public ByteSearchPosition findPos(ByteSearch needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findPos(needle);
    }

    @Override
    public ByteSearchSection findSection(ByteSection needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findSection(needle);
    }

    @Override
    public ByteSearchSection findSectionStart(ByteSection needle) throws EOCException {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        return rwbuffer.findSectionStart(needle);
    }

    @Override
    public void movePast(ByteSearchPosition section) {
        if (status != Status.READ) {
            throw new FileClosedOnReadException(this);
        }
        rwbuffer.movePast(section);
    }

}
