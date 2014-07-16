package io.github.repir.tools.Content;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSDir;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

public class MergeFiles {

   public static Log log = new Log(MergeFiles.class);

   public static void main(String args[]) {
      Datafile out = new Datafile(args[0]);
      ArrayList<Datafile> files = new ArrayList<Datafile>();
      for (int i = 1; i < args.length; i++) {
         files.add(new Datafile(args[i]));
      }
      FSDir.mergeFiles(out, files.iterator());
   }
}
