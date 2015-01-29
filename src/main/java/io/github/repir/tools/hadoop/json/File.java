package io.github.repir.tools.hadoop.json;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.io.struct.StructuredRecordFileIterator;
import io.github.repir.tools.io.struct.StructuredTextTSV;

/**
 * Supports Hadoop access to file storage in a tab separated values file, while
 * the data is being transmitted in a json object, which in general is not as
 * fast as tsv, but allows for easy transmission of complex data structures such
 * as nested collections or maps.
 *
 * @author jeroen
 */
public abstract class File<J extends Writable> extends StructuredTextTSV implements StructuredRecordFile<J> {

    public static final Log log = new Log(File.class);

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
