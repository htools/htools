package io.github.htools.io;

import io.github.htools.collection.ArrayMap;
import io.github.htools.collection.HashMapList;
import io.github.htools.collection.HashMapSet;
import io.github.htools.collection.ListIterator;
import io.github.htools.hadoop.Conf;
import io.github.htools.lib.IteratorIterable;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.htools.io.HDFSMove.verbose;

/**
 * HDFSPath id a wrapper around hadoop.fs.Path that adds functionality to the
 * use of paths. Typically a HDFSPath represents a directory of files (although
 * some methods also work if HDFSPath points to a file). The interface through
 * HPath provides the same operations on FSPath which is the equivalent to
 * HDFSPath for local filesystems, to allow the same code to run on both
 * environments.
 * <p>
 *
 * @author jbpvuurens
 */
public class HDFSPath extends Path implements HPath {

    public static Log log = new Log(HDFSPath.class);
    public FileSystem fs;
    private String lastcomponent;

    /**
     * Constructs a Dir object that uses the provided path
     * <p>
     *
     * @param directorypath
     */
    public HDFSPath(Configuration conf, String directorypath) {
        super(directorypath.length() == 0 ? "." : directorypath);
        fs = Conf.getFileSystem(conf);
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
        this.fs = Conf.getFileSystem(conf);
    }

    public IteratorIterable<DirComponent> wildcardIterator() {
        return new HPathWildcardIterator(this, FileFilter.exceptSuccess);
    }

