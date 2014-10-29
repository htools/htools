package io.github.repir.tools.hadoop.Structured;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Structure.StructuredRecordFile;
import io.github.repir.tools.Structure.StructuredRecordFileIterator;
import io.github.repir.tools.Structure.StructuredTextTSV;
/**
 *
 * @author jeroen
 */
public abstract class File<J extends Writable> extends StructuredTextTSV implements StructuredRecordFile<J> {
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
