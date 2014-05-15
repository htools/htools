/*
 */
package io.github.repir.tools.Content;

import java.util.ArrayList;

/**
 *
 * @author jeroen
 */
public interface Dir {

   String getCanonicalPath();

   /**
    * Construct a filename based on the path of this dir and the filename given
    * <p/>
    * @param filename
    * @return
    */
   String getFilename(String filename);

   Datafile getFile(String filename);

   /**
    * Constructs a new subdir, based on this path and the name of the subdir.
    * The subdir is however not yet created. This can be done by calling
    * mkdirs() on the returned Dir object.
    * <p/>
    * @param subdir
    * @return
    */
   Dir getSubdir(String subdir);

   public ArrayList<Datafile> matchDatafiles(String regex);
   
   boolean mkdirs();

   boolean exists();
}
