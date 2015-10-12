package io.github.htools.io.struct;

import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        try {
            this.file = file;
            file.openRead();
            next();
        } catch (IOException ex) { }
    }

    @Override
    public boolean hasNext() {
       return next != null;
    }

    @Override
    public R next() {
        R current = next;
        try {
            if (file.nextRecord()) {
                next = (R) file.readRecord();
            } else
                next = null;
        } catch (IOException ex) {
            next = null;
        }
        if (next == null) {
            try {
                file.closeRead();
            } catch (IOException ex) {}
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
