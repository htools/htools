package io.github.htools.hadoop.structured;

import io.github.htools.hadoop.InputFormat;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredFile;
import io.github.htools.io.struct.StructuredRecordFile;
import io.github.htools.io.struct.StructuredRecordFileIterator;
import io.github.htools.lib.Log;

import java.io.IOException;
/**
 * Supports Hadoop access to file storage in a structured binary file. While data
 * stored in this format is not easy to manually inspect, is does allow for any
 * type of data to be stored and transmitted. Structured files do not support
 * file splitting, therefore {@link InputFormat#setNonSplitable(io.github.htools.hadoop.Job) }
 * must be used.
 * @author jeroen
 */
public abstract class File<J extends Writable> extends StructuredFile implements StructuredRecordFile<J> {
   public static final Log log = new Log( File.class );

   public File(Datafile df) throws IOException {
       super(df);
   }
   
    @Override
    public J readRecord() throws IOException {
        J u = newRecord();
        u.read(this);
        return u;
    }

   @Override
    public void setDatafile(Datafile df) {
        super.setDatafile(df);
    }
    
    @Override
    public StructuredRecordFileIterator<File, J> iterator() {
        return new StructuredRecordFileIterator(this);
    }

    @Override
    public void write(J record) {
        record.write(this);
    }

    @Override
    public boolean findFirstRecord() {
        if (getOffset() == 0 && getOffset() < getCeiling()) {
            return true;
        }
        throw new UnsupportedOperationException("Strcutured Binary files do not support file splitting!");
    }
}
