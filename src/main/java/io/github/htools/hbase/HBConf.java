package io.github.htools.hbase;

import io.github.htools.hadoop.Conf;
import io.github.htools.lib.Log;
import org.apache.hadoop.hbase.HBaseConfiguration;

/**
 * Extension of Hadoop's Conf that can read/write configurations from flat text
 * files, or from command line args.
 * <p>
 * This variant adds the resources for HBase to the configuration, to use instead
 * of Conf when HBase is used.
 *
 * @author Jeroen Vuurens
 */
public class HBConf extends Conf {

    public static Log log = new Log(HBConf.class);

    public HBConf(String args[], String template) {
        this();
        parseArgs(args, template);
    }

    public HBConf() {
        super();
        HBaseConfiguration.addHbaseResources(this);
    }

    protected HBConf(org.apache.hadoop.conf.Configuration other) {
        super(other);
        HBaseConfiguration.addHbaseResources(this);
    }
}
