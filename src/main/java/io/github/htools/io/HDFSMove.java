package io.github.htools.io;

import io.github.htools.hadoop.Conf;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

public class HDFSMove {

   public static Log log = new Log(HDFSMove.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException {
      Conf ap = new Conf(args, "--v --r -i input -o output");
      String input = ap.get("input");
      String output = ap.get("output");
      if (ap.getBoolean("v", false)) {
         verbose = true;
      }
      if (!ap.getBoolean("r", false) && !input.contains("*")) {
         if (!verbose)
            HDFSPath.rename(HDFSPath.getFS(), input, output);
      } else if (ap.getBoolean("r", false)) {
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
         HDFSPath sdir = new HDFSPath(new Configuration(), sourcedir);
         HDFSPath ddir = new HDFSPath(new Configuration(), destdir);
         sdir.move(ddir, sourcefile, destfile);
      }
   }
}
