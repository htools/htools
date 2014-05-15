package io.github.repir.tools.Content;

import io.github.repir.tools.ByteSearch.ByteSearch;
import static io.github.repir.tools.Content.HDFSMove.verbose;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

/**
 * The Dir class represents a directory of files, and contains many methods to
 * access Files within the directory as RFile
 * <p/>
 * @author jbpvuurens
 */
public class HDFSDir extends Path implements Dir {

   public static Log log = new Log(HDFSDir.class);
   private ArrayList<HDFSDir> subdirs;
   private ArrayList<Path> files;
   private boolean dirread = false;
   public FileSystem fs;
   private Configuration conf;

   /**
    * Constructs a Dir object that uses the provided path
    * <p/>
    * @param directorypath
    */
   public HDFSDir(Configuration conf, String directorypath) {
      super(directorypath.length() == 0 ? "." : directorypath);
      this.conf = conf;
      try {
         fs = this.getFileSystem(conf);
      } catch (IOException ex) {
         log.exception(ex, "Constructor( %s, %s )", conf, directorypath);
      }
   }

   public HDFSDir(FileSystem fs, String directorypath) {
      super(directorypath);
      this.fs = fs;
   }

   public HDFSDir(HDFSDir path, String child) {
      super(path, child);
      this.conf = path.conf;
      this.fs = path.fs;
   }

   public HDFSDir(Path path, Configuration conf, String child) {
      super(path, child);
      this.conf = conf;
      try {
         this.fs = path.getFileSystem(conf);
      } catch (IOException ex) {
         log.exception(ex, "Constructor( %s, %s, %s )", path, conf, child);
      }
   }

   public ArrayList<Path> getFiles() {
      if (files == null) {
         readDir();
      }
      return files;
   }

   public ArrayList<HDFSDir> getSubDirs() {
      if (subdirs == null) {
         readDir();
      }
      return subdirs;
   }

   public static FileSystem getFS(Configuration conf) {
      try {
         return FileSystem.get(conf);
      } catch (IOException ex) {
         log.exception(ex, "getFS( %s )", conf);
      }
      return null;
   }

   public static FileSystem getFS() {
      return getFS(new Configuration());
   }

   public boolean exists() {
      return exists(fs, this);
   }

   public boolean isFile() {
      return isFile(fs, this);
   }

   public boolean isDir() {
      return isDir(fs, this);
   }

   public static boolean exists(FileSystem fs, Path path) {
      try {
         return fs.exists(path);
      } catch (IOException ex) {
         log.exception(ex, "exists( %s, %s )", fs, path);
         return false;
      }
   }

   public static boolean isFile(FileSystem fs, Path path) {
      try {
         return fs.isFile(path);
      } catch (IOException ex) {
         log.exception(ex, "isFile( %s, %s )", fs, path);
         return false;
      }
   }

   public static long age(FileSystem fs, Path path) {
      try {
         return System.currentTimeMillis() - fs.getFileStatus(path).getModificationTime();
      } catch (IOException ex) {
         log.exception(ex, "age( %s, %s )", fs, path);
         return 0;
      }
   }

   public static boolean isDir(FileSystem fs, Path path) {
      try {
         if (fs.exists(path)) {
            return fs.getFileStatus(path).isDir();
         }
      } catch (IOException ex) {
         log.exception(ex, "isDir( %s, %s )", fs, path);
      }
      return false;
   }

   public static void delete(FileSystem fs, Path path) {
      try {
         fs.delete(path, true);
      } catch (IOException ex) {
         log.exception(ex, "delete( %s, %s )", fs, path);
      }
   }

   public static void rename(FileSystem fs, Path path, Path dest) {
      try {
         fs.rename(path, dest);
      } catch (IOException ex) {
         log.exception(ex, "rename( %s, %s, %s )", fs, path, dest);
      }
   }

   public static void copy(FileSystem fs, Path path, Path dest) {
      OutputStream out = null;
      try {
         InputStream in = fs.open(path);
         out = fs.create(dest);
         IOUtils.copyBytes(in, out, 4096, false);
         in.close();
         out.close();
      } catch (IOException ex) {
         log.exception(ex, "copy %s %s", path, dest);
      } finally {
         try {
            out.close();
         } catch (IOException ex) {
            log.exception(ex, "copy %s %s", path, dest);
         }
      }
   }

   public static void rename(FileSystem fs, String source, String dest) {
      rename(fs, new Path(source), new Path(dest));
   }

   public static void copy(FileSystem fs, String source, String dest) {
      copy(fs, new Path(source), new Path(dest));
   }

   public static void delete(FileSystem fs, String filename) {
      delete(fs, new Path(filename));
   }

   /**
    * Constructs a new subdir, based on this path and the name of the subdir.
    * The subdir is however not yet created. This can be done by calling
    * mkdirs() on the returned Dir object.
    * <p/>
    * @param subdir
    * @return
    */
   @Override
   public HDFSDir getSubdir(String subdir) {
      return new HDFSDir(this, subdir);
   }

   @Override
   public boolean mkdirs() {
      try {
         fs.mkdirs(this);
         return true;
      } catch (IOException ex) {
         log.exception(ex, "mkdirs() fs %s", fs);
      }
      return false;
   }

   /**
    * Construct a filename based on the path of this dir and the filename given
    * <p/>
    * @param filename
    * @return
    */
   @Override
   public String getFilename(String filename) {
      return this.getCanonicalPath() + "/" + filename;
   }

   @Override
   public Datafile getFile(String filename) {
      return new Datafile(getFS(), getFilename(filename));
   }

   @Override
   public ArrayList<Datafile> matchDatafiles(String regex) {
      ArrayList<Datafile> matches = new ArrayList<Datafile>();
      for (String file : matchFiles(regex)) {
         matches.add(this.getFile(file));
      }
      return matches;
   }

