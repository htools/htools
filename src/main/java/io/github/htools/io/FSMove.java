package io.github.htools.io;

import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;

public class FSMove {

   public static Log log = new Log(FSMove.class);
   public static boolean verbose = false;

   public static void main(String... args) {
      if (args[0].equals("-v")) {
         verbose = true;
         args = (String[])ArrayTools.subArray(args, 1);
         log.info("%d", args.length);
      }
      String sourcedir, sourcefile, destdir, destfile;
      if (args[0].contains("/")) {
         sourcedir = args[0].substring(0, args[0].lastIndexOf('/'));
         sourcefile = args[0].substring(args[0].lastIndexOf('/') + 1);
      } else {
         sourcedir = ".";
         sourcefile = args[0];
      }
      if (args[1].contains("/")) {
         destdir = args[1].substring(0, args[1].lastIndexOf('/'));
         destfile = args[1].substring(args[1].lastIndexOf('/') + 1);
      } else {
         destdir = ".";
         destfile = args[1];
      }
      FSPath sdir = new FSPath(sourcedir);
      FSPath ddir = new FSPath(destdir);
      sdir.move(ddir, sourcefile, destfile, verbose);
   }
}
