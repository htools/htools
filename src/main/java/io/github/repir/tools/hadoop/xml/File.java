package io.github.repir.tools.hadoop.xml;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredFileRecord;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.io.struct.StructuredRecordFileIterator;
import io.github.repir.tools.io.struct.StructuredTextXML;

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
    public J readRecord() {
        J u = newRecord();
        u.read(this);
        return u;
    }

    @Override
    public StructuredRecordFileIterator<File, J> iterator() {
        return new StructuredRecordFileIterator(this);
    }

    @Override
    public void write(J record) {
        record.write(this);
    }
}
