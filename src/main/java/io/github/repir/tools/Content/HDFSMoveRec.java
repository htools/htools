package io.github.repir.tools.Content;

import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.Log;
import org.apache.hadoop.conf.Configuration;

public class HDFSMoveRec {

   public static Log log = new Log(HDFSMoveRec.class);

   public static void main(String... args) {
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
