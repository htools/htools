package io.github.repir.tools.Content;

import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

public class HDFSMove {

   public static Log log = new Log(HDFSMove.class);
   public static boolean verbose = false;

   public static void main(String args[]) {
      ArgsParser ap = new ArgsParser(args, "-v -r -i input -o output");
      String input = ap.get("input");
      String output = ap.get("output");
      if (ap.getBoolean("v")) {
         verbose = true;
      }
      if (!ap.getBoolean("r") && !input.contains("*")) {
         if (!verbose)
            HDFSDir.rename(HDFSDir.getFS(), input, output);
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
         HDFSDir sdir = new HDFSDir(new Configuration(), sourcedir);
         HDFSDir ddir = new HDFSDir(new Configuration(), destdir);
         sdir.move(ddir, sourcefile, destfile);
      }
   }
}
