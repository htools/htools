package io.github.htools.hadoop.decompress;

import io.github.htools.lib.Log;
import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.Job;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Decompress {

	private static final Log log = new Log(Job.class);

	public static void main(String[] args) throws Exception {

                Conf conf = new Conf(args, 
                                "-i inputfile -o outputfile");
                
                String in = conf.get("inputfile");    
                Path out = new Path(conf.get("outputfile"));

		log.info("Tool name: %s", log.getLoggedClass().getName());
		log.info(" - input paths: %s", in);
		log.info(" - output path: %s", out);

		Job job = new Job(conf, in, out);
                job.getConfiguration().setInt("mapreduce.task.timeout", 1800000);
                conf.setLong("mapreduce.input.fileinputformat.split.minsize", Long.MAX_VALUE);
                conf.setLong("mapreduce.input.fileinputformat.split.maxsize", Long.MAX_VALUE);
                job.setJarByClass(log.getLoggedClass());
                
		job.setInputFormatClass(NonSplitTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		NonSplitTextInputFormat.addInputPaths(job, in);
                
		job.setMapperClass(DecompressMap.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(Text.class);
                
                job.setNumReduceTasks(0);                
                TextOutputFormat.setOutputPath(job, out);

		// delete the output dir
                FileSystem.get(conf).delete(out, true);

		job.waitForCompletion(true);
	}
}
