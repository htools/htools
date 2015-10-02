package io.github.htools.hadoop.io.archivereader; 

import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.io.HDFSPath;
import io.github.htools.io.HDFSIn;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.hadoop.Conf;
import java.io.IOException;
import java.util.Map;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 *
 * @author Jeroen Vuurens
 */
public class ReaderTest {
  public static Log log = new Log( ReaderTest.class ); 

   public static void main(String[] args) throws IOException, InterruptedException {
      Conf conf = new Conf(args, "source");
      Path p = new Path(conf.get("source"));
      long length = HDFSIn.getLength(Conf.getFileSystem(conf), p);
      String[] locations = HDFSPath.getLocations(Conf.getFileSystem(conf), conf.get("source"), 0);
      FileSplit fs = new FileSplit(p, 0, length, locations);
      ArchiveReader er = new ReaderTREC();
      ExtractorConf extractor = new ExtractorConf(conf);
      er.initialize(fs, conf);
      er.nextKeyValue();
      Content ew = er.getCurrentValue();
      extractor.process(ew);
      for (Map.Entry<String, ExtractChannel> c : ew.entrySet()) {
         log.printf("%s %s\n", c.getKey(), c.getValue().getContentStr());
      }
   }

}
