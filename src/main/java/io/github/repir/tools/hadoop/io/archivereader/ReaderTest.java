package io.github.repir.tools.hadoop.io.archivereader; 

import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.io.HDFSIn;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.hadoop.Conf;
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
      long length = HDFSIn.getLength(HDFSPath.getFS(conf), p);
      String[] locations = HDFSPath.getLocations(HDFSPath.getFS(conf), conf.get("source"), 0);
      FileSplit fs = new FileSplit(p, 0, length, locations);
      Reader er = new ReaderTREC();
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
