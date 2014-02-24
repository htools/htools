package io.github.repir.tools.Lib;

import java.io.FileNotFoundException;
import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFile;
import io.github.repir.tools.Content.HDFSDir;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import io.github.repir.tools.DataTypes.Configuration;

/**
 *
 * @author jbpvuurens
 */
public class HDTools {

   public static Log log = new Log(HDTools.class);
   public static long lastprogress = 0;

   public static FileSystem getFS() {
      try {
         return FileSystem.get(new Configuration());
      } catch (IOException ex) {
         log.exception(ex, "getFS()");
      }
      return null;
   }

   protected static void setLogFile(FileSystem fs, String basedir) {
      Path p;
      if (Log.out == null) {
         for (int i = 0; i < 2000; i++) {
            p = new Path(basedir + i);
            try {
               if (!fs.exists(p)) {
                  //log.info("logdir %s%04d", basedir, i);
                  Log.setLogFile(new Datafile(fs, basedir + i));
                  Log.out.openWrite();
                  break;
               }
            } catch (IOException ex) {
            }
         }
      }
   }

   public static void setLogFile(FileSystem fs, Configuration c) throws IOException {
      setLogFile(fs, c.get("repository.dir") + "/log/" + c.get("mapred.job.name").replaceAll(" ", "") + "_map_");
   }

   public static void setReduceLogFile(FileSystem fs, Configuration c) throws IOException {
      setLogFile(fs, c.get("repository.dir") + "/log/" + c.get("mapred.job.name").replaceAll(" ", "") + "_reduce_");
   }

   public static void closeLog() {
      if (Log.out != null) {
         Log.out.close();
         Log.out = null;
      }
   }

   public static void clearLog(FileSystem fs, Configuration c) {
      try {
         String jobname = c.get("mapred.job.name").replaceAll(" ", "");
         String name = c.get("repository.dir") + "/log/";
         HDFSDir p = new HDFSDir(c, name);
         for (Path file : p.getFiles()) {
            if (file.getName().startsWith(jobname)) {
               fs.delete(file, false);
            }
         }
      } catch (IOException e) {
         log.exception(e, "clearLog( %s, %s )", fs, c);
      }
   }
   
   public static Configuration readConfigNoMR(String filename) {
      String irefdir = System.getenv("irefdir");
      String irefversion = System.getenv("irefversion");
      Datafile in = configfile(filename);
      ConfTool confreader = new ConfTool(in);
      Configuration conf = confreader.toConf();
      conf.set("iref.irefdir", irefdir + "/");
      conf.set("iref.ireflibdir", irefdir + "/lib/");
      conf.set("iref.configdir", irefdir + "/settings/");
      conf.set("iref.conf", filename);
      conf.set("iref.version", irefversion);
      return conf;
   }

   public static Configuration readConfig(String filename) {
      Configuration conf = readConfigNoMR( filename );
      addJars( conf );
      return conf;
   }

   public static Datafile configfile(String filename) {
      if (filename.charAt(0) != '/') {
         String irefdir = System.getenv("irefdir");
         filename = irefdir + "/settings/" + filename;
      }
      Datafile in = new Datafile(filename);
      return in;
   }

   public static Datafile configfile( Configuration conf ) {
      String irefdir = System.getenv("irefdir");
      Datafile in = new Datafile(irefdir + "/settings/" + conf.get("iref.conf"));
      return in;
   }
   
   public static void addToConfiguration(Configuration configuration, String list) {
      if (list != null && list.length() > 0) {
      ConfTool conf = new ConfTool();
      for (String p : StrTools.split(list, ",")) {
         conf.read(p);
      }
      conf.toConf(configuration);
      }
   }

   public static void writeParametersToFile( Configuration conf, Map<String, String> parameters ) {
      parameters = new HashMap<String, String>(parameters);
      Datafile df = configfile(conf);
      String content = df.readAsString();
      String lines[] = content.split("\\n");
      df.openWrite();
      for (String line : lines) {
         if (line.contains("=")) {
            String key = line.substring(0, line.indexOf("=")).trim();
            String value = parameters.get(key);
            if (value == null) {
               df.printf("%s=%s\n", key, value);
               parameters.remove(key);
            } else {
               df.printf("%s\n", line);
            }
         }
      }
      for (Map.Entry<String, String> e : parameters.entrySet()) {
         df.printf("%s=%s\n", e.getKey(), e.getValue());
      }
      df.closeWrite();
   }
   
