package io.github.repir.tools.io;

import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.collection.ListIterator;
import static io.github.repir.tools.io.HDFSMove.verbose;
import io.github.repir.tools.lib.IteratorIterable;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Trash;
import org.apache.hadoop.io.IOUtils;

/**
 * The Dir class represents a directory of files, and contains many methods to
 * access Files within the directory as RFile
 * <p/>
 * @author jbpvuurens
 */
public class HDFSPath extends org.apache.hadoop.fs.Path implements Path { 

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
    
    public HDFSPath wildcardIterator() {
        ArrayDeque<String> components = new ArrayDeque();
        HDFSPath path = this;
        while (!path.exists()) {
            components.addFirst(path.getName());
            path = path.getParent();
        }
        log.info("%s %s", path.toString(), components);
        if (components.size() == 0)
            return this;
        return new HDFSPathWildcard(path, components);
    }


    public void delete() throws IOException {
        fs.delete(this.makeQualified(fs), true);
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

    /**
     * @return parent directory of a file or directory
     */
    @Override
    public HDFSPath getParent() {
        String path = toString();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 1) {
            if (path.startsWith("/"))
                return new HDFSPath(fs, "/");
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

    public static boolean rename(FileSystem fs, org.apache.hadoop.fs.Path path, org.apache.hadoop.fs.Path dest) {
        try {
            return fs.rename(path, dest);
        } catch (IOException ex) {
            log.exception(ex, "rename( %s, %s, %s )", fs, path, dest);
        }
        return false;
    }

    public static boolean copy(FileSystem fs, org.apache.hadoop.fs.Path path, org.apache.hadoop.fs.Path dest) {
        OutputStream out = null;
        try {
            InputStream in = fs.open(path);
            out = fs.create(dest);
            IOUtils.copyBytes(in, out, 4096, false);
            in.close();
            out.close();
            return true;
        } catch (IOException ex) {
            log.exception(ex, "copy %s %s", path, dest);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                log.exception(ex, "copy %s %s", path, dest);
            }
        }
        return false;
    }

    public static boolean rename(FileSystem fs, String source, String dest) {
        return rename(fs, new org.apache.hadoop.fs.Path(source), new org.apache.hadoop.fs.Path(dest));
    }

    public static boolean copy(FileSystem fs, String source, String dest) {
        return copy(fs, new org.apache.hadoop.fs.Path(source), new org.apache.hadoop.fs.Path(dest));
    }

    public static boolean delete(FileSystem fs, String filename) {
        return delete(fs, new org.apache.hadoop.fs.Path(filename));
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
        Trash trash = new Trash(fs, fs.getConf());
        trash.moveToTrash(this);
    }

    @Override
    public IteratorIterable<DirComponent> iterator() {
        try {
            if (!this.exists() && this.getCanonicalPath().contains("*"))
                return wildcardIterator().iterator();
            else
                return new ListIterator(get());
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

    protected ListIterator<DirComponent> iterator(ByteSearch regex) throws IOException {
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

    protected ListIterator<DirComponent> iteratorDirs(ByteSearch regexstring) throws IOException {
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
    public ArrayList<String> getFilenames() throws IOException {
        ArrayList<String> results = new ArrayList();
        for (DirComponent c : iterator()) {
            if (c instanceof Datafile)
                results.add(((Datafile)c).getFilename());
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
            this.getParent().getFilenames(getName());
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles(String regexstring) throws IOException {
        return getFiles( ByteSearch.createFilePattern(regexstring) );
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
        return getFilenames( ByteSearch.createFilePattern(regexstring) );
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
    
    private ArrayList<HDFSPath> getDirs(ByteSearch pattern) throws IOException {
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
