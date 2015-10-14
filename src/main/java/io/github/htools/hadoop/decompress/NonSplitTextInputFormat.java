package io.github.htools.hadoop.decompress;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
/**
 *
 * @author jeroen
 */
public class NonSplitTextInputFormat extends TextInputFormat {
   
   @Override
   protected boolean isSplitable(JobContext context, Path path) {
       return false;
   }
   
}
