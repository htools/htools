package io.github.repir.tools.Content;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Lib.Log;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

/**
 * The Dir class represents a directory of files, and contains many methods to
 * access Files within the directory as FSFile
 * <p/>
 * @author jbpvuurens
 */
public class FSDir extends File implements Dir {

   public static Log log = new Log(FSDir.class);
   private ArrayList<FSDir> subdirs;
   private ArrayList<FSFile> files;
   private boolean dirread = false;

   /**
    * Constructs a Dir object that uses the provided path
    * <p/>
    * @param directorypath
    */
   public FSDir(String directorypath) {
      super(directorypath);
   }

   /**
    * Reads the directory contents, that can be accessed through subdirs and
    * files. However, the subdirs are not yet read. You can read the whole Dir
    * tree by using {@link Dir#readDirRecursive() }
    */
   private void readDir() {
      files = new ArrayList<FSFile>();
      subdirs = new ArrayList<FSDir>();
      if (this.isDirectory()) {
         String children[] = this.list();
         for (String child : children) {
            File d = new File(this.getFilename(child));
            if (d.isDirectory()) {
               subdirs.add(new FSDir(this.getFilename(child)));
            } else {
               files.add(new FSFile(this.getFilename(child)));
            }
         }
         dirread = true;
      }
   }

   public ArrayList<FSFile> getFiles() {
      if (files == null) {
         readDir();
      }
      return files;
   }
   
   public ArrayList<FSDir> getDirs() {
      if (subdirs == null) {
         readDir();
      }
      return subdirs;
   }
   
   @Override
   public boolean exists() {
      return (super.exists() && super.isDirectory());
   }

   public boolean existsFile(String filename) {
      File f = new File(this.getFilename(filename));
      return (f.exists() && f.isFile());
   }

   public static boolean exists(String filename) {
      File f = new File(filename);
      return f.exists();
   }

   public static boolean isDir(String filename) {
      File f = new File(filename);
      return f.exists() && f.isDirectory();
   }

   /**
    * Returns an ArrayList with all Datafiles in the Dir whose name exists the
    * regular expression.
    * <p/>
    * @param regex
    * @return
    */
   public ArrayList<Datafile> matchDatafiles(String regex) {
      if (!dirread) {
         readDir();
      }
      ArrayList<Datafile> matches = new ArrayList<Datafile>();
      for (FSFile file : files) {
         if (file.getFilename().matches(regex)) {
            matches.add(new Datafile(file.getFullPathName()));
         }
      }
      return matches;
   }

   /**
    * @return an ArrayList of FSFile in the Dir
    */
   public ArrayList<FSFile> readFiles() {
      if (!dirread) {
         readDir();
      }
      return files;
   }

   /**
    * Returns an ArrayList with all Datafiles in the Dir whose name exists the
    * regular expression.
    * <p/>
    * @param regex
    * @return
    */
   public ArrayList<FSFile> matchFiles(String regex) {
      if (!dirread) {
         readDir();
      }
      ByteRegex bregex = new ByteRegex(regex);
      ArrayList<FSFile> matches = new ArrayList<FSFile>();
      for (FSFile file : files) {
         if (bregex.exists(file.getFilename())) {
            matches.add(new FSFile(file.getFullPathName()));
         }
      }
      return matches;
   }

   @Override
   public String getCanonicalPath() {
      try {
         return super.getCanonicalPath();
      } catch (IOException ex) {
         log.fatalexception(ex, "getCanonicalPath()");
      }
      return null;
   }

   /**
    * Constructs a new subdir, based on this path and the name of the subdir.
    * The subdir is however not yet created. This can be done by calling
    * mkdirs() on the returned Dir object.
    * <p/>
    * @param subdir
    * @return
    */
   public FSDir getSubdir(String subdir) {
      return new FSDir(this.getCanonicalPath() + "/" + subdir);
   }

   @Override
   public boolean mkdirs() {
      //log.info("mkdirs %s %b", this.getCanonicalPath(), this.isDirectory());
      boolean succes;
      if (this.isDirectory()) {
         return true;
      }
      if (!super.mkdirs()) {
         log.warn("JDir: problem creating directory %s\n", this.getPath());
         return false;
      }
      return true;
   }

