package io.github.htools.io;

import io.github.htools.lib.Log;
import java.util.ArrayList;

public class MergeFiles {

   public static Log log = new Log(MergeFiles.class);

   public static void main(String args[]) {
      Datafile out = new Datafile(args[0]);
      ArrayList<Datafile> files = new ArrayList<Datafile>();
      for (int i = 1; i < args.length; i++) {
         files.add(new Datafile(args[i]));
      }
      FSPath.mergeFiles(out, files.iterator());
   }
}
