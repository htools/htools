package io.github.htools.hadoop;

import io.github.htools.hadoop.io.OutputFormat;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.io.HPath;
import io.github.htools.io.struct.StructuredTextTSV;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;

import static io.github.htools.lib.PrintTools.sprintf;

/**
 *
 * @author jeroen
 */
public abstract class LogFileAbstract extends StructuredTextTSV {

    public static final Log log = new Log(LogFileAbstract.class);
    public static final String LABEL = "logfile.outputpath";

    public LogFileAbstract(TaskAttemptContext context) {
        super(getDatafile(context));
    }

    public LogFileAbstract(Datafile outputfile) {
        super(outputfile);
    }

    public static void setOutputPath(Configuration conf, String outpath) {
        conf.set(LABEL, outpath);
    }

    public static String getOutputPath(Configuration conf) {
        return conf.get(LABEL);
    }

    public static Datafile getDatafile(TaskAttemptContext context) {
        Configuration conf = context.getConfiguration();
        Datafile df = null;
        String logdir = conf.get(LABEL);
        if (logdir == null) {
            HDFSPath path = OutputFormat.getLogDir(context);
            df = path.getFile(getLogFilename(context));
        } else {
            df = new HDFSPath(conf, logdir).getFile(getLogFilename(context));
        }
        return df;
    }

    public static String getLogFilename(TaskAttemptContext context) {
        TaskAttemptID taskAttemptID = context.getTaskAttemptID();
        TaskType taskType = taskAttemptID.getTaskType();
        int task = ContextTools.getTaskID(context);
        int attempt = ContextTools.getAttemptID(context);

        if (taskType == TaskType.MAP) {
            return sprintf("map.%05d", task);
        } else if (taskType == TaskType.REDUCE) {
            return sprintf("reduce.%05d", task);
        } else {
            return "other";
        }
    }

    @Override
    public void write() {
        super.write();
        this.datafile.flush();
    }
}
