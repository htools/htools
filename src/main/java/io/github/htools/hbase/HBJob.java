package io.github.htools.hbase;

import io.github.htools.hadoop.Job;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.UUID;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;

/**
 * Extension of Job that sets the configuration to work with HBase.
 *
 * @author jer
 */
public class HBJob extends Job {

    public static Log log = new Log(HBJob.class);

    /**
     * Additionally to the super constructor, the HBase resources and security
     * credentials are added to the configuration, which is necessary to use
     * HBase from the cluster.
     *
     * @param configuration
     * @param parameters parameters to be added to the jobname
     * @throws IOException
     * @see Job
     */
    public HBJob(Configuration configuration, Object... parameters) throws IOException {
        super(configuration, parameters);
        HBaseConfiguration.addHbaseResources(conf);
        TableMapReduceUtil.initCredentials(this);
    }

    public void doBulkLoad(String... tableNames) throws ClassNotFoundException, InterruptedException, Exception {
        doBulkLoad(true, tableNames);
    }

    /**
     * Setup a job using BulkOutputFormat, to prepare writes to HBase tables by
     * writing Puts to HFiles in a non-customizable reducer, and bulk loading
     * these files into HBase tables, which is much faster than using
     *
     * @param verbose
     * @param tableNames
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws Exception
     */
    public void doBulkLoad(boolean verbose, HTable... tables) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        ClassTools.preLoad(LoadIncrementalHFiles.class);

        HDFSPath bulkLoadPath = doCreateHFiles(verbose, tables);

        if (bulkLoadPath != null) {
            // Load generated HFiles into table
            LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
            for (int i = 0; i < tables.length; i++) {
                String tableName = Table.getSafeName(tables[i]);
                Table.loadTable(tables[i],
                        loader,
                        bulkLoadPath.getSubdir(tableName).getCanonicalPath());
            }
        } else {
            log.info("loading failed.");
        }
    }

    /**
     * Setup a job using BulkOutputFormat, to prepare writes HFiles in a
     * non-customizable reducer, as a preparation to bulk load into HBase
     *
     * @param verbose
     * @param tables tables to load
     * @return null when failed, otherwise the Path where the HFiles are stored.
     * Different from the standard bulktool, the tablename (namespace_table) is
     * the first sub folder.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws Exception
     */
    public HDFSPath doCreateHFiles(boolean verbose, HTable... tables) throws IOException, ClassNotFoundException, InterruptedException, Exception {

        // setup the bulkload temp folder
        HDFSPath bulkLoadPath = new HDFSPath(
                getConfiguration(),
                "/tmp/" + UUID.randomUUID().toString());
        if (bulkLoadPath.existsDir()) {
            bulkLoadPath.trash();
        }

        // setup the job
        this.setMapOutputKeyClass(ImmutableBytesWritable.class);
        this.setMapOutputValueClass(Put.class);
        BulkOutputFormat.configureIncrementalLoad(this, tables);
        BulkOutputFormat.setOutputPath(this, bulkLoadPath);
        if (waitForCompletion(verbose)) {
            log.info("created HFiles in %s", bulkLoadPath.getCanonicalPath());
            return bulkLoadPath;
        } else {
            return null;
        }
    }

    public void doBulkLoadSinglePut(boolean verbose, HTable table) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        ClassTools.preLoad(LoadIncrementalHFiles.class);

        // setup the bulkload temp folder
        HDFSPath bulkLoadPath = new HDFSPath(
                getConfiguration(),
                "/tmp/" + UUID.randomUUID().toString());
        if (bulkLoadPath.existsDir()) {
            bulkLoadPath.trash();
        }

        // setup the job
        setMapOutputKeyClass(ImmutableBytesWritable.class);
        setMapOutputValueClass(Put.class);
        HFileOutputFormat2.configureIncrementalLoad(this, table);
        HFileOutputFormat2.setOutputPath(this, bulkLoadPath);
        if (waitForCompletion(verbose)) {
            // Load generated HFiles into table
            LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
            loader.doBulkLoad(bulkLoadPath, table);
        } else {
            log.info("loading failed.");
        }
    }

    public void doBulkLoad(boolean verbose, String... tables) throws IOException, InterruptedException, Exception {
        HTable[] hTables = new HTable[tables.length];
        for (int i = 0; i < tables.length; i++) {
            hTables[i] = new HTable(conf, tables[i]);
        }
        doBulkLoad(verbose, hTables);
    }
}
