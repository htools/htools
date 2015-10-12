package io.github.htools.hadoop.xml;

import io.github.htools.io.Datafile;
import io.github.htools.lib.Log;
import io.github.htools.io.struct.StructuredFileRecord;
import io.github.htools.io.struct.StructuredRecordFile;
import io.github.htools.io.struct.StructuredRecordFileIterator;
import io.github.htools.io.struct.StructuredTextXML;
import java.io.IOException;

/**
 * Supports Hadoop access to file storage in an XML file.
 * @author jeroen
 */
public abstract class File<J extends StructuredFileRecord> 
        extends StructuredTextXML 
        implements StructuredRecordFile<J> {
   public static final Log log = new Log( File.class );

   public File(Datafile df) {
       super(df);
   }
   
    @Override
    public J readRecord() throws IOException {
        J u = newRecord();
        u.read(this);
        return u;
    }

    @Override
    public StructuredRecordFileIterator<File, J> iterator() {
        return new StructuredRecordFileIterator(this);
    }

    @Override
    public void write(J record) throws IOException {
        record.write(this);
    }
}
