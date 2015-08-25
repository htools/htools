package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Structure is intended to store a static set of records, that are
 * internally sorted, and accessible through an internal hash collision table.
 * <p>
 * This structure contains one fixed element, which is a bucketindex,
 *
 * @author jer
 */
public abstract class StructuredFileCollision extends StructuredFileSort {

    public static Log log = new Log(StructuredFileCollision.class);
    protected IntField bucketindex = this.addInt("bucketindex");
    BufferReaderWriter residenttable;
    int tablesize;
    StructuredFileCollisionInternal hashfile;

    public StructuredFileCollision(Datafile df) {
        super(df);
        setLoadFactor(3);
    }

    public abstract StructuredFile clone();

    public void setTableSize(int tablesize) {
        this.tablesize = tablesize;
        this.setCapacity(tablesize);
    }

    public int getTableSize() {
        return tablesize;
    }

    @Override
    public void closeWrite() {
        super.closeWrite();
        if (hashfile != null) {
            hashfile.closeWrite(bucketcapacity);
        }
    }

    @Override
    public void openWriteFinal() {
        hashfile = new StructuredFileCollisionInternal(new Datafile(this.destfile.getSubFile(".hash")));
        hashfile.openWrite();
        this.remove(bucketindex);
        super.openWriteFinal();
        this.getDatafile().write(bucketcapacity); // allows to use readFully
    }

    @Override
    public void openRead() {
        try {
            this.remove(bucketindex);
            super.openRead();
            residenttable = new BufferReaderWriter(getDatafile().readFully());
            super.closeRead();
            bucketcapacity = residenttable.readInt();
            hashfile = new StructuredFileCollisionInternal(new Datafile(this.destfile.getSubFile(".hash")));
            hashfile.openRead();
        } catch (EOCException ex) {
            log.fatalexception(ex, "openRead() bucketindex %d residenttable %s", bucketindex, residenttable);
        }
    }

    @Override
    public int compare(StructuredFileSort o1, StructuredFileSort o2) {
        int comp = ((StructuredFileCollision) o1).bucketindex.value - ((StructuredFileCollision) o2).bucketindex.value;
        if (comp == 0) {
            comp = secondaryCompare(o1, o2);
        }
        return (comp != 0) ? comp : 1;
    }

    public abstract int secondaryCompare(StructuredFileSort o1, StructuredFileSort o2);

    @Override
    public int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
        int comp = ((StructuredFileCollisionRecord) o1).getBucketIndex() - ((StructuredFileCollisionRecord) o2).getBucketIndex();
        if (comp == 0) {
            comp = secondaryCompare(o1, o2);
        }
        return (comp != 0) ? comp : 1;
    }

    public abstract int secondaryCompare(StructuredFileSortRecord o1, StructuredFileSortRecord o2);

    public StructuredFileCollisionRecord find(StructuredFileCollisionRecord r) {
        //log.info("bucketindex %d", r.bucketindex);
        long offset = hashfile.getOffset(r.getBucketIndex());
        residenttable.setOffset(offset);
        return find(residenttable, r);
    }

    public abstract StructuredFileCollisionRecord find(BufferReaderWriter table, StructuredFileCollisionRecord r);
}
