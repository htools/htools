package io.github.htools.hadoop.io;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * From Cloud9
 * 
 * @author jeroen
 */
public class NullInputFormat extends InputFormat<NullWritable, NullWritable> {

	@Override
	public RecordReader<NullWritable, NullWritable> createRecordReader(InputSplit split,
			TaskAttemptContext contex) throws IOException, InterruptedException {
		return new NullRecordReader();
	}

	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
		List<InputSplit> splits = new ArrayList<InputSplit>();
		splits.add(new NullInputSplit());
		return splits;
	}

	public static class NullRecordReader extends RecordReader<NullWritable, NullWritable> {
		private boolean returnRecord = true;

		@Override
		public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {}

		@Override
		public NullWritable getCurrentKey() throws IOException, InterruptedException {
			return NullWritable.get();
		}

		@Override
		public NullWritable getCurrentValue() throws IOException, InterruptedException {
			return NullWritable.get();
		}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			if (returnRecord == true) {
				returnRecord = false;
				return true;
			}

			return returnRecord;
		}

		@Override
		public void close() throws IOException {}

		@Override
		public float getProgress() throws IOException, InterruptedException {
			return 0;
		}
	}
}