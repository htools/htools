/*
 */
package io.github.htools.io;

import io.github.htools.collection.ListIterator;
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

   public ArrayList<Datafile> getFiles(String regex) throws IOException;
   
   boolean mkdirs();

   @Override
   boolean existsDir();
   
   @Override
   boolean existsFile();
   
   boolean exists();
   
   void remove() throws IOException;
   
    public Iterator<DirComponent> iterator();

    public Iterator<DirComponent> iteratorRecursive() throws IOException;

    public Iterator<DirComponent> iterator(String regexstring) throws IOException;

    public Iterator<DirComponent> iteratorDirs() throws IOException;

    public Iterator<DirComponent> iteratorFiles() throws IOException;

    public Iterator<DirComponent> iteratorDirs(String regexstring) throws IOException;

    public Iterator<DirComponent> iteratorFiles(String regexstring) throws IOException;

    public Iterator<DirComponent> iteratorDirs(ByteSearch regexstring) throws IOException;
    
    public Iterator<DirComponent> iterator(ByteSearch regex) throws IOException;

    public Iterator<DirComponent> wildcardIterator() throws IOException;

    public ArrayList<DirComponent> get() throws IOException;

    public ArrayList<DirComponent> getRecursive() throws IOException;

    public ArrayList<DirComponent> get(String regexstring) throws IOException;

    public ArrayList<Datafile> getFiles() throws IOException;

    public ArrayList<Datafile> getFilesNewerThan(long lastupdate) throws IOException;

    public ArrayList<String> getFilenames() throws IOException;

    public ArrayList<String> getFilepathnames() throws IOException;

    public ArrayList<? extends HPath> getDirs() throws IOException;

    public ArrayList<? extends HPath> getDirs(ByteSearch pattern) throws IOException;
    
    public ArrayList<String> getDirnames() throws IOException;

    public ArrayList<? extends HPath> getDirs(String regexstring) throws IOException;
    
    public ArrayList<Datafile> getFilesStartingWith(String start) throws IOException;


}
