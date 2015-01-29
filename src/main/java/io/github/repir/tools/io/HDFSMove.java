package io.github.repir.tools.io;

import io.github.repir.tools.lib.ArgsParser;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

public class HDFSMove {

   public static Log log = new Log(HDFSMove.class);
   public static boolean verbose = false;

   public static void main(String args[]) throws IOException {
      ArgsParser ap = new ArgsParser(args, "-v -r -i input -o output");
      String input = ap.get("input");
      String output = ap.get("output");
      if (ap.getBoolean("v")) {
         verbose = true;
      }
      if (!ap.getBoolean("r") && !input.contains("*")) {
         if (!verbose)
            HDFSPath.rename(HDFSPath.getFS(), input, output);
      } else if (ap.getBoolean("r")) {
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
