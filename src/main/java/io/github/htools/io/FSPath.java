package io.github.htools.io;

import io.github.htools.collection.ListIterator;
import io.github.htools.lib.IteratorIterable;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import org.apache.hadoop.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.htools.lib.Const.NULLLONG;

/**
 * FSPath id a wrapper around File that adds functionality to the use of paths
 * on local filesystems. Typically a FSPath represents a directory of files
 * (although some methods also work if FSPath points to a file). The interface
 * through HPath provides the same operations on HDFSPath which is the
 * equivalent to FSPath for the HDFS filesystems, to allow the same code to run
 * on both environments.
 * <p>
 * @author jbpvuurens
 */
public class FSPath extends File implements HPath {

    public static Log log = new Log(FSPath.class);

    /**
     * Constructs a Dir object that uses the provided path
     * <p>
     * @param directorypath
     */
    public FSPath(String directorypath) {
        super(directorypath);
    }

    @Override
    public boolean existsDir() {
        return (super.exists() && super.isDirectory());
    }

    public boolean existsFile(String filename) {
        File f = new File(this.getFilename(filename));
        return (f.exists() && f.isFile());
    }

    @Override
    public boolean existsFile() {
        return (exists() && isFile());
    }

    public static boolean exists(String filename) {
        File f = new File(filename);
        return f.exists();
    }

