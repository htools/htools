package io.github.repir.tools.Content;

import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

public class HDFSMove {

   public static Log log = new Log(HDFSMove.class);
   public static boolean verbose = false;

   public static void main(String... args) {
      if (args[0].equals("-v")) {
         verbose = true;
         args = (String[])ArrayTools.subArray(args, 1);
      }
      if (!args[0].equals("-r") && !args[0].contains("*")) {
         if (!verbose)
            HDFSDir.rename(HDFSDir.getFS(), args[0], args[1]);
      } else if (args[0].equals("-r")) {
         if (!verbose)
            HDFSMoveRec.main(args[1], args[2]);
      } else {
         String sourcedir, sourcefile, destdir, destfile;
         if (args[0].contains("/")) {
            sourcedir = args[0].substring(0, args[0].lastIndexOf('/'));
            sourcefile = args[0].substring(args[0].lastIndexOf('/') + 1);
         } else {
            sourcedir = "";
            sourcefile = args[0];
         }
         if (args[1].contains("/")) {
            destdir = args[1].substring(0, args[1].lastIndexOf('/'));
            destfile = args[1].substring(args[1].lastIndexOf('/') + 1);
         } else {
            destdir = "";
            destfile = args[1];
         }
         HDFSDir sdir = new HDFSDir(new Configuration(), sourcedir);
         HDFSDir ddir = new HDFSDir(new Configuration(), destdir);
         sdir.move(ddir, sourcefile, destfile);
      }
   }
}
