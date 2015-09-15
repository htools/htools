package io.github.htools.io;

import io.github.htools.hadoop.Conf;
import io.github.htools.lib.Log;
import java.io.IOException;

public class HDFSRestore {

   public static Log log = new Log(HDFSRestore.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException {
      Conf conf = new Conf(args, "-i input -o output");
      HDFSPath.restore(conf.getHDFSPath("input"), conf.getHDFSPath("output"));
   }
}