    public static boolean existsDir(String filename) {
        File f = new File(filename);
        return f.exists() && f.isDirectory();
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

    @Override
    public String getName() {
        String path = getCanonicalPath();
        return path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
    }

    public static void copy(String path1, String path2) throws IOException {
        java.nio.file.Files.copy(
                new java.io.File(path1).toPath(),
                new java.io.File(path2).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                java.nio.file.LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * @return last Modification time of the path
     */
    public static long getLastModified(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            return file.lastModified();
        } else {
            return NULLLONG;
        }
    }

    public static boolean setLastModified(String filename, long time) {
        File file = new File(filename);
        if (file.exists()) {
            file.setLastModified(time);
            return true;
        }
        return false;
    }

    /**
     * Constructs a new subdir, based on this path and the name of the subdir.
     * The subdir is however not yet created. This can be done by calling
     * mkdirs() on the returned Dir object.
     * <p>
     * @param subdir
     * @return
     */
    public FSPath getSubdir(String subdir) {
        return new FSPath(this.getCanonicalPath() + "/" + subdir);
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
     * Construct a filename based on the path of this dir and the filename given
     * <p>
     * @param filename
     * @return
     */
    @Override
    public String getFilename(String filename) {
        return this.getCanonicalPath() + "/" + filename;
    }

    @Override
    public Datafile getFile(String filename) {
        return new Datafile(getFilename(filename));
    }

    /**
     * Construct a FSFile based on the path of this dir and the filename given
     * <p>
     * @param filename
     * @return
     */
    public FSFile getRFile(String filename) {
        return new FSFile(getFilename(filename));
    }

    public FSFileOutBuffer getDataFileOut(String filename) {
        return new FSFileOutBuffer(getFilename(filename));
    }

    public static void mergeFiles(Datafile out, Iterator<Datafile> files) throws IOException {
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

    public void move(FSPath ddir, String sourcefile, String destfile, boolean verbose) {
        String pattern = sourcefile.replaceAll("\\.", "\\\\.").replaceAll("[\\*]", "(.*)");
        Pattern p = Pattern.compile(pattern);
        String destfilecomponents[] = (destfile + " ").split("[\\*]");
        for (String file : getFilenames()) {
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

    @Override
    public IteratorIterable<DirComponent> iterator() {
        if (exists()) {
            return new ListIterator(get());
        } else {
            return wildcardIterator();
        }
    }

    public ListIterator<DirComponent> iteratorRecursive() {
        return new ListIterator(getRecursive());
    }

    public ListIterator<DirComponent> iterator(String regexstring) {
        return new ListIterator(get(regexstring));
    }

    @Override
    public ListIterator<HPath> iteratorDirs() {
        return new ListIterator(FSPath.this.getDirs());
    }

    @Override
    public ListIterator<Datafile> iteratorFiles() {
        return new ListIterator(getFiles());
    }

    @Override
    public ListIterator<HPath> iteratorDirs(ByteSearch regexstring) {
        return new ListIterator(getDirs(regexstring));
    }

    @Override
    public ListIterator<DirComponent> iterator(ByteSearch regex) {
        return new ListIterator(get(regex));
    }

    @Override
    public IteratorIterable<DirComponent> wildcardIterator() {
        return new HPathWildcardIterator(this, FileFilter.acceptAll);
    }

    @Override
    public ArrayList<DirComponent> get() {
        ArrayList<DirComponent> results = new ArrayList();
        if (!exists()) {
            for (DirComponent c : wildcardIterator()) {
                results.add((Datafile) c);
            }
        } else if (this.isFile()) {
            results.add(new Datafile(this.getCanonicalPath()));
        } else if (this.isDirectory()) {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (file.isDirectory()) {
                    results.add(new FSPath(fullname));
                } else {
                    results.add(new Datafile(fullname));
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<DirComponent> getRecursive() {
        ArrayList<DirComponent> results = new ArrayList();
        if (!exists()) {
            for (DirComponent c : wildcardIterator()) {
                results.add(c);
                if (c.existsDir()) {
                    results.addAll(((FSPath) c).getRecursive());
                }
            }
        } else {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (file.isDirectory()) {
                    FSPath dir = new FSPath(fullname);
                    results.add(dir);
                    results.addAll(dir.getRecursive());
                } else {
                    results.add(new Datafile(fullname));
                }
            }
        }
        return results;
    }

    public ArrayList<DirComponent> get(String regexstring) {
        ByteSearch pattern = ByteSearch.create(regexstring);
        return get(pattern);
    }

    public ArrayList<DirComponent> get(ByteSearch pattern) {
        ArrayList<DirComponent> results = new ArrayList();
        for (DirComponent c : get()) {
            if (pattern.exists(c.getName())) {
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

    @Override
    public ArrayList<String> getFilenames() {
        ArrayList<String> results = new ArrayList();
        for (Datafile df : getFiles()) {
            results.add(df.getName());
        }
        return results;
    }

    public ArrayList<String> getFilepathnames() {
        ArrayList<String> results = new ArrayList();
        for (Datafile df : getFiles()) {
            results.add(df.getCanonicalPath());
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles(ByteSearch pattern) {
        ArrayList<Datafile> results = new ArrayList();
        for (Datafile df : getFiles()) {
            if (pattern.exists(df.getName())) {
                results.add(df);
            }
        }
        return results;
    }

    @Override
    public FSPath getParentPath() {
        String path = toString();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 1) {
            if (path.startsWith("/")) {
                return new FSPath("/");
            }
            return new FSPath("~");
        }
        String parent = path.substring(0, lastSlash);
        return new FSPath(parent);
    }

    @Override
    public ArrayList<HPath> getDirs() {
        ArrayList<HPath> results = new ArrayList();
        for (String f : list()) {
            String fullname = getFilename(f);
            File file = new File(fullname);
            if (file.isDirectory()) {
                results.add(new FSPath(fullname));
            }
        }
        return results;
    }

    @Override
    public ArrayList<String> getDirnames() {
        ArrayList<String> results = new ArrayList();
        for (String f : list()) {
            String fullname = getFilename(f);
            File file = new File(fullname);
            if (file.isDirectory()) {
                results.add(fullname);
            }
        }
        return results;
    }

    @Override
    public ArrayList<FSPath> getDirs(ByteSearch pattern) {
        ArrayList<FSPath> results = new ArrayList();
        for (String name : list()) {
            if (pattern.exists(name)) {
                String fullname = getFilename(name);
                File file = new File(fullname);
                if (file.isDirectory()) {
                    results.add(new FSPath(fullname));
                }
            }
        }
        return results;
    }

    private ArrayList<String> getFilenames(ByteSearch pattern) throws IOException {
        ArrayList<String> results = new ArrayList();
        for (Datafile df : getFiles(pattern)) {
            results.add(df.getName());
        }
        return results;
    }

    @Override
    public void remove() throws IOException {
        this.delete();
    }

    @Override
    public Iterator<Datafile> iteratorFiles(ByteSearch pattern) {
        return new ListIterator(getFiles(pattern));
    }

}
