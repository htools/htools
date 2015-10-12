package io.github.htools.hbase;

import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author Jeroen
 */
public abstract class TableF extends Table {
    public static Log log = new Log(TableF.class);

    public TableF(Configuration conf, String tablename, String[] regions) throws IOException {
        super(conf, tablename, regions);
        addColumnFamily("f");
    }
    
    public void add(byte[] column, byte[] value) {
        add(columnFamilies.get(0).getName(), column, value);
    }
}
