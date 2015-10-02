package io.github.htools.hbase;

import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.Job;
import io.github.htools.io.DirComponent;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.StrTools;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
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
     * writing Puts to HFiles in a non-customizable reducer, and bulk loading these
     * files into HBase tables, which is much faster than using 
     * @param verbose
     * @param tableNames
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     * @throws Exception 
     */
    public void doBulkLoad(boolean verbose, HTable ... tables) throws IOException, ClassNotFoundException, InterruptedException, Exception {
        ClassTools.preLoad(LoadIncrementalHFiles.class);
        // needed because configuration points to hdfs://hathi-surfsara causing a Wrong FS
        FileSystem fs = FileSystem.get(new URI("hdfs://head02.hathi.surfsara.nl:8020"), conf);
        
        // setup the bulkload temp folder
        HDFSPath bulkLoadPath = new HDFSPath(getConfiguration(), "/tmp/bulkload");
        if (bulkLoadPath.exists()) {
            bulkLoadPath.trash();
        }
        
        // setup the job
        BulkOutputFormat.configureIncrementalLoad(this, tables);
        BulkOutputFormat.setOutputPath(this, bulkLoadPath);
        if (waitForCompletion(verbose)) {
            HDFSPath hbaseOwned = new HDFSPath(conf, "/tmp/" + UUID.randomUUID().toString());
            // Load generated HFiles into table
            LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
            for (int i = 0; i < tables.length; i++) {
                String tableName = tables[i].getName().getNameAsString().replaceAll(":", "_");
                Path tableFolder = fs.makeQualified( new Path("/tmp/bulkload", tableName) );
                HDFSPath.rename(fs, tableFolder, hbaseOwned);
                for (DirComponent d : hbaseOwned.getRecursive()) {
                    HDFSPath.setPermissions(conf, d.getCanonicalPath(), "777");
                }
                //hbaseOwned.setOwner("hbase", "hbase");
                loader.doBulkLoad(hbaseOwned, tables[i]);
            }
        } else {
            log.info("loading failed.");
        }
    }
    
    public void doBulkLoad(boolean verbose, Table ... tables) throws IOException, InterruptedException, Exception {
        HTable[] hTables = new HTable[tables.length];
        for (int i = 0; i < tables.length; i++) {
            hTables[i] = tables[i].getHTable(conf);
        }
        doBulkLoad(verbose, hTables);
    }
    
    public void doBulkLoad(Table ... tables) throws IOException, InterruptedException, Exception {
        doBulkLoad(tables);
    }
    
    public void doBulkLoad(boolean verbose, String ... tables) throws IOException, InterruptedException, Exception {
        HTable[] hTables = new HTable[tables.length];
        for (int i = 0; i < tables.length; i++) {
            hTables[i] = Table.getHTable(conf, tables[i]);
        }
        doBulkLoad(verbose, hTables);
    }
}
