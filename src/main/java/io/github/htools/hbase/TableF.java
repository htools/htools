package io.github.htools.hbase;

import io.github.htools.lib.Log;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author Jeroen
 */
public abstract class TableF extends Table {
    public static Log log = new Log(TableF.class);

    public TableF(String tablename, String[] regions) {
        super(tablename, regions);
        addColumnFamily("f");
    }
    
    public void add(byte[] column, byte[] value) {
        add(columnFamilies.get(0).getName(), column, value);
    }
}