   /**
    * reads the whole Dir tree recursively. This can be startTime consuming on deep
    * directory trees. If the contents of subdirs is not needed, {@link Dir#readDir()
    * } can be used instead.
    */
   public void readDirRecursive() {
      if (this.isDirectory()) {
         String children[] = this.list();
         for (String child : children) {
            File d = new File(this.getFilename(child));
            if (d.isDirectory()) {
               FSDir jd = new FSDir(this.getFilename(child));
               subdirs.add(jd);
               jd.readDirRecursive();
            } else {
               files.add(new FSFile(this.getFilename(child)));
            }
         }
      }
   }

   /**
    * returns an ArrayList with all files in the directory. The FSFile objects
    * contain full paths, but directories are excluded.
    * <p/>
    * @param list
    */
   protected ArrayList<FSFile> readFilesRecursive() {
      ArrayList<FSFile> files = new ArrayList<FSFile>();
      ArrayList<FSDir> dirs = new ArrayList<FSDir>();
      dirs.add(this);
      for (int d = 0; d < dirs.size(); d++) {
         if (this.isDirectory()) {
            String children[] = this.list();
            for (String child : children) {
               File f = new File(this.getFilename(child));
               if (f.isDirectory()) {
                  dirs.add(new FSDir(this.getFilename(child)));
               } else {
                  files.add(new FSFile(this.getFilename(child)));
               }
            }
         }
      }
      return files;
   }

   /**
    * Construct a filename based on the path of this dir and the filename given
    * <p/>
    * @param filename
    * @return
    */
   public String getFilename(String filename) {
      return this.getCanonicalPath() + "/" + filename;
   }

   @Override
   public Datafile getFile(String filename) {
      return new Datafile(getFilename(filename));
   }

   /**
    * Construct a FSFile based on the path of this dir and the filename given
    * <p/>
    * @param filename
    * @return
    */
   public FSFile getRFile(String filename) {
      return new FSFile(getFilename(filename));
   }

   public FSFileOutBuffer getDataFileOut(String filename) {
      return new FSFileOutBuffer(getFilename(filename));
   }

   public TreeSet<Datafile> fileSelection(String filestart) {
      readDir();
      TreeSet<Datafile> sortedfiles = new TreeSet<Datafile>();
      for (FSFile file : files) {
         if (file.getFilename().startsWith(filestart) && file.getFilename().length() > filestart.length()) {
            sortedfiles.add(new Datafile(file.toString()));
         }
      }
      return sortedfiles;
   }

   public static void mergeFiles(Datafile out, Iterator<Datafile> files) {
      OutputStream o = out.getOutputStream();
      while (files.hasNext()) {
         Datafile df = files.next();
         //log.info("file %s", df.getFullPath());
         try {
            IOUtils.copyBytes(df.getInputStream(), o, 4096, false);
         } catch (IOException ex) {
            log.exception(ex, "mergeFiles( %s, %s ) when merging files", out, files);
         } finally {
            df.close();
         }
      }
      out.close();
   }
   
   public static boolean rename(String from, String to) {
       return new File(from).renameTo(new File(to));
   }
   
   public void move(FSDir ddir, String sourcefile, String destfile, boolean verbose) {
      String pattern = sourcefile.replaceAll("\\.", "\\\\.").replaceAll("[\\*]", "(.*)");
      Pattern p = Pattern.compile(pattern);
      String destfilecomponents[] = (destfile + " ").split("[\\*]");
      for (FSFile f : getFiles()) {
         String file = f.getFilename();
         Matcher m = p.matcher(file);
         int component = 0;
         if (m.matches() && m.start() == 0) {
            StringBuilder sb = new StringBuilder();
            if (destfilecomponents.length > 1) {
               for (int c = 0; c < m.groupCount(); c++) {
                  sb.append(destfilecomponents[c]).append(m.group(c + 1));
               }
               for (int c = m.groupCount(); c < destfilecomponents.length; c++) {
                  sb.append(destfilecomponents[c]);
               }
            } else {
               sb.append(destfilecomponents[0]);
            }
            log.printf("'%s' -> '%s'", getFilename(file), ddir.getFilename(sb.toString().trim()));
            if (!verbose) {
               new File(getFilename(file)).renameTo(new File(ddir.getFilename(sb.toString().trim())));
            }
         }
      }
   }
}
