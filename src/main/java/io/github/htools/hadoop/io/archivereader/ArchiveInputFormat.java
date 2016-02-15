package io.github.htools.hadoop.io.archivereader;

import io.github.htools.collection.ArrayMap;
import io.github.htools.extract.Content;
import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.FileFilter;
import io.github.htools.hadoop.io.FileInputFormat;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.regex.Pattern;

import static io.github.htools.lib.ClassTools.*;

/**
 * ReaderInputFormat extends FileInputFormat to supply Hadoop with the input to
 * process. To use ReaderInputFormat, instantiate with {@link #ReaderInputFormat(io.github.htools.hadoop.Job)
 * }
 * using an array of paths on the HDFS, that contain the input files to process.
 * The paths can be files or directories, which are scanned recursively for any
 * file.
 * <p>
 * The input is configured by "repository.inputdir", which can be a comma
 * seperated list of folders, or an array, e.g. multiple
 * "+repository.inputdir=...". The dirs are scanned recursively for input files.
 * See {@link FileFilter} if certain files can be included or excluded.
 * <p>
 * By default, valid files are submitted to an instantiation of the configured
 * "repository.entityreader". Alternatively, different entityreaders can be
 * configured for different file types, by assigning an entity reader for files
 * that end with some extension, e.g. "+repository.assignentityreader=.pdf
 * EntitReaderPDF"
 * <p>
 * !!Note that Java does not have a way to uncompress .z files, so the .z files
 * on the original TREC disks have to be uncompressed outside this framework.
 * <p>
 * @author jeroen
 */
public class ArchiveInputFormat extends FileInputFormat<LongWritable, Content> {

    public static Log log = new Log(ArchiveInputFormat.class);
    public static final String READERCLASS = ArchiveInputFormat.class.getCanonicalName().toLowerCase() + ".readerclass";
    public static final String READERREGEX = ArchiveInputFormat.class.getCanonicalName().toLowerCase() + ".readerregex";
    public static final String READERREGEXCLASS = ArchiveInputFormat.class.getCanonicalName().toLowerCase() + ".readerregexclass";
    static FileFilter filefilter;
    static String defaultArchiveReader;
    static ArrayMap<Pattern, String> assignedArchiveReader = new ArrayMap();

    protected static void loadArchiveReaderSettings(Configuration conf) {
        defaultArchiveReader = conf.get(READERCLASS);
        String[] regex = conf.getStrings(READERREGEX);
        if (regex != null && regex.length > 0) {
            String[] reader = conf.getStrings(READERREGEXCLASS);
            for (int i = 0; i < regex.length; i++) {
                Pattern matcher = Pattern.compile(regex[i]);
                assignedArchiveReader.add(matcher, reader[i]);
            }
        }
    }

    public static void setDefaultArchiveReader(Job job, Class archiveReaderClass) {
        job.getConfiguration().set(READERCLASS, archiveReaderClass.getCanonicalName());
    }

    public static void setFileSpecificReader(Configuration conf, String regex, String readerclass) {
        Conf.addToStringList(conf, READERREGEX, regex);
        Conf.addToStringList(conf, READERREGEXCLASS, readerclass);
    }

    protected static String getArchiveReaderName(InputSplit is, Configuration conf) {
        if (defaultArchiveReader == null) {
            loadArchiveReaderSettings(conf);
        }
        if (assignedArchiveReader.size() > 0) {
            String file = ((FileSplit) is).getPath().getName();
            for (Map.Entry<Pattern, String> entry : assignedArchiveReader) {
                if (entry.getKey().matcher(file).find()) {
                    return entry.getValue();
                }
            }
        }
        return defaultArchiveReader;
    }

    @Override
    public RecordReader<LongWritable, Content> createRecordReader(InputSplit is, TaskAttemptContext tac) {
        Class clazz = toClass(getArchiveReaderName(is, tac.getConfiguration()), ArchiveReader.class.getPackage().getName());
        Constructor c;
        try {
            c = getAssignableConstructor(clazz, ArchiveReader.class);
            return (RecordReader<LongWritable, Content>) construct(c);
        } catch (ClassNotFoundException ex) {
            log.fatalexception(ex, "createRecordReader(%s)", ((FileSplit) is).getPath().toString());
        }
        return null;
    }
}
