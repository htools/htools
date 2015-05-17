package io.github.repir.tools.hadoop.structured;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredFile;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.io.struct.StructuredRecordFileIterator;
import io.github.repir.tools.hadoop.InputFormat;
/**
 * Supports Hadoop access to file storage in a structured binary file. While data
 * stored in this format is not easy to manually inspect, is does allow for any
 * type of data to be stored and transmitted. Structured files do not support
 * file splitting, therefore {@link InputFormat#
 * @author jeroen
 */
public abstract class File<J extends Writable> extends StructuredFile implements StructuredRecordFile<J> {
   public static final Log log = new Log( File.class );

   public File(Datafile df) {
       super(df);
   }
   
    @Override
    public J readRecord() {
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