    public IteratorIterable<DirComponent> wildcardIterator(FileFilter filter) {
        return new HPathWildcardIterator(this, filter);
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

    public static long getLastModified(FileSystem fs, String file) {
        return getFileStatus(fs, file).getModificationTime();
    }

    public static void setLastModified(FileSystem fs, String file, long time) throws IOException {
        org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(file);
        fs.setTimes(path, time, time);
    }

    public static FileStatus getFileStatus(FileSystem fs, String file) {
        try {
            org.apache.hadoop.fs.Path path = new org.apache.hadoop.fs.Path(file);
            return fs.getFileStatus(path);
        } catch (IOException ex) {
        }
        return null;
    }

    public FileStatus getFileStatus() {
        try {
            return fs.getFileStatus(this);
        } catch (IOException ex) {
        }
        return null;
    }

    public long getModificationTime() {
        return getFileStatus().getModificationTime();
    }

    public void setOwner(String username, String groupname) throws IOException {
        fs.setOwner(this, username, groupname);
    }

    public void setPermissions(String umask) throws IOException {
        FsPermission permission = new FsPermission(umask);
        fs.setPermission(this, permission);
    }

    public static void setPermissions(Configuration conf, String path, String umask) throws IOException {
        FsPermission permission = new FsPermission(umask);
        Conf.getFileSystem(conf).setPermission(new Path(path), permission);
    }

    public FileStatus[] listFileStatus() {
        try {
            return fs.listStatus(this);
        } catch (IOException ex) {
        }
        return new FileStatus[0];
    }

    public static FileStatus[] listFileStatus(FileSystem fs, String filepath) {
        try {
            org.apache.hadoop.fs.Path path = new Path(filepath);
            return fs.listStatus(path);
        } catch (IOException ex) {
        }
        return new FileStatus[0];
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

    @Override
    public boolean existsDir() {
        return isDir(fs, this);
    }

    @Override
    public boolean exists() {
        return exists(fs, this);
    }

    @Override
    public boolean existsFile() {
        return existsFile(fs, this);
    }

    public static boolean exists(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            return fs.exists(path);
        } catch (IOException ex) {
            log.exception(ex, "exists( %s, %s )", fs, path);
            return false;
        }
    }

    public static boolean exists(FileSystem fs, String pathstring) {
        try {
            return fs.exists(new Path(pathstring));
        } catch (IOException ex) {
            log.exception(ex, "exists( %s, %s )", fs, pathstring);
            return false;
        }
    }

    public static boolean existsFile(FileSystem fs, org.apache.hadoop.fs.Path path) {
        try {
            if (fs.exists(path)) {
                FileStatus fileStatus = fs.getFileStatus(path);
                return fileStatus.isFile();
            }
            return false;
        } catch (IOException ex) {
            //log.exception(ex, "isFile( %s, %s )", fs, path);
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
        if (!fs.isDirectory(dest)) {
            if (fs.exists(dest)) {
                fs.delete(dest, false);
            } else {
                fs.mkdirs(dest.getParent());
            }
        } else {
            dest = dest.suffix("/" + source.getName());
        }
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
        if (dest.existsFile()) {
            log.warn("synchronize cannot overwrite existing file with dir %s", dest.getCanonicalPath());
            return;
        }
        ArrayList<String> files = new ArrayList();
        if (source.existsDir()) {
            if (dest.existsDir()) {
                FileStatus[] sourceStatus = source.listFileStatus();
                HashMap<String, FileStatus> map = dest.getStatusMap();
                for (FileStatus s : sourceStatus) {
                    String name = s.getPath().getName();
                    if (s.isDirectory()) {
                        map.remove(name);
                        backup(source.getSubdir(name), dest.getSubdir(name));
                    } else {
                        if (dest.existsDir()) {
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
        if (dest.existsFile()) {
            log.warn("restore cannot overwrite existing file with dir %s", dest.getCanonicalPath());
            return;
        }
        if (source.existsDir()) {
            if (dest.existsDir()) {
                FileStatus[] sourceStatus = source.listFileStatus();
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

    public static void removeNonExisting(HDFSPath source, HDFSPath dest) throws IOException {
        if (dest.existsFile()) {
            log.warn("restore cannot overwrite existing file with dir %s", dest.getCanonicalPath());
            return;
        }
        if (source.existsDir()) {
            if (dest.existsDir()) {
                FileStatus[] sourceStatus = source.listFileStatus();
                HashMap<String, FileStatus> map = dest.getStatusMap();
                for (FileStatus s : sourceStatus) {
                    String name = s.getPath().getName();
                    if (s.isDirectory()) {
                        map.remove(name);
                    } else {
                        map.remove(name);
                    }
                }
                for (FileStatus dstatus : map.values()) {
                    dest.getFile(dstatus.getPath().getName()).delete();
                }
            }
        } else if (!source.existsDir()) {
            if (dest.existsFile()) {
                dest.remove();
            }
        }
    }

    /**
     * Distributes files in the given paths (not recursing to sub folders) over
     * their locations, using the least frequently occurring location first
     *
     * @param paths
     * @return &lt;Location, List&lt;File&gt;&gt;
     */
    public static HashMapList<String, String> distributePath(FileSystem filesystem, Collection<HDFSPath> paths) {
        ArrayList<FileStatus> statussen = new ArrayList();
        for (HDFSPath p : paths) {
            FileStatus[] listFileStatus = p.listFileStatus();
            if (listFileStatus != null) {
                for (FileStatus fs : listFileStatus) {
                    statussen.add(fs);
                }
            }
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
            FileStatus[] listFileStatus = listFileStatus(filesystem, entry.getKey());
            if (listFileStatus != null) {
                for (FileStatus fs : listFileStatus) {
                    if (entry.getValue().contains(fs.getPath().getName())) {
                        statussen.add(fs);
                    }
                }
            }
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
            } catch (IOException ex) {
            }
        }
        ArrayMap<ArrayList<String>, String> sorted =
                new ArrayMap(counthosts.invert()).sorted(new Comparator2());
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

    public HashMap<String, FileStatus> getStatusMap() {
        HashMap<String, FileStatus> map = new HashMap();
        for (FileStatus f : this.listFileStatus()) {
            map.put(f.getPath().getName(), f);
        }
        return map;
    }

    /**
     * Constructs a new subdir, based on this path and the name of the subdir.
     * The subdir is however not yet created. This can be done by calling
     * mkdirs() on the returned Dir object.
     * <p>
     *
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
     * <p>
     *
     * @param filename
     * @return
     */
    @Override
    public String getFilename(String filename) {
        return this.getCanonicalPath() + "/" + filename;
    }

    @Override
    public Datafile getFile(String filename) {
        return new Datafile(fs, getFilename(filename));
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

    public static long[] mergeFiles(Datafile out, Collection<Datafile> sortedfiles) throws IOException {
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

    public long[] mergeFiles(Datafile out, ByteSearch filestart) throws IOException {
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
                FileStatus file = getFileStatus(fs, filename);
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

    /**
     * Move the HDFSpath to the trash (so basically delete with possibility to
     * undo)
     *
     * @throws IOException
     */
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
        if (!this.existsDir()) {
            return wildcardIterator();
        } else {
            return new ListIterator(get());
        }
    }

    @Override
    public ListIterator<DirComponent> iteratorRecursive() {
        return new ListIterator(getRecursive());
    }

    public ListIterator<DirComponent> iterator(String regexstring) {
        return new ListIterator(get(regexstring));
    }

    public ListIterator<DirComponent> iterator(ByteSearch regex) {
        return new ListIterator(get(regex));
    }

    public ListIterator<HPath> iteratorDirs() {
        return new ListIterator(getDirs());
    }

    public ListIterator<Datafile> iteratorFiles() {
        return new ListIterator(getFiles());
    }

    public ListIterator<HPath> iteratorDirs(ByteSearch regexstring) {
        return new ListIterator(getDirs(regexstring));
    }

    public ListIterator<Datafile> iteratorFiles(ByteSearch regexstring) {
        return new ListIterator(getFiles(regexstring));
    }

    @Override
    public ArrayList<DirComponent> get() {
        return get(FileFilter.exceptSuccess);
    }

    public ArrayList<DirComponent> get(FileFilter filter) {
        ArrayList<DirComponent> results = new ArrayList();
        if (!exists()) {
            for (DirComponent c : wildcardIterator(filter)) {
                results.add(c);
            }
        } else if (isDir(fs, this)) {
            try {
                FileStatus[] listStatus = fs.listStatus(this);
                for (FileStatus child : listStatus) {
                    if (filter.allow(child.getPath())) {
                        if (child.isDir()) {
                            results.add(new HDFSPath(fs, child.getPath().toString()));
                        } else {
                            results.add(new Datafile(fs, child.getPath().toString()));
                        }
                    }
                }
            } catch (IOException ex) {
            }
        } else if (existsFile(fs, this)) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<DirComponent> getRecursive() {
        return getRecursive(FileFilter.exceptSuccess);
    }

    public ArrayList<DirComponent> getRecursive(FileFilter filter) {
        ArrayList<DirComponent> results = new ArrayList();
        if (!exists()) {
            for (DirComponent c : wildcardIterator(filter)) {
                results.add(c);
                if (c instanceof HDFSPath && c.existsDir()) {
                    results.addAll(((HDFSPath) c).getRecursive());
                }
            }
        } else if (isDir(fs, this)) {
            try {
                for (FileStatus child : fs.listStatus(this)) {
                    if (filter.allow(child.getPath())) {
                        if (child.isDir()) {
                            HDFSPath dir = new HDFSPath(fs, child.getPath().toString());
                            results.add(dir);
                            results.addAll(dir.getRecursive());
                        } else {
                            results.add(new Datafile(fs, child.getPath().toString()));
                        }
                    }
                }
            } catch (IOException ex) {
            }
        } else if (existsFile(fs, this)) {
            results.add(new Datafile(fs, this.getCanonicalPath()));
        }
        return results;
    }

    public ArrayList<Datafile> getRecursiveFiles(FileFilter filter) {
        ArrayList<Datafile> results = new ArrayList();
        for (DirComponent c : getRecursive(filter)) {
            if (c instanceof Datafile)
                results.add((Datafile) c);
        }
        return results;
    }

    public ArrayList<DirComponent> get(String regexstring) {
        return get(ByteSearch.createFilePattern(regexstring));
    }

    @Override
    public ArrayList<DirComponent> get(ByteSearch pattern) {
        ArrayList<DirComponent> results = new ArrayList();
        for (DirComponent c : get()) {
            if (pattern.match(c.getName())) {
                results.add(c);
            }
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles() {
        ArrayList<Datafile> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof Datafile) {
                results.add((Datafile) c);
            }
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFilesNewerThan(long lastupdate) {
        ArrayList<Datafile> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof Datafile && ((Datafile) c).getLastModified() >= lastupdate) {
                results.add((Datafile) c);
            }
        }
        return results;
    }

    public ArrayList<Datafile> getFilesNewerThan(HDFSPath path) {
        return getFilesNewerThan(path, FileFilter.exceptSuccess);
    }

    public ArrayList<Datafile> getFilesNewerThan(HDFSPath path, FileFilter filter) {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.existsDir() && path.existsDir()) {
                HashMap<String, FileStatus> mapped = new HashMap();
                for (FileStatus f : path.listFileStatus()) {
                    mapped.put(f.getPath().getName(), f);
                }
                for (FileStatus child : this.listFileStatus()) {
                    if (filter.allow(child.getPath())) {
                        if (!child.isDir()) {
                            FileStatus f = mapped.get(child.getPath().getName());
                            if (f == null || child.getModificationTime() > f.getModificationTime()) {
                                results.add(new Datafile(fs, child.getPath().toString()));
                            }
                        }
                    }
                }
            } else {
                return getFiles();
            }
        } else if (existsFile(fs, this)) {
            if (path.existsDir() && path.existsDir()) {
                HDFSPath child = path.getSubdir(this.getName());
                if (existsFile(fs, child) && this.getModificationTime() > child.getModificationTime()) {
                    results.add(new Datafile(fs, getFileStatus().getPath().toString()));
                }
            } else if (!path.existsDir() || getModificationTime() > path.getModificationTime()) {
                results.add(new Datafile(fs, getFileStatus().getPath().toString()));
            }
        }
        return results;
    }

    public ArrayList<Datafile> getFilesOlderThan(HDFSPath path, FileFilter filter) {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.existsDir() && path.existsDir()) {
                HashMap<String, FileStatus> mapped = new HashMap();
                try {
                    for (FileStatus f : path.fs.listStatus(path)) {
                        mapped.put(f.getPath().getName(), f);
                    }
                } catch (IOException ex) {
                }
                try {
                    for (FileStatus child : fs.listStatus(this)) {
                        if (!child.isDir() && filter.allow(child.getPath())) {
                            FileStatus f = mapped.get(child.getPath().getName());
                            if (f == null || child.getModificationTime() < f.getModificationTime()) {
                                results.add(new Datafile(fs, child.getPath().toString()));
                            }
                        }
                    }
                } catch (IOException ex) {
                }
            } else {
                return getFiles();
            }
        } else if (existsFile(fs, this)) {
            if (path.existsDir() && path.existsDir()) {
                HDFSPath child = path.getSubdir(this.getName());
                if (existsFile(fs, child) && this.getModificationTime() < child.getModificationTime()) {
                    results.add(new Datafile(fs, getFileStatus().getPath().toString()));
                }
            } else if (!path.existsDir() || getModificationTime() < path.getModificationTime()) {
                results.add(new Datafile(fs, getFileStatus().getPath().toString()));
            }
        }
        return results;
    }

    public ArrayList<Datafile> getFilesNonExist(HDFSPath path) {
        return getFilesNonExist(path, FileFilter.exceptSuccess);
    }

    public ArrayList<Datafile> getFilesNonExist(HDFSPath path, FileFilter filter) {
        ArrayList<Datafile> results = new ArrayList();
        if (isDir(fs, this)) {
            if (path.existsDir() && path.existsDir()) {
                HashSet<String> mapped = new HashSet();
                for (FileStatus f : path.listFileStatus()) {
                    mapped.add(f.getPath().getName());
                }
                for (FileStatus child : this.listFileStatus()) {
                    if (!child.isDir() && !mapped.contains(child.getPath().getName()) && filter.allow(child.getPath())) {
                        results.add(new Datafile(fs, child.getPath().toString()));
                    }
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<String> getFilenames() {
        return getFilenames(FileFilter.exceptSuccess);
    }

    public ArrayList<String> getFilenames(FileFilter filter) {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : get(filter)) {
            if (c instanceof Datafile) {
                results.add(c.getName());
            }
        }
        return results;
    }

    public ArrayList<String> getFilepathnames() {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof Datafile) {
                results.add(c.getCanonicalPath());
            }
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles(ByteSearch pattern) {
        ArrayList<Datafile> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof Datafile && pattern.match(c.getName())) {
                results.add((Datafile) c);
            }
        }
        return results;
    }

    public ArrayList<String> getFilenames(ByteSearch pattern) {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof Datafile && pattern.match(c.getName())) {
                results.add(c.getName());
            }
        }
        return results;
    }

    @Override
    public ArrayList<HDFSPath> getDirs() {
        ArrayList<HDFSPath> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof HPath) {
                results.add((HDFSPath) c);
            }
        }
        return results;
    }

    @Override
    public ArrayList<String> getDirnames() {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof HPath) {
                results.add(c.getName());
            }
        }
        return results;
    }

    public ArrayList<HDFSPath> getDirs(String regexstring) {
        return getDirs(ByteSearch.createFilePattern(regexstring));
    }

    @Override
    public ArrayList<HDFSPath> getDirs(ByteSearch pattern) {
        ArrayList<HDFSPath> results = new ArrayList();
        for (DirComponent c : get()) {
            if (c instanceof HPath && pattern.match(c.getName())) {
                results.add((HDFSPath) c);
            }
        }
        return results;
    }
}
