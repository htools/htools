package io.github.repir.tools.hadoop;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.io.Path;
import io.github.repir.tools.lib.Log;
import static io.github.repir.tools.lib.PrintTools.sprintf;
import io.github.repir.tools.io.struct.StructuredTextTSV;
import io.github.repir.tools.hadoop.io.OutputFormat;
import org.apache.hadoop.conf.Configuration;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;

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
        super(getDatafile(outputfile));
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

    public static Datafile getDatafile(Datafile df) {
        Path dir = df.getDir();
        df = dir.getFile(sprintf("_log/%s", df.getFilename()));
        return df;
    }

    @Override
    public void write() {
        super.write();
        this.datafile.flush();
    }
}
