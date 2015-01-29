package io.github.repir.tools.hadoop.tsv;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredFileRecord;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.io.struct.StructuredRecordFileIterator;
import io.github.repir.tools.io.struct.StructuredTextTSV;
/**
 * Supports Hadoop access to file storage in a tab separated values file. The TSV
 * storage is easy for manual inspection, to use in-/output with other tsv interfaces
 * such as pig or Hadoop's NLineInputFormat.
 * @author jeroen
 */
public abstract class File<J extends StructuredFileRecord> extends StructuredTextTSV implements StructuredRecordFile<J> {
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
