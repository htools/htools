package io.github.htools.io;

import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;

public class HDFSMoveRec {

   public static Log log = new Log(HDFSMoveRec.class);

   public static void main(String... args) throws IOException {
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
      HDFSPath sdir = new HDFSPath(new Configuration(), sourcedir);
      HDFSPath ddir = new HDFSPath(new Configuration(), destdir);
      move(sdir, ddir, sourcefile, destfile);
   }

   public static void move(HDFSPath sdir, HDFSPath ddir, String sourcefile, String destfile) throws IOException {
      sdir.move( ddir, sourcefile, destfile);
      for (HDFSPath newsdir : sdir.getDirs()) {
         String subdir = newsdir.getSubDirOf(sdir);
         HDFSPath newddir = ddir.getSubdir(subdir);
         //log.info( "%s %s %s %s", sdir.getCanonicalPath(), newsdir.getCanonicalPath(), subdir, ddir.getCanonicalPath());
         move(newsdir, newddir, sourcefile, destfile);
      }
   }
}
