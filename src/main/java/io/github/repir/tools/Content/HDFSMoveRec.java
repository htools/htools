package io.github.repir.tools.Content;

import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

public class HDFSMoveRec {

   public static Log log = new Log(HDFSMoveRec.class);

   public static void main(String... args) {
      ArgsParser ap = new ArgsParser(args, "-i input -o output");
      String input = ap.get("input");
      String output = ap.get("output");
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
      move(sdir, ddir, sourcefile, destfile);
   }

   public static void move(HDFSDir sdir, HDFSDir ddir, String sourcefile, String destfile) {
      sdir.move( ddir, sourcefile, destfile);
      for (HDFSDir newsdir : sdir.getSubDirs()) {
         String subdir = newsdir.getSubDirOf(sdir);
         HDFSDir newddir = ddir.getSubdir(subdir);
         //log.info( "%s %s %s %s", sdir.getCanonicalPath(), newsdir.getCanonicalPath(), subdir, ddir.getCanonicalPath());
         move(newsdir, newddir, sourcefile, destfile);
      }
   }
}
