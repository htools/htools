package io.github.htools.io;

import io.github.htools.hadoop.Conf;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

public class HDFSMove {

   public static Log log = new Log(HDFSMove.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException  {
      Conf conf = new Conf(args, "--v --r -i input -o output");
      String input = conf.get("input");
      String output = conf.get("output");
      if (conf.getBoolean("v", false)) {
         verbose = true;
      }
      if (!conf.getBoolean("r", false) && !input.contains("*")) {
         if (!verbose)
            HDFSPath.rename(Conf.getFileSystem(conf), input, output);
      } else if (conf.getBoolean("r", false)) {
         if (!verbose)
            HDFSMoveRec.main(input, output);
      } else {
         String sourcedir, sourcefile, destdir, destfile;
         if (input.contains("/")) {
            sourcedir = input.substring(0, input.lastIndexOf('/'));
            sourcefile = input.substring(input.lastIndexOf('/') + 1);
         } else {
            sourcedir = "";
            sourcefile = input;
         }
         if (output.contains("/")) {
            destdir = output.substring(0, output.lastIndexOf('/'));
            destfile = output.substring(output.lastIndexOf('/') + 1);
         } else {
            destdir = "";
            destfile = output;
         }
         HDFSPath sdir = new HDFSPath(conf, sourcedir);
         HDFSPath ddir = new HDFSPath(conf, destdir);
         sdir.move(ddir, sourcefile, destfile);
      }
   }
}
