package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.io.struct.StructuredRecordFile;
import io.github.repir.tools.hadoop.ContextTools;
import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.hadoop.InputFormat;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Supports creating a single output file per top input folder.
 * @author jeroen
 */
public abstract class OutputFormatFolder<F extends StructuredRecordFile, V extends Writable> extends OutputFormat<F, V> {

    public static Log log = new Log(OutputFormatFolder.class);

    public OutputFormatFolder(Job job, Class fileclass, Class writableclass) {
        super(job, fileclass, writableclass);
    }

    public OutputFormatFolder(Class fileclass, Class writableclass) {
        super(fileclass, writableclass);
    }

    @Override
    public Datafile getDatafile(TaskAttemptContext context, String folder) throws IOException {
        ArrayList<String> topDirs = InputFormat.topDirs(context.getConfiguration());
        int reducerID = ContextTools.getTaskID(context);
        String inputdir = topDirs.get(reducerID);
        String lastComponent = inputdir.substring(inputdir.lastIndexOf("/") + 1);
        HDFSPath dir = new HDFSPath(context.getConfiguration(), folder);
        return dir.getFile(lastComponent);
    }
}
