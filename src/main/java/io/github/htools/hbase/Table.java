package io.github.htools.hbase;

import io.github.htools.collection.ArrayMap3;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.Bytes;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.partition.TotalOrderPartitioner;

/**
 *
 * @author Jeroen
 */
public abstract class Table extends HTable {

    public static Log log = new Log(Table.class);
    protected final ImmutableBytesWritable tableKey;
    protected final ArrayList<HColumnDescriptor> columnFamilies = new ArrayList();
    private byte[][] regions;
    public byte[] rowkey;
    ArrayMap3<byte[], byte[], byte[]> values = new ArrayMap3();
    //TotalOrderPartitioner partitioner;

    protected Table(Configuration conf, String tableName, String[] regions) throws IOException {
        super(conf, tableName);
        this.tableKey = new ImmutableBytesWritable(tableName.getBytes());
        addRegions(regions);
    }

    protected void addColumnFamily(String family) {
        addColumnFamily(new HColumnDescriptor(ByteTools.toBytes(family)));
    }

    protected void addColumnFamily(HColumnDescriptor columnFamily) {
        columnFamilies.add(columnFamily);
    }

    private void addRegions(String... regions) {
        this.regions = new byte[regions.length][];
        for (int i = 0; i < regions.length; i++) {
            this.regions[i] = ByteTools.toBytes(regions[i]);
        }
    }

    public byte[][] getRegions() {
        return regions;
    }

    public String getStringName() {
        return getName().getNameAsString();
    }

    public String getSafeName() {
        return getStringName().replaceAll(":", "_");
    }

    public static String getSafeName(HTable table) {
        return table.getName().getNameAsString().replaceAll(":", "_");
    }

    /**
     * Create table in HBase.
     *
     * @param conf
     * @throws IOException when table exists
     */
    public void createTable() throws IOException {
        HBaseAdmin admin = new HBaseAdmin(getConfiguration());
        HTableDescriptor hTableDescriptor = getHTableDescriptor();
        admin.createTable(hTableDescriptor, regions);
    }

    /**
     * Drops table in HBase when it exists.
     *
     * @param conf
     * @throws IOException
     */
    public void dropTable() throws IOException {
        if (exists()) {
            HBaseAdmin admin = new HBaseAdmin(getConfiguration());
            admin.disableTable(getStringName());
            admin.deleteTable(getStringName());
        }
    }

    public static void loadTable(HTable table, String path) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        LoadIncrementalHFiles loader = new LoadIncrementalHFiles(table.getConfiguration());
        loadTable(table, loader, path);
    }
    
    public void loadTable(String path) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        loadTable(this, path);
    }
    
    public void loadTable(LoadIncrementalHFiles loader, 
            String path) 
            throws IOException, ClassNotFoundException, InterruptedException, Exception {
        loadTable(this, loader, path);
    }
    
    public static void loadTable(HTable table, 
            LoadIncrementalHFiles loader, 
            String path) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        loader.doBulkLoad(new Path(path), table);
    }
    
    /**
     * @param conf
     * @return true when table exists
     * @throws IOException
     */
    public boolean exists() throws IOException {
        HBaseAdmin admin = new HBaseAdmin(getConfiguration());
        return admin.tableExists(getStringName());
    }

    /**
     * @return The HTableDescriptor to create or alter the table
     */
    protected HTableDescriptor getHTableDescriptor() {
        HTableDescriptor hTableDescriptor = new HTableDescriptor(tableKey.get());
        for (HColumnDescriptor family : this.columnFamilies) {
            hTableDescriptor.addFamily(family);
        }
        return hTableDescriptor;
    }

    /**
     * For a row operation, set the row key. The row operations support only one
     * row at a time and are not thread safe.
     *
     * @param rowkey
     */
    public void setRowKey(byte[] rowkey) {
        this.rowkey = rowkey;
    }

    /**
     * For a row operation, set the row key. The row operations support only one
     * row at a time and are not thread safe.
     *
     * @param rowkey
     */
    public void setRowKey(String rowkey) {
        this.rowkey = Bytes.toBytes(rowkey);
    }

    /**
     * For a row operation, set the row key. The row operations support only one
     * row at a time and are not thread safe.
     *
     * @param rowkey
     */
    public void setRowKey(int rowkey) {
        this.rowkey = Bytes.toBytes(rowkey);
    }

    /**
     * For a row operation, set the row key. The row operations support only one
     * row at a time and are not thread safe.
     *
     * @param rowkey
     */
    public void setRowKey(long rowkey) {
        this.rowkey = Bytes.toBytes(rowkey);
    }

    /**
     * For a row operation, set a value for a column. The row operations support
     * setting multiple columns.
     *
     * @param columnFamily
     * @param column
     * @param value
     */
    public void add(byte[] columnFamily, byte[] column, byte[] value) {
        values.add(columnFamily, column, value);
    }

    /**
     * writes the current row operation as a Put, for use with
     * TableOutputFormat.
     *
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    public void put(TaskInputOutputContext<? extends Object, ? extends Object, ImmutableBytesWritable, Mutation> context) throws IOException, InterruptedException {
        Put put = createPut();
        context.write(tableKey, put);
        values = new ArrayMap3();
    }

    /**
     * @return a Put for the written setRowKey() and add()
     * @throws IOException
     * @throws InterruptedException
     */
    public Put createPut() throws IOException, InterruptedException {
        Put put = new Put(rowkey);

        // map of (columnFamility, column, value)
        for (Map.Entry<byte[], Tuple2<byte[], byte[]>> entry : values) {
            put.add(entry.getKey(), entry.getValue().key, entry.getValue().value);
        }
        return put;
    }

    /**
     * Writes the current row operation as a Put, for use with
     * HMultiFileOutputFormat
     *
     * @param context
     * @throws IOException
     * @throws InterruptedException
     */
    public void putBulkLoadMulti(TaskInputOutputContext<? extends Object, ? extends Object, ImmutableBytesWritable, Put> context) throws IOException, InterruptedException {
//        if (partitioner == null) {
//            partitioner = new TotalOrderPartitioner();
//            partitioner.setConf(context.getConfiguration());
//        }
        ImmutableBytesWritable key = new ImmutableBytesWritable(
                BulkOutputFormat.makeKey(getStringName(), rowkey)
        );
        Put put = createPut();
        context.write(key, put);
        values = new ArrayMap3();
    }

    public void putBulkLoadPut(TaskInputOutputContext<? extends Object, ? extends Object, ImmutableBytesWritable, Put> context) throws IOException, InterruptedException {
//        if (partitioner == null) {
//            partitioner = new TotalOrderPartitioner();
//            partitioner.setConf(context.getConfiguration());
//        }
        ImmutableBytesWritable key = new ImmutableBytesWritable(rowkey);
        Put put = createPut();
        context.write(key, put);
        values = new ArrayMap3();
    }
}
