package io.github.repir.tools.io;

import io.github.repir.tools.collection.ArrayMap;
import io.github.repir.tools.collection.HashMapList;
import io.github.repir.tools.collection.HashMapSet;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.collection.ListIterator;
import io.github.repir.tools.hadoop.io.DatafileInputFormat;
import static io.github.repir.tools.io.HDFSMove.verbose;
import io.github.repir.tools.lib.IteratorIterable;
import io.github.repir.tools.lib.Log;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Trash;
import org.apache.hadoop.io.IOUtils;

/**
 * The Dir class represents a directory of files, and contains many methods to
 * access Files within the directory as RFile
 * <p/>
 * @author jbpvuurens
 */
public class HDFSPath extends org.apache.hadoop.fs.Path implements HPath {

    public static Log log = new Log(HDFSPath.class);
    public FileSystem fs;
    private String lastcomponent;

    /**
     * Constructs a Dir object that uses the provided path
     * <p/>
     * @param directorypath
     */
    public HDFSPath(Configuration conf, String directorypath) {
        super(directorypath.length() == 0 ? "." : directorypath);
        try {
            fs = this.getFileSystem(conf);
        } catch (IOException ex) {
            log.exception(ex, "Constructor( %s, %s )", conf, directorypath);
        }
    }

    public HDFSPath(Configuration conf, org.apache.hadoop.fs.Path path) {
        this(conf, path.toString());
    }

    public HDFSPath(FileSystem fs, String directorypath) {
        super(directorypath);
        this.fs = fs;
    }

    public HDFSPath(FileSystem fs, org.apache.hadoop.fs.Path path) {
        this(fs, path.toString());
    }

    public HDFSPath(HDFSPath path, String child) {
        super(path, child);
        this.fs = path.fs;
    }

    public HDFSPath(HDFSPath path) {
        this(path.fs, path);
    }

    public HDFSPath(org.apache.hadoop.fs.Path path, Configuration conf, String child) {
        super(path, child);
        try {
            this.fs = path.getFileSystem(conf);
        } catch (IOException ex) {
            log.exception(ex, "Constructor( %s, %s, %s )", path, conf, child);
        }
    }

    public HPathWildcardIterator wildcardIterator() {
        return new HPathWildcardIterator(this);
    }

    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public void remove() throws IOException {
        delete(fs, this);
    }

    public void deleteOnExit() throws IOException {
        fs.deleteOnExit(this);
    }

    public static FileSystem getFS(Configuration conf) {
        try {
            return FileSystem.get(conf);
        } catch (IOException ex) {
            log.exception(ex, "getFS( %s )", conf);
        }
        return null;
    }

    public static long getLastModified(FileSystem fs, String file) throws IOException {
        org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(file);
        return fs.getFileStatus(path).getModificationTime();
    }

    public static void setLastModified(FileSystem fs, String file, long time) throws IOException {
        org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(file);
        fs.setTimes(path, time, time);
    }

    public FileStatus getFileStatus() throws IOException {
        return fs.getFileStatus(this);
    }

    public long getModificationTime() throws IOException {
        return fs.getFileStatus(this).getModificationTime();
    }

    public FileStatus[] listFileStatus() throws IOException {
        return fs.listStatus(this);
    }

    /**
     * @return parent directory of a file or directory
     */
    @Override
    public HDFSPath getParentPath() {
        String path = toString();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 1) {
            if (path.startsWith("/")) {
                return new HDFSPath(fs, "/");
            }
            return new HDFSPath(fs, "");
        }
        String parent = path.substring(0, lastSlash);
        return new HDFSPath(fs, parent);
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