   /**
    * A new configuration object is created by reading the configfile mentioned
    * as the first argument using {@link #readConfig(java.lang.String) }.
    * Configuration assignments (module.key=value) are taken out of the args and
    * added to the configuration using {@link ConfTool#read(java.lang.String) }.
    * The remaining arguments in args are processed using {@link ArgsParser}
    * with a template string, that stores the arguments in the configuration
    * under the key given in the template string. {@link ConfTool} is used for
    * the conversion, therefore, an int will be stored as an int.
    *
    * @param args the String array passed to main
    * @param template a string indicating which parameters besides configfile
    * are passed
    * @return configuration object that is created from the configfile (first
    * argument) and the remaining arguments.
    */
   public static Configuration readConfig(String args[], String template) {
      Configuration conf = readConfigNoMR( args, template );
      addJars( conf );
      return conf;
   }
   
   public static Configuration readConfigNoMR(String args[], String template) {
      ConfTool ct = new ConfTool();
      Configuration conf = readConfigNoMR(args[0]);
      conf.setStrings("iref.args", args);
      args = argsToConf(args, conf);
      ArgsParser parsedargs = new ArgsParser(args, "configfile " + template);
      for (Map.Entry<String, String> entry : parsedargs.parsedargs.entrySet()) {
         ct.store(entry.getKey(), entry.getValue() + "\n");
      }
      if (parsedargs.repeatedgroup != null) {
         conf.setStrings(parsedargs.repeatedgroupname, parsedargs.getRepeatedGroup());
      }
      ct.toConf(conf);
      return conf;
   }
   
   public static ByteRegex configuration = new ByteRegex("\\+?\\c\\w*(\\.\\c\\w*)+=\\S*$");

   public static int run(Configuration conf, Tool clazz, String... args) {
      try {
         return ToolRunner.run(conf, clazz, args);
      } catch (Exception ex) {
         log.exception(ex, "LibToArgs()");
      }
      return 1;
   }

   private static void addJars(Configuration conf) {
      String libs = libToArgs(conf);
      try {
         if (libs != null && libs.length() > 0) {
            conf.set("tmpjars",
                    validateFiles(libs, conf));
            //setting libjars in client classpath
            URL[] libjars = getLibJars(conf);
            if (libjars != null && libjars.length > 0) {
               conf.setClassLoader(new URLClassLoader(libjars, conf.getClassLoader()));
               Thread.currentThread().setContextClassLoader(
                       new URLClassLoader(libjars,
                       Thread.currentThread().getContextClassLoader()));
            }
         }
      } catch (IOException ex) {
         log.fatalexception(ex, "addJarsToJobClassPath", libs);
      }
   }

   private static URL[] getLibJars(Configuration conf) throws IOException {
      String jars = conf.get("tmpjars");
      if (jars == null) {
         return null;
      }
      String[] files = jars.split(",");
      URL[] cp = new URL[files.length];
      for (int i = 0; i < cp.length; i++) {
         Path tmp = new Path(files[i]);
         cp[i] = FileSystem.getLocal(conf).pathToFile(tmp).toURI().toURL();
      }
      return cp;
   }

   private static String validateFiles(String files, Configuration conf) throws IOException {
      if (files == null) {
         return null;
      }
      String[] fileArr = files.split(",");
      String[] finalArr = new String[fileArr.length];
      for (int i = 0; i < fileArr.length; i++) {
         String tmp = fileArr[i];
         String finalPath;
         Path path = new Path(tmp);
         URI pathURI = path.toUri();
         FileSystem localFs = FileSystem.getLocal(conf);
         if (pathURI.getScheme() == null) {
            //default to the local file system
            //check if the file exists or not first
            if (!localFs.exists(path)) {
               throw new FileNotFoundException("File " + tmp + " does not exist.");
            }
            finalPath = path.makeQualified(localFs).toString();
         } else {
            // check if the file exists in this file system
            // we need to recreate this filesystem object to copy
            // these files to the file system jobtracker is running
            // on.
            FileSystem fs = path.getFileSystem(conf);
            if (!fs.exists(path)) {
               throw new FileNotFoundException("File " + tmp + " does not exist.");
            }
            finalPath = path.makeQualified(fs).toString();
            try {
               fs.close();
            } catch (IOException e) {
            };
         }
         finalArr[i] = finalPath;
      }
      return StringUtils.arrayToString(finalArr);
   }

