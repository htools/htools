package io.github.htools.io;

import io.github.htools.hadoop.Conf;
import io.github.htools.lib.Log;
import java.io.IOException;

public class HDFSBackup {

   public static Log log = new Log(HDFSBackup.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException {
      Conf conf = new Conf(args, "-i input -o output");
      HDFSPath.backup(conf.getHDFSPath("input"), conf.getHDFSPath("output"));
   }
}