    public static boolean exists(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            return fs.exists(path);
        } catch (IOException ex) {
            log.exception(ex, "exists( %s, %s )", fs, path);
            return false;
        }
    }

    public static boolean isFile(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            return fs.isFile(path);
        } catch (IOException ex) {
            log.exception(ex, "isFile( %s, %s )", fs, path);
            return false;
        }
    }

    public static long age(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            return System.currentTimeMillis() - fs.getFileStatus(path).getModificationTime();
        } catch (IOException ex) {
            log.exception(ex, "age( %s, %s )", fs, path);
            return 0;
        }
    }

    public static boolean isDir(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            if (fs.exists(path)) {
                return fs.getFileStatus(path).isDir();
            }
        } catch (IOException ex) {
            log.exception(ex, "isDir( %s, %s )", fs, path);
        }
        return false;
    }

    public static boolean delete(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            return fs.delete(path, true);
        } catch (IOException ex) {
            log.exception(ex, "delete( %s, %s )", fs, path);
        }
        return false;
    }

    public static boolean rename(FileSystem fs, String source, String dest) throws IOException {
        return rename(fs, new org.apache.hadoop.fs.Path(source), new org.apache.hadoop.fs.Path(dest));
    }

    public static boolean copy(FileSystem fs, String source, String dest) throws IOException {
        return copy(fs, new org.apache.hadoop.fs.Path(source), new org.apache.hadoop.fs.Path(dest));
    }

    public static boolean copy(FileSystem fs, org.apache.hadoop.fs.Path source, org.apache.hadoop.fs.Path dest) throws IOException {
        log.info("%s -> %s", source.toString(), dest.toString());
        if (fs.isDirectory(dest)) {
            dest = dest.suffix("/" + source.getName());
        } else {
            fs.mkdirs(dest.getParent());
        }
        FileUtil.copy(fs, source, fs, dest.suffix("._COPYING"), false, true, fs.getConf());
        if (fs.exists(dest)) {
            fs.delete(dest, false);
        }
        return rename(fs, dest.suffix("._COPYING"), dest);
    }

    public static boolean rename(FileSystem fs, org.apache.hadoop.fs.Path source, org.apache.hadoop.fs.Path dest) throws IOException {
        if (!fs.isDirectory(dest))
            if (fs.exists(dest))
                fs.delete(dest, false);
            else 
                fs.mkdirs(dest.getParent());
        else 
            dest = dest.suffix("/" + source.getName());
        return fs.rename(source, dest);
    }

    public static boolean copyToLocal(FileSystem fs, String source, String dest) throws IOException {
        return FileUtil.copy(fs, new org.apache.hadoop.fs.Path(source), new File(dest), false, fs.getConf());
    }

    public static boolean copyFromLocal(FileSystem fs, String source, String dest) throws IOException {
        return FileUtil.copy(new File(source), fs, new org.apache.hadoop.fs.Path(source), false, fs.getConf());
    }

    public static boolean delete(FileSystem fs, String filename) {
        return delete(fs, new org.apache.hadoop.fs.Path(filename));
    }

    public static void backup(HDFSPath source, HDFSPath dest) throws IOException {
        if (dest.isFile()) {
            log.warn("synchronize cannot overwrite existing file with dir %s", dest.getCanonicalPath());
            return;
        }
        if (source.isDir()) {
            if (dest.exists()) {
                FileStatus[] sourceStatus = source.fs.listStatus(source);
                HashMap<String, FileStatus> map = dest.getStatusMap();
                for (FileStatus s : sourceStatus) {
                    String name = s.getPath().getName();
                    if (s.isDirectory()) {
                        map.remove(name);
                        backup(source.getSubdir(name), dest.getSubdir(name));
                    } else {
                        if (dest.exists()) {
                            FileStatus dstatus = map.remove(name);
                            if (dstatus == null || dstatus.getModificationTime() < s.getModificationTime()) {
                                copy(source.fs, source.getSubdir(name), dest.getSubdir(name));
                            }
                        }
                    }
                }
                for (FileStatus dstatus : map.values()) {
                    dest.getFile(dstatus.getPath().getName()).delete();
                }
            } else {
                copy(source.fs, source, dest);
            }
        }
    }

    public static void restore(HDFSPath source, HDFSPath dest) throws IOException {
        if (dest.isFile()) {
            log.warn("restore cannot overwrite existing file with dir %s", dest.getCanonicalPath());
            return;
        }
        if (source.isDir()) {
            if (dest.exists()) {
                FileStatus[] sourceStatus = source.fs.listStatus(source);
                HashMap<String, FileStatus> map = dest.getStatusMap();
                for (FileStatus s : sourceStatus) {
                    String name = s.getPath().getName();
                    if (s.isDirectory()) {
                        map.remove(name);
                        backup(source.getSubdir(name), dest.getSubdir(name));
                    } else {
                        FileStatus dstatus = map.remove(name);
                        if (dstatus == null || dstatus.getModificationTime() > s.getModificationTime()) {
                            copy(source.fs, source.getSubdir(name), dest.getSubdir(name));
                        }
                    }
                }
                for (FileStatus dstatus : map.values()) {
                    dest.getFile(dstatus.getPath().getName()).delete();
                }
            } else {
                copy(source.fs, source, dest);
            }
        }
    }

    /**
     * Distributes files in the given paths (not recursing to sub folders) 
     * over their locations, using the least frequently occurring location first
     * @param paths
     * @return <Location, List<File>>
     */
    public static HashMapList<String, String> distributePath(FileSystem filesystem, Collection<HDFSPath> paths) {
        ArrayList<FileStatus> statussen = new ArrayList();
        for (HDFSPath p : paths) {
            try {
                FileStatus[] listFileStatus = p.listFileStatus();
                if (listFileStatus != null)
                    for (FileStatus fs : listFileStatus)
                        statussen.add(fs);
            } catch (IOException ex) {}
        }
        return distributeFiles(filesystem, statussen);
    }
    
    public static HashMapList<String, String> distributeDatafiles(FileSystem filesystem, Collection<Datafile> files) {
        HashMapSet<String, String> dirmap = new HashMapSet();
        for (Datafile df : files) {
            dirmap.add(df.getDir().getCanonicalPath(), df.getName());
        }
        ArrayList<FileStatus> statussen = new ArrayList();
        for (Map.Entry<String, HashSet<String>> entry : dirmap.entrySet()) {
            try {
                FileStatus[] listFileStatus = filesystem.listStatus(new org.apache.hadoop.fs.Path(entry.getKey()));
                if (listFileStatus != null)
                    for (FileStatus fs : listFileStatus)
                        if (entry.getValue().contains(fs.getPath().getName()))
                            statussen.add(fs);
            } catch (IOException ex) {}
        }
        return distributeFiles(filesystem, statussen);
    }
    
    private static HashMapList<String, String> distributeFiles(FileSystem filesystem, Collection<FileStatus> dirs) {
        HashMapList<String, String> dist = new HashMapList();
        HashMapList<String, String> counthosts = new HashMapList();
        for (FileStatus file : dirs) {
            try {
                BlockLocation[] fileBlockLocations = filesystem.getFileBlockLocations(file, 0, 0);
                if (fileBlockLocations != null) {
                    for (BlockLocation b : fileBlockLocations) {
                        for (String h : b.getHosts()) {
                            counthosts.add(h, file.getPath().toString());
                        }
                    }
                }
            } catch (IOException ex) { }
        }
        ArrayMap<ArrayList<String>, String> sorted = ArrayMap.invert(counthosts).sorted(new Comparator2());
        HashSet<String> assigned = new HashSet();
        for (Map.Entry<ArrayList<String>, String> entry : sorted) {
            for (String file : entry.getKey()) {
                if (!assigned.contains(file)) {
                    dist.add(entry.getValue(), file);
                    assigned.add(file);
                }
            }
        }
        return dist;
    }
    
    static class Comparator2 implements Comparator<Map.Entry<ArrayList<String>, String>> {
        @Override
        public int compare(Map.Entry<ArrayList<String>, String> o1, Map.Entry<ArrayList<String>, String> o2) {
            return o1.getKey().size() - o2.getKey().size();
        }
    }
    
    public HashMap<String, FileStatus> getStatusMap() throws IOException {
        HashMap<String, FileStatus> map = new HashMap();
        for (FileStatus f : fs.listStatus(this)) {
            map.put(f.getPath().getName(), f);
        }
        return map;
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
    public HDFSPath getSubdir(String subdir) {
        return new HDFSPath(this, subdir);
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

    public String getSubDirOf(HDFSPath dir) {
        String parent = dir.getCanonicalPath();
        String current = this.getCanonicalPath();
        if (current.length() > parent.length() && current.startsWith(parent)) {
            return current.substring(parent.length() + 1);
        }
        return "";
    }

    @Override
    public String getCanonicalPath() {
        return toString();
    }

    @Override
    public String getName() {
        String path = getCanonicalPath();
        return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
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

    public long[] mergeFiles(Datafile out, String filestart) throws IOException {
        //log.info("out %s dir %s filestart %s", out.getFullPath(), this.getCanonicalPath(), filestart);
        TreeSet<Datafile> sortedfiles = new TreeSet(getFiles(filestart));
        return mergeFiles(out, sortedfiles);
    }

    public void move(HDFSPath ddir, String sourcefile, String destfile) throws IOException {
        String pattern = sourcefile.replaceAll("\\.", "\\\\.").replaceAll("[\\*]", "(.*)");
        Pattern p = Pattern.compile(pattern);
        String destfilecomponents[] = destfile.split("[\\*]");
        for (String file : getFilenames()) {
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
                    HDFSPath.rename(fs, getFilename(file), ddir.getFilename(sb.toString()));
                }
            }
        }
    }

    public static String[] getLocations(FileSystem fs, String filename, long offset) {
        String hosts[] = new String[0];
        try {
            if (fs != null) {
                FileStatus file = fs.getFileStatus(new org.apache.hadoop.fs.Path(filename));
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

    public void move(HDFSPath ddir) {
        try {
            fs.rename(this, ddir);
        } catch (IOException ex) {
            log.exception(ex, "move %s to %s", this, ddir);
        }
    }

    public void trash() throws IOException {
        if (exists()) {
            Trash trash = new Trash(fs, fs.getConf());
            trash.moveToTrash(this);
        }
    }

    public static void trash(FileSystem fs, org.apache.hadoop.fs.Path path) throws IOException {
        if (fs.exists(path)) {
            Trash trash = new Trash(fs, fs.getConf());
            trash.moveToTrash(path);
        }
    }

    public static void trash(FileSystem fs, String path) throws IOException {
        trash(fs, new org.apache.hadoop.fs.Path(path));
    }

    @Override
    public IteratorIterable<DirComponent> iterator() {
        try {
            if (!this.exists() && this.getCanonicalPath().contains("*")) {
                return wildcardIterator().iterator();
            } else {
                return new ListIterator(get());
            }
        } catch (IOException ex) {
            log.fatalexception(ex, "iterator( %s )", this.getCanonicalPath());
        }
        return null;
    }

    public ListIterator<DirComponent> iteratorRecursive() throws IOException {
        return new ListIterator(getRecursive());
    }

    public ListIterator<DirComponent> iterator(String regexstring) throws IOException {
        return new ListIterator(get(regexstring));
    }

    public ListIterator<DirComponent> iterator(ByteSearch regex) throws IOException {
        return new ListIterator(get(regex));
    }

    public ListIterator<DirComponent> iteratorDirs() throws IOException {
        return new ListIterator(getDirs());
    }

    public ListIterator<DirComponent> iteratorFiles() throws IOException {
        return new ListIterator(getFiles());
    }

    public ListIterator<DirComponent> iteratorDirs(String regexstring) throws IOException {
        return new ListIterator(getDirs(regexstring));
    }

    public ListIterator<DirComponent> iteratorDirs(ByteSearch regexstring) throws IOException {
        return new ListIterator(getDirs(regexstring));
    }

    public ListIterator<DirComponent> iteratorFiles(String regexstring) throws IOException {
        return new ListIterator(getFiles(regexstring));
    }

    private ListIterator<DirComponent> iteratorFiles(ByteSearch regexstring) throws IOException {
        return new ListIterator(getFiles(regexstring));
    }

    @Override
    public ArrayList<DirComponent> get() throws IOException {
        ArrayList<DirComponent> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (child.isDir()) {
                    results.add(new HDFSPath(fs, child.getPath().toString()));
                } else {
                    results.add(new Datafile(fs, child.getPath().toString()));
                }
            }
        } else if (isFile(fs, this)) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<DirComponent> getRecursive() throws IOException {
        ArrayList<DirComponent> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (child.isDir()) {
                    HDFSPath dir = new HDFSPath(fs, child.getPath().toString());
                    results.add(dir);
                    results.addAll(dir.getRecursive());
                } else {
                    results.add(new Datafile(fs, child.getPath().toString()));
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<DirComponent> get(String regexstring) throws IOException {
        return get(ByteSearch.createFilePattern(regexstring));
    }

    private ArrayList<DirComponent> get(ByteSearch pattern) throws IOException {
        ArrayList<DirComponent> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (pattern.exists(name)) {
                    if (!child.isDir()) {
                        results.add(new Datafile(fs, child.getPath().toString()));
                    } else {
                        results.add(new HDFSPath(fs, child.getPath().toString()));

                    }
                }
            }
        } else if (isFile(fs, this) && pattern.exists(getName())) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles() throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (!child.isDir()) {
                    results.add(new Datafile(fs, child.getPath().toString()));
                }
            }
        } else if (isFile(fs, this)) {
            results.add(new Datafile(fs, this));
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFilesNewerThan(long lastupdate) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (!child.isDir() && child.getModificationTime() >= lastupdate) {
                    results.add(new Datafile(fs, child.getPath().toString()));
                }
            }
        } else if (isFile(fs, this)) {
            results.add(new Datafile(fs, this));
        }
        return results;
    }

    public ArrayList<Datafile> getFilesNewerThan(HDFSPath path) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.isDir() && path.exists()) {
                HashMap<String, FileStatus> mapped = new HashMap();
                for (FileStatus f : path.fs.listStatus(path)) {
                    mapped.put(f.getPath().getName(), f);
                }
                for (FileStatus child : fs.listStatus(this)) {
                    if (!child.isDir()) {
                        FileStatus f = mapped.get(child.getPath().getName());
                        if (f == null || child.getModificationTime() > f.getModificationTime()) {
                            results.add(new Datafile(fs, child.getPath().toString()));
                        }
                    }
                }
            } else {
                return getFiles();
            }
        } else if (isFile(fs, this)) {
            if (path.isDir() && path.exists()) {
                HDFSPath child = path.getSubdir(this.getName());
                if (isFile(fs, child) && this.getModificationTime() > child.getModificationTime()) {
                    results.add(new Datafile(fs, getFileStatus().getPath().toString()));
                }
            } else if (!path.exists() || getModificationTime() > path.getModificationTime())
                results.add(new Datafile(fs, getFileStatus().getPath().toString()));
        }
        return results;
    }

    public ArrayList<Datafile> getFilesOlderThan(HDFSPath path) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.isDir() && path.exists()) {
                HashMap<String, FileStatus> mapped = new HashMap();
                for (FileStatus f : path.fs.listStatus(path)) {
                    mapped.put(f.getPath().getName(), f);
                }
                for (FileStatus child : fs.listStatus(this)) {
                    if (!child.isDir()) {
                        FileStatus f = mapped.get(child.getPath().getName());
                        if (f == null || child.getModificationTime() < f.getModificationTime()) {
                            results.add(new Datafile(fs, child.getPath().toString()));
                        }
                    }
                }
            } else {
                return getFiles();
            }
        } else if (isFile(fs, this)) {
            if (path.isDir() && path.exists()) {
                HDFSPath child = path.getSubdir(this.getName());
                if (isFile(fs, child) && this.getModificationTime() < child.getModificationTime()) {
                    results.add(new Datafile(fs, getFileStatus().getPath().toString()));
                }
            } else if (!path.exists() || getModificationTime() < path.getModificationTime())
                results.add(new Datafile(fs, getFileStatus().getPath().toString()));
        }
        return results;
    }

    public ArrayList<Datafile> getFilesNonExist(HDFSPath path) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.isDir() && path.exists()) {
                HashSet<String> mapped = new HashSet();
                for (FileStatus f : path.fs.listStatus(path)) {
                    mapped.add(f.getPath().getName());
                }
                for (FileStatus child : fs.listStatus(this)) {
                    if (!child.isDir() && !mapped.contains(child.getPath().getName())) {
                        results.add(new Datafile(fs, child.getPath().toString()));
                    }
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<String> getFilenames() throws IOException {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : iterator()) {
            if (c instanceof Datafile) {
                results.add(((Datafile) c).getName());
            }
        }
        return results;
    }

    public ArrayList<String> getFilepathnames() throws IOException {
        ArrayList<String> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (child.isFile()) {
                    results.add(child.getPath().toString());
                }
            }
        } else if (isFile(fs, this)) {
            results.add(this.getCanonicalPath());
        } else if (this.getName().contains("*")) {
            this.getParentPath().getFilenames(getName());
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles(String regexstring) throws IOException {
        return getFiles(ByteSearch.createFilePattern(regexstring));
    }

    private ArrayList<Datafile> getFiles(ByteSearch pattern) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (pattern.match(name)) {
                    if (!child.isDir()) {
                        results.add(new Datafile(fs, child.getPath().toString()));
                    }
                }
            }
        } else if (isFile(fs, this) && pattern.match(getName())) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    public ArrayList<String> getFilenames(String regexstring) throws IOException {
        return getFilenames(ByteSearch.createFilePattern(regexstring));
    }

    private ArrayList<String> getFilenames(ByteSearch pattern) throws IOException {
        ArrayList<String> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (pattern.match(name)) {
                    if (!child.isDir()) {
                        results.add(name);
                    }
                }
            }
        } else if (isFile(fs, this) && pattern.match(getName())) {
            results.add(getName());
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFilesStartingWith(String start) throws IOException {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (name.startsWith(start)) {
                    if (!child.isDir()) {
                        results.add(new Datafile(fs, child.getPath().toString()));
                    }
                }
            }
        } else if (isFile(fs, this) && getName().startsWith(start)) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<HDFSPath> getDirs() throws IOException {
        ArrayList<HDFSPath> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (child.isDir()) {
                    results.add(new HDFSPath(fs, child.getPath().toString()));
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<String> getDirnames() throws IOException {
        ArrayList<String> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                if (child.isDir()) {
                    results.add(child.getPath().toString());
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<HDFSPath> getDirs(String regexstring) throws IOException {
        return getDirs(ByteSearch.createFilePattern(regexstring));
    }

    @Override
    public ArrayList<HDFSPath> getDirs(ByteSearch pattern) throws IOException {
        ArrayList<HDFSPath> results = new ArrayList();
        if (isDir(fs, this)) {
            for (FileStatus child : fs.listStatus(this)) {
                String name = child.getPath().getName();
                if (pattern.exists(name)) {
                    if (child.isDir()) {
                        results.add(new HDFSPath(this.fs, child.getPath().toString()));
                    }
                }
            }
        }
        return results;
    }
}
