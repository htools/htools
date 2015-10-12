package io.github.htools.io.struct;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class StructuredFileSortedByteJumptable extends StructuredFileSort {

    public static Log log = new Log(StructuredFileSortedByteJumptable.class);
    public BufferReaderWriter residenttable;
    int tablesize;
    StructuredFileByteJumptableInternal idfile;
    protected int id = 0;

    public StructuredFileSortedByteJumptable(Datafile df) throws IOException {
        super(df);
    }

    public void setTableSize(int tablesize) {
        this.tablesize = tablesize;
    }

    @Override
    public void closeWrite() throws IOException {
        super.closeWrite();
        if (idfile != null) {
            idfile.closeWrite();
        }
    }

    @Override
    public void openWriteFinal() throws IOException {
        log.info("openWriteFinal()");
        id = 0;
        idfile = new StructuredFileByteJumptableInternal(new Datafile(this.destfile.getSubFile(".jumparray")));
        idfile.openWrite();
        super.openWriteFinal();
        this.getDatafile().write(tablesize);
    }

    @Override
    public void openRead() throws IOException {
        try {
            super.openRead();
            residenttable = new BufferReaderWriter(this.getDatafile().readFully());
            super.closeRead();
            tablesize = residenttable.readInt();
            idfile = new StructuredFileByteJumptableInternal(new Datafile(this.destfile.getSubFile(".jumparray")));
            idfile.openRead();
        } catch (EOCException ex) {
            log.fatalexception(ex, "openRead() residenttable %s datafile %s", residenttable, getDatafile());
        }
    }

    public StructuredFileSortJumptableRecord find(int id) throws IOException {
        //log.info("bucketindex %d", r.bucketindex);
        StructuredFileSortJumptableRecord record = (StructuredFileSortJumptableRecord) this.createRecord();
        long offset = idfile.getOffset(id);
        residenttable.setOffset(offset);
        int skip = idfile.getSkip(id);
        for (int i = 0; i <= skip; i++) {
            record.read();
        }
        return record;
    }

    @Override
    public StructuredFile clone() {
        StructuredFileSortedByteJumptable tuple = null;
        try {
            Constructor<? extends StructuredFileSortedByteJumptable> declaredConstructor = this.getClass().getDeclaredConstructor(Datafile.class);
            tuple = declaredConstructor.newInstance(new Datafile(this.getDatafile()));
            tuple.setTableSize(tablesize);
        } catch (Exception ex) {
            log.exception(ex, "clone() tablesize %d", tablesize);
        }
        return tuple;
    }
}
