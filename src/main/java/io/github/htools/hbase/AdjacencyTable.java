package io.github.htools.hbase;

import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author Jeroen
 */
public abstract class AdjacencyTable extends TableF {
    public static Log log = new Log(AdjacencyTable.class);

    public AdjacencyTable(Configuration conf, String tablename, String[] regions) throws IOException {
        super(conf, tablename, regions);
    }
    
    public void addRow(byte[] source, byte[] destination, byte[] value) {
        this.setRowKey(source);
        add(destination, value);
    }
    
    public void addRow(String source, String destination, byte[] value) {
        addRow(ByteTools.toBytes(source), ByteTools.toBytes(destination), value);
    }
    
    public void addRow(byte[] source, String destination, byte[] value) {
        addRow(source, ByteTools.toBytes(destination), value);
    }
    
    public void addRow(byte[] source, String destination, int value) {
        addRow(source, ByteTools.toBytes(destination), value);
    }
    
    public void addRow(String source, String destination, int value) {
        addRow(ByteTools.toBytes(source), ByteTools.toBytes(destination), value);
    }
    
    public void addRow(byte[] source, byte[] destination, int value) {
        addRow(source, destination, Bytes.toBytes(value));
    }
    

}
