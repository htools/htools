package io.github.htools.hbase;

import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author Jeroen
 */
public abstract class AdjacencyTable extends TableF {
    public static Log log = new Log(AdjacencyTable.class);

    public AdjacencyTable(String tablename, String[] regions) {
        super(tablename, regions);
    }
    
    public void addRow(byte[] source, byte[] destination, byte[] value) {
        this.setRowKey(source);
        add(destination, value);
    }
    
    public void addRow(String source, String destination, byte[] value) {
        addRow(source.getBytes(), destination.getBytes(), value);
    }
    
    public void addRow(String source, String destination, int value) {
        addRow(ByteTools.toBytes(source), ByteTools.toBytes(destination), value);
    }
    
    public void addRow(byte[] source, byte[] destination, int value) {
        addRow(source, destination, Bytes.toBytes(value));
    }
    

}
