package io.github.htools.hadoop.io.archivereader;

import io.github.htools.io.Datafile;
import io.github.htools.extract.Content;
import io.github.htools.fcollection.FHashSet;
import io.github.htools.fcollection.FHashSetInt;
import io.github.htools.fcollection.FHashSetLong;
import io.github.htools.hadoop.Conf;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * A document reader read an input file, identifying document markers to store
 * one document at a startTime in a BytesWritable, that is used in a map()
 * process. The LongWritable that is passed along indicates the offset in the
 * input file, which can be used to trace problems.
 * <p>
 * Note, Hadoop can split uncompressed files, to divide the work between
 * mappers. These splits are likely to place an offset inside a document. The
 * desired cause of action if that the mapper who starts reading a document that
 * encounters the InputSplit's ceiling, keeps reading past the ceiling (you can,
 * the ceiling is just an indicator). The other Mapper starts at the designated
 * offset and searches from that point until the first start of document tag.
 * This way no documents are processed twice or get lost.
 * <p>
 * EntityReader is used internally by {@link ReaderInputFormat}, to read
 * the next entity from a source archive, for processing by a Mapper as
 * {@link Content}.
 *
 * @author jeroen
 */
public abstract class ArchiveReader extends RecordReader<LongWritable, Content> {

   public static Log log = new Log(ArchiveReader.class);
   protected TaskAttemptContext context;
   protected long start;
   protected long end;
   protected Datafile fsin;
   protected LongWritable key = new LongWritable();
   protected Content entitywritable;
   protected FileSystem filesystem;
   protected Configuration conf;

   @Override
   public void initialize(InputSplit is, TaskAttemptContext tac) {
         context = tac;
         initialize( is, tac.getConfiguration() );
   }

   public final void initialize(InputSplit is, Configuration conf) {
      //log.info("initialize");
      try {
         this.conf = conf;
         filesystem = FileSystem.get(conf);
         FileSplit fileSplit = (FileSplit) is;
         Path file = fileSplit.getPath();
         start = fileSplit.getStart();
         end = start + fileSplit.getLength();
         fsin = new Datafile(filesystem, file);
         fsin.setOffset(start);
         fsin.setBufferSize(10000000);
         fsin.openRead();
         initialize(fileSplit);
      } catch (IOException ex) {
         log.exception(ex, "initialize( %s ) conf %s filesystem %s fsin %s", is, conf, filesystem, fsin);
      }
   }

   public abstract void initialize(FileSplit fileSplit);

   /**
    * Reads the input file, scanning for the next document, setting key and
    * entitywritable with the offset and byte contents of the document read.
    * <p>
    * @return true if a next document was read
    */
   @Override
   public abstract boolean nextKeyValue();

   @Override
   public LongWritable getCurrentKey() throws IOException, InterruptedException {
      return key;
   }

   @Override
   public Content getCurrentValue() throws IOException, InterruptedException {
      return entitywritable;
   }
   
   /**
    * NB this indicates progress as the data that has been read, for some
    * MapReduce tasks processing the data continues for some startTime, causing
    * the progress indicator to halt at 100%.
    * <p>
    * @return @throws IOException
    * @throws InterruptedException
    */
   @Override
   public float getProgress() throws IOException, InterruptedException {
      return (fsin.getOffset() - start) / (float) (end - start);
   }

   @Override
   public void close() throws IOException {
      fsin.close();
   }
}
