package io.github.repir.tools.io.struct;

import io.github.repir.tools.lib.Log;
import java.util.Iterator;

/**
 * Iterates over a StructuredRecordFile, returning the generic Record. 
 * Iteration can be bounded by setting the file offset and/or ceiling.
 * Removal is not supported, since reading from a file is assumed. 
 * Additionally, peek() can be used to peek ahead to the next Record.
 * @author jeroen
 */
public class StructuredRecordFileIterator<F extends StructuredRecordFile, R extends StructuredFileRecord> implements Iterator<R> {

    public static final Log log = new Log(StructuredRecordFileIterator.class);
    F file;
    R next;

    public StructuredRecordFileIterator(F file) {
        this.file = file;
        file.openRead();
        next();
    }

    @Override
    public boolean hasNext() {
       return next != null;
    }

    @Override
    public R next() {
        R current = next;
        if (file.nextRecord()) {
            next = (R) file.readRecord();
        } else
            next = null;
        if (next == null) {
            file.closeRead();
        }
        return current;
    }

    public R peek() {
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
