/*
 */
package io.github.htools.io;

import io.github.htools.search.ByteSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author jeroen
 */
public interface HPath extends DirComponent, Iterable<DirComponent> {

   String getCanonicalPath();

   /**
    * Construct a filename based on the path of this dir and the filename given
    * <p>
    * @param filename
    * @return
    */
   String getFilename(String filename);

   String getName();

   Datafile getFile(String filename);

   /**
    * Constructs a new subdir, based on this path and the name of the subdir.
    * The subdir is however not yet created. This can be done by calling
    * mkdirs() on the returned Dir object.
    * <p>
    * @param subdir
    * @return
    */
   HPath getSubdir(String subdir);

   HPath getParentPath();

   public ArrayList<Datafile> getFiles(ByteSearch pattern);
   
   boolean mkdirs();

   @Override
   boolean existsDir();
   
   @Override
   boolean existsFile();
   
   boolean exists();
   
   void remove() throws IOException;
   
    public Iterator<DirComponent> iterator();

    public Iterator<DirComponent> iteratorRecursive();

    public Iterator<HPath> iteratorDirs();

    public Iterator<Datafile> iteratorFiles();

    public Iterator<HPath> iteratorDirs(ByteSearch pattern);

    public Iterator<Datafile> iteratorFiles(ByteSearch pattern);
    
    public Iterator<DirComponent> iterator(ByteSearch pattern);

    public Iterator<DirComponent> wildcardIterator();

    public ArrayList<DirComponent> get();

    public ArrayList<DirComponent> getRecursive();

    public ArrayList<DirComponent> get(ByteSearch pattern);

    public ArrayList<Datafile> getFiles();

    public ArrayList<Datafile> getFilesNewerThan(long lastupdate);

    public ArrayList<String> getFilenames();

    public ArrayList<String> getFilepathnames();

    public ArrayList<? extends HPath> getDirs();

    public ArrayList<? extends HPath> getDirs(ByteSearch pattern);
    
    public ArrayList<String> getDirnames();

}