   public static String[] libToArgs(Configuration conf, String... otherargs) {
      String libs = libToArgs( conf );
      if (otherargs == null) {
         return new String[]{"-libjars", libs};
      } else {
         String r[] = new String[2 + otherargs.length];
         r[0] = "-libjars";
         r[1] = libs;
         System.arraycopy(otherargs, 0, r, 2, otherargs.length);
         return r;
      }
   }
   public static String libToArgs(Configuration conf) {
      StringBuilder sb = new StringBuilder();
      for (String lib : conf.getSubStrings("iref.lib")) {
         if (!FSFile.exists(lib)) {
            lib = conf.get("iref.ireflibdir") + lib;
         }
         sb.append(",").append(lib);
      }
      return (sb.length() == 0)?"":sb.substring(1);
   }

   public static String[] argsToConf(String args[], Configuration conf) {
      ConfTool c = new ConfTool();
      ArrayList<String> ar = new ArrayList<String>();
      for (int i = 0; i < args.length; i++) {
         if (configuration.matchFirst(args[i])) {
            c.read(args[i]);
         } else {
            ar.add(args[i]);
         }
      }
      c.toConf(conf);
      args = ar.toArray(new String[ar.size()]);
      return args;
   }

   public static String[] processArgSettings(Configuration conf, String args[]) {
      ConfTool c = new ConfTool();
      ArrayList<String> ar = new ArrayList<String>();
      for (int i = 0; i < args.length; i++) {
         int pos = args[i].indexOf("=");
         if (checkArg(args[i])) {
            c.read(args[i]);
         } else {
            ar.add(args[i]);
         }
      }
      c.toConf(conf);
      args = ar.toArray(new String[ar.size()]);
      return args;
   }

   public static void softSetConfiguration(Configuration conf, String key, String value) {
      if (conf.get(key) == null) {
         conf.set(key, value);
      }
   }

   public static void setPriorityHigh(Configuration conf) {
      softSetConfiguration(conf, "mapred.job.priority", JobPriority.HIGH.toString());
   }

   public static void setPriorityVeryHigh(Configuration conf) {
      softSetConfiguration(conf, "mapred.job.priority", JobPriority.VERY_HIGH.toString());
   }

   public static void setPriorityLow(Configuration conf) {
      softSetConfiguration(conf, "mapred.job.priority", JobPriority.LOW.toString());
   }
   public static ByteRegex checkarg = new ByteRegex("\\c\\w*(\\.\\c\\w*)+\\=\\S*$");

   public static boolean checkArg(String a) {
      return checkarg.match(a);
   }

   public static void main(String[] args) {
      log.info("%b", checkArg("apap.ap=13"));
   }

   public static String[] decodeChannels(String ch) {
      String c[] = ch.split(" *, *");
      for (int i = 0; i < c.length; i++) {
         if (c[i].equalsIgnoreCase("main")) {
            c[i] = null;
         }
      }
      return c;
   }

   public static int getReducerId(Reducer.Context context) {
      return context.getTaskAttemptID().getTaskID().getId();
   }

   public static enum MATCH_COUNTERS {

      MAPTASKSDONE,
      MAPTASKSTOTAL,
      REDUCETASKSDONE,
      REDUCETASKSTOTAL
   }

   public static void mapReport(Context context) {
      context.getCounter(MATCH_COUNTERS.MAPTASKSDONE).increment(1);
      context.progress();
   }

   public static void mapAddTasks(Context context, int tasks) {
      context.getCounter(MATCH_COUNTERS.MAPTASKSTOTAL).increment(tasks);
      context.progress();
   }

   public static void mapReport(Context context, int tasks) {
      context.getCounter(MATCH_COUNTERS.MAPTASKSDONE).increment(tasks);
      context.progress();
   }

   public static void reduceReport(Reducer.Context context) {
      context.getCounter(MATCH_COUNTERS.REDUCETASKSDONE).increment(1);
      context.progress();
   }

   public static void reduceReport(Reducer.Context context, int tasks) {
      context.getCounter(MATCH_COUNTERS.REDUCETASKSDONE).increment(tasks);
      context.progress();
   }

   public static void reduceAddTasks(Reducer.Context context, int tasks) {
      context.getCounter(MATCH_COUNTERS.REDUCETASKSTOTAL).increment(tasks);
      context.progress();
   }

   public static long getReduceTasks(Reducer.Context context) {
      return context.getCounter(MATCH_COUNTERS.REDUCETASKSTOTAL).getValue();
   }

   public static long getFileLength(FileSystem fs, Path path) {
      try {
         return fs.getFileStatus(path).getLen();
      } catch (IOException ex) {
         log.fatal(ex);
      }
      return 0;
   }

   public static long getFileLength(FileSystem fs, String filename) {
      return getFileLength(fs, new Path(filename));
   }