   public ArrayList<String> matchFiles(String regex) {
      if (!dirread) {
         readDir();
      }
      ArrayList<String> matches = new ArrayList<String>();
      Pattern pattern = Pattern.compile(regex);
      for (Path file : files) {
         Matcher matcher = pattern.matcher(file.getName());
         if (matcher.matches()) {
            matches.add(file.getName());
         }
      }
      return matches;
   }

   public ArrayList<Datafile> matchDatafiles(ByteSearch regex) {
      ArrayList<Datafile> matches = new ArrayList<Datafile>();
      for (String file : matchFiles(regex)) {
         matches.add(this.getFile(file));
      }
      return matches;
   }

   public ArrayList<String> matchFiles(ByteSearch regex) {
      if (!dirread) {
         readDir();
      }
      ArrayList<String> matches = new ArrayList<String>();
      for (Path file : files) {
         //log.info("matchDatafiles %s %s %b", file.toString(), regex, file.getName().matches(regex));
         if (regex.exists(file.getName())) {
            matches.add(file.toString());
         }
      }
      return matches;
   }

   public String getSubDirOf(HDFSDir dir) {
      String parent = dir.getCanonicalPath();
      String current = this.getCanonicalPath();
      if (current.length() > parent.length() && current.startsWith(parent)) {
         return current.substring(parent.length() + 1);
      }
      return "";
   }

   public String getCanonicalPath() {
      StringBuilder sb = new StringBuilder();
      try {
         FileStatus filestatus = fs.getFileStatus(this);
         Path p = filestatus.getPath();
         while (p != null) {
            String name = p.getName();
            if (name.length() > 0) {
               sb.insert(0, p.getName());
               sb.insert(0, "/");
            }
            p = p.getParent();
         }
      } catch (IOException ex) {
         log.exception(ex, "getCanonicalPath() fs %s", fs);
      }
      return sb.toString();
   }

   private void readDir() {
      try {
         files = new ArrayList<Path>();
         subdirs = new ArrayList<HDFSDir>();
         if (fs.getFileStatus(this).isDir()) {
            FileStatus children[] = fs.listStatus(this);
            for (FileStatus child : children) {
               if (child.isDir()) {
                  subdirs.add(new HDFSDir(this.fs, child.getPath().toString()));
               } else {
                  files.add(child.getPath());
               }
            }
            dirread = true;
         }
      } catch (IOException ex) {
         log.exception(ex, "readDir() fs %s subdirs %s files %s dirread %b", fs, subdirs, files, dirread);
      }
   }

   public TreeSet<Datafile> fileSelection(String filestart) {
      readDir();
      TreeSet<Datafile> sortedfiles = new TreeSet<Datafile>();
      for (Path file : files) {
         if (file.getName().startsWith(filestart) && file.getName().length() > filestart.length()) {
            sortedfiles.add(new Datafile(fs, file.toString()));
         }
      }
      return sortedfiles;
   }

   public static long[] mergeFiles(Datafile out, TreeSet<Datafile> sortedfiles) {
      OutputStream o = out.getOutputStream();
      long offsets[] = new long[sortedfiles.size()];
      long offset = 0;
      int offsetpos = 0;
      for (Datafile in : sortedfiles) {
         try {
            offsets[offsetpos++] = offset;
            offset += in.getLength();
            IOUtils.copyBytes(in.getInputStream(), o, 4096, false);
            in.close();
         } catch (IOException ex) {
            log.exception(ex, "mergeFiles( %s %s )", out, sortedfiles);
         } finally {
            if (in != null) {
               in.close();
            }
         }
      }
      out.close();
      return offsets;
   }

   public long[] mergeFiles(Datafile out, String filestart) {
      //log.info("out %s dir %s filestart %s", out.getFullPath(), this.getCanonicalPath(), filestart);
      TreeSet<Datafile> sortedfiles = fileSelection(filestart);
      return mergeFiles(out, sortedfiles);
   }

   public void move(HDFSDir ddir, String sourcefile, String destfile) {
      String pattern = sourcefile.replaceAll("\\.", "\\\\.").replaceAll("[\\*]", "(.*)");
      Pattern p = Pattern.compile(pattern);
      String destfilecomponents[] = destfile.split("[\\*]");
      for (Path f : getFiles()) {
         String file = f.getName();
         Matcher m = p.matcher(file);
         int component = 0;
         if (m.matches() && m.start() == 0) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < m.groupCount(); c++) {
               sb.append(destfilecomponents[c]).append(m.group(c + 1));
            }
            for (int c = m.groupCount(); c < destfilecomponents.length; c++) {
               sb.append(destfilecomponents[c]);
            }
            log.printf("%s -> %s", getFilename(file), ddir.getFilename(sb.toString()));
            if (!verbose) {
               HDFSDir.rename(fs, getFilename(file), ddir.getFilename(sb.toString()));
            }
         }
      }
   }

   public static String[] getLocations(FileSystem fs, String filename, long offset) {
      String hosts[] = new String[0];
      try {
         if (fs != null) {
            FileStatus file = fs.getFileStatus(new Path(filename));
            BlockLocation[] blkLocations = fs.getFileBlockLocations(file, offset, 0);
            if (blkLocations.length > 0) {
               hosts = blkLocations[0].getHosts();
            }
         }
      } catch (IOException ex) {
         log.exception(ex, "getLocations( %s, %s, %d )", fs, filename, offset);
      }
      return hosts;
   }

   
   public void move(HDFSDir ddir) {
      try {
         fs.rename(this, ddir);
      } catch (IOException ex) {
         log.exception(ex, "move %s to %s", this, ddir);
      }
   }
}
