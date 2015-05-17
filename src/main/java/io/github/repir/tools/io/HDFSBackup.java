package io.github.repir.tools.io;

import io.github.repir.tools.hadoop.Conf;
import io.github.repir.tools.lib.Log;
import java.io.IOException;

public class HDFSBackup {

   public static Log log = new Log(HDFSBackup.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException {
      Conf conf = new Conf(args, "-i input -o output");
      String input = conf.get("input");
      String output = conf.get("output");
      HDFSPath.backup(new HDFSPath(conf, input), new HDFSPath(conf, output));
   }
}