   public static String[] getLocations(FileSystem fs, String filename, long offset) {
      String hosts[] = new String[0];
      try {
         if (fs != null) {
            //log.info("getLocations filename %s", filename);
            FileStatus file = fs.getFileStatus(new Path(filename));
            BlockLocation[] blkLocations = fs.getFileBlockLocations(file, offset, 0);
            if (blkLocations.length > 0) {
               hosts = blkLocations[0].getHosts();
            }
         }
      } catch (IOException ex) {
         log.exception(ex, "getLocations( %s, %s, %d )", fs, filename, offset);
      }
      //log.info("getLocations() %d %s", hosts.length, hosts.toString());
      return hosts;
   }

   public static String[] getLocations(HDFSDir d, int partition) {
      FileSystem fs = d.getFS();
      HashMap<String, Integer> hosts = new HashMap<String, Integer>();
      String partitionstring = io.github.repir.tools.Lib.PrintTools.sprintf("%04d", partition);
      try {
         if (d.getFS() != null) {
            for (Path p : d.getFiles()) {
               if (p.getName().contains(partitionstring)) {
                  FileStatus file = fs.getFileStatus(p);
                  BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, 0);
                  for (BlockLocation b : blkLocations) {
                     String h[] = b.getHosts();
                     for (String host : h) {
                        Integer count = hosts.get(host);
                        if (count == null) {
                           hosts.put(host, 1);
                        } else {
                           hosts.put(host, count + 1);
                        }
                     }
                  }
               }
            }
         }
      } catch (IOException ex) {
         log.exception(ex, "getLocations( %s, %d )", d, partition);
      }
      int max = 0;
      String maxlocation = "";
      for (Map.Entry<String, Integer> entry : hosts.entrySet()) {
         if (entry.getValue() > max) {
            max = entry.getValue();
            maxlocation = entry.getKey();
         }
      }
      //log.info("getLocations() %d %s", hosts.length, hosts.toString());
      return new String[]{maxlocation};
   }

   public static boolean exists(FileSystem fs, Path path) {
      try {
         return fs.exists(path);
      } catch (IOException ex) {
         log.exception(ex, "exists( %s, %s )", fs, path);
         return false;
      }
   }

   public static long suggestSplitSize(JobContext context, ArrayList<Path> paths) {
      try {
         org.apache.hadoop.conf.Configuration conf = context.getConfiguration();
         int max = conf.getInt("cluster.nodes", 1);
         int minmapsize = conf.getInt("cluster.minmapsize", 1000000);
         int maxmapsize = conf.getInt("cluster.maxmapsize", 5000000);
         //Path paths[] = FileInputFormat.getInputPaths(context);
         TreeSet<UniqueLong> length = new TreeSet<UniqueLong>();
         FileSystem fs = FileSystem.get(conf);
         for (Path p : paths) {
            //log.info("Path %s", p.toString());
            length.add(new UniqueLong(fs.getFileStatus(p).getLen()));
         }
         long maxlen = length.pollLast().l;
         //log.info("suggestSplitSize() max %d", max);
         if (length.size() > max) {
            return maxmapsize;
         }
         while ((length.size() < max && maxlen > minmapsize) || maxlen > maxmapsize) {
            //log.info("suggestSplitSize() size %d maxlen %d", length.size(), maxlen);
            long c = Math.round(maxlen / (double) maxmapsize);
            if (c > 1) {
               long size = Math.round(maxlen / (double) c);
               for (int i = 0; i < c - 1; i++) {
                  length.add(new UniqueLong(size));
               }
               length.add(new UniqueLong(maxlen - (c - 1) * size));
            } else {
               length.add(new UniqueLong((3 * maxlen) / 4));
               length.add(new UniqueLong(maxlen / 4));
            }
            maxlen = length.pollLast().l;
         }
         if (length.size() == 0) {
            return maxlen;
         } else {
            return length.last().l;
         }
      } catch (IOException ex) {
         log.exception(ex, "suggestSplitSize( %s , %s )", context, paths);
      }
      return 0;
   }

   static class UniqueLong implements Comparable<UniqueLong> {

      Long l;

      public UniqueLong(long l) {
         this.l = l;
      }

      @Override
      public int compareTo(UniqueLong o) {
         return (l.compareTo(o.l) == 0) ? 1 : l.compareTo(o.l);
      }
   }

   public static void print(Configuration conf, String prefix) {
      for (Entry<String, String> e : conf) {
         if (prefix == null || prefix.length() == 0 || e.getKey().startsWith(prefix)) {
            log.printf("%s=%s", e.getKey(), e.getValue());
         }
      }
   }
}
