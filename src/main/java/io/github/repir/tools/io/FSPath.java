package io.github.repir.tools.io;

import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.collection.ListIterator;
import static io.github.repir.tools.lib.Const.NULLINT;
import static io.github.repir.tools.lib.Const.NULLLONG;
import io.github.repir.tools.lib.Log;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.hadoop.io.IOUtils;

/**
 * The Dir class represents a directory of files, and contains many methods to
 * access Files within the directory as FSFile
 * <p/>
 * @author jbpvuurens
 */
public class FSPath extends File implements Path {

    public static Log log = new Log(FSPath.class);

    /**
     * Constructs a Dir object that uses the provided path
     * <p/>
     * @param directorypath
     */
    public FSPath(String directorypath) {
        super(directorypath);
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

    @Override
    public String getCanonicalPath() {
        try {
            return super.getCanonicalPath();
        } catch (IOException ex) {
            log.fatalexception(ex, "getCanonicalPath()");
        }
        return null;
    }

    public static void copy(String path1, String path2) throws IOException {
        java.nio.file.Files.copy( 
                new java.io.File(path1).toPath(),
                new java.io.File(path2).toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
                java.nio.file.LinkOption.NOFOLLOW_LINKS );
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
     * <p/>
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
    public ListIterator<DirComponent> iterator() {
        return new ListIterator(get());
    }

    public ListIterator<DirComponent> iteratorRecursive() {
        return new ListIterator(getRecursive());
    }

    public ListIterator<DirComponent> iterator(String regexstring) {
        return new ListIterator(get(regexstring));
    }

    public ListIterator<DirComponent> iteratorDirs() {
        return new ListIterator(FSPath.this.getDirs());
    }

    public ListIterator<DirComponent> iteratorFiles() {
        return new ListIterator(getFiles());
    }

    public ListIterator<DirComponent> iteratorDirs(String regexstring) {
        return new ListIterator(getDirs(regexstring));
    }

    public ListIterator<DirComponent> iteratorFiles(String regexstring) {
        return new ListIterator(FSPath.this.getFiles(regexstring));
    }

    public ArrayList<DirComponent> get() {
        ArrayList<DirComponent> results = new ArrayList();
        for (String f : list()) {
            String fullname = getFilename(f);
            File file = new File(fullname);
            if (file.isDirectory()) {
                results.add(new FSPath(fullname));
            } else {
                results.add(new Datafile(fullname));
            }
        }
        return results;
    }

    public ArrayList<DirComponent> getRecursive() {
        ArrayList<DirComponent> results = new ArrayList();
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
        return results;
    }

    public ArrayList<DirComponent> get(String regexstring) {
        ByteSearch pattern = ByteSearch.create(regexstring);
        ArrayList<DirComponent> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                if (pattern.exists(f)) {
                    String fullname = getFilename(f);
                    File file = new File(fullname);
                    if (file.isDirectory()) {
                        results.add(new FSPath(fullname));
                    } else {
                        results.add(new Datafile(fullname));
                    }
                }
            }
        } else if (isFile()) {
            results.add(new Datafile(this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFiles() {
        ArrayList<Datafile> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (!file.isDirectory()) {
                    results.add(new Datafile(fullname));
                }
            }
        } else if (isFile()) {
            results.add(new Datafile(this.getCanonicalPath()));
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFilesNewerThan(long lastupdate) {
        ArrayList<Datafile> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (!file.isDirectory() && file.lastModified() >= lastupdate) {
                    results.add(new Datafile(fullname));
                }
            }
        } else if (isFile()) {
            results.add(new Datafile(this.getCanonicalPath()));
        }
        return results;
    }

    public ArrayList<String> getFilenames() {
        ArrayList<String> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (!file.isDirectory()) {
                    results.add(f);
                }
            }
        } else if (isFile()) {
            results.add(this.getName());
        }
        return results;
    }

    public ArrayList<String> getFilepathnames() {
        ArrayList<String> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (!file.isDirectory()) {
                    results.add(fullname);
                }
            }
        } else if (isFile()) {
            results.add(this.getCanonicalPath());
        }
        return results;
    }

    public ArrayList<Datafile> getFiles(String regexstring) {
        ByteSearch pattern = ByteSearch.create(regexstring);
        ArrayList<Datafile> results = new ArrayList();
        if (isDirectory()) {
            for (String f : list()) {
                if (pattern.exists(f)) {
                    String fullname = getFilename(f);
                    File file = new File(fullname);
                    if (!file.isDirectory()) {
                        results.add(new Datafile(fullname));
                    }
                }
            }
        } else if (isFile() && pattern.exists(getName())) {
            results.add(new Datafile(this.getCanonicalPath()));
        }

        return results;
    }

    public ArrayList<Path> getDirs() {
        ArrayList<Path> results = new ArrayList();
        for (String f : list()) {
            String fullname = getFilename(f);
            File file = new File(fullname);
            if (file.isDirectory()) {
                results.add(new FSPath(fullname));
            }
        }
        return results;
    }

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

    public ArrayList<Path> getDirs(String regexstring) {
        ByteSearch pattern = ByteSearch.create(regexstring);
        ArrayList<Path> results = new ArrayList();
        for (String f : list()) {
            if (pattern.exists(f)) {
                String fullname = getFilename(f);
                File file = new File(fullname);
                if (file.isDirectory()) {
                    results.add(new FSPath(fullname));
                }
            }
        }
        return results;
    }

    @Override
    public ArrayList<Datafile> getFilesStartingWith(String start) {
        ArrayList<Datafile> results = new ArrayList();
        if (isDirectory()) {
            for (String name : list()) {
                if (name.startsWith(start)) {
                    String fullname = getFilename(name);
                    File file = new File(fullname);
                    if (!file.isDirectory()) {
                        results.add(new Datafile(fullname));
                    }
                }
            }
        } else if (isFile() && getName().startsWith(start)) {
            results.add(new Datafile(this.getCanonicalPath()));
        }

        return results;
    }

    public ArrayList<String> getFilenames(String regexstring) throws IOException {
        return getFilenames(ByteSearch.createFilePattern(regexstring));
    }

    private ArrayList<String> getFilenames(ByteSearch pattern) throws IOException {
        ArrayList<String> results = new ArrayList();
        if (this.isDirectory()) {
            for (String name : this.getFilenames()) {
                if (pattern.match(name)) {
                    results.add(name);
                }
            }
        } else if (this.isFile() && pattern.match(getName())) {
            results.add(getName());
        }
        return results;
    }

    @Override
    public void remove() throws IOException {
       this.delete();
    }

}
