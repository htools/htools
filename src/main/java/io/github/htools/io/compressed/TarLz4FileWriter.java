package io.github.htools.io.compressed;

import io.github.htools.collection.OrderedQueueMap;
import io.github.htools.io.Datafile;
import io.github.htools.io.DirComponent;
import io.github.htools.io.FSPath;
import io.github.htools.lib.ArgsParser;
import java.io.IOException;
import io.github.htools.lib.Log;
import static io.github.htools.lib.PrintTools.sprintf;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

/**
 *
 * @author Jeroen
 */
public class TarLz4FileWriter extends ArchiveFileWriter {

    public static Log log = new Log(TarLz4FileWriter.class);
    private LZ4FrameOutputStream lz4Stream;
    ArchiveOutputStream tar;

    public TarLz4FileWriter(OutputStream os, int compressionlevel) throws IOException {
        super(os, compressionlevel);
    }

    @Override
    protected void initialize(BufferedOutputStream os, int compressionlevel) throws IOException {
        try {
            switch (compressionlevel) {
                case 9:
                    lz4Stream = new LZ4HCFrameOutputStream(os);
                    break;
                case 1:
                    lz4Stream = new LZ4FrameOutputStream(os);
                    break;
                default:
                    log.printf("warning only currently support compressionlevels 1 and 9 [%d]", compressionlevel);
                    lz4Stream = new LZ4FrameOutputStream(os);
            }
            tar = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, lz4Stream);
        } catch (ArchiveException ex) {
            log.fatalexception(ex, "initialize compressionlevel=%d", compressionlevel);
        }
    }

    @Override
    public void close() throws IOException {
        tar.close();
    }

    @Override
    public void write(File file) throws IOException {
        ArchiveEntry archiveEntry = tar.createArchiveEntry(file, file.getName());
        tar.putArchiveEntry(archiveEntry);
        FileInputStream inputStream = new FileInputStream(file);
        IOUtils.copy(inputStream, tar);
        inputStream.close();
        tar.closeArchiveEntry();
    }

    /**
     * compress the files into the configured number of parts, of roughly the same
     * file size (measured by uncompressed size). 
     * @param ap
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void packParts(ArgsParser ap) throws FileNotFoundException, IOException {
        // level 9 is high compression, 1 is fast compression
        int compressionlevel = ap.getBoolean("9") ? 9 : 1;
        int partCount = ap.getInt("parts");
        // get current root, and compute the prefix to strip from the filepaths
        FSPath root = new FSPath("");
        int rootpath = root.getCanonicalPath().length() > 1 ? root.getCanonicalPath().length() + 1 : 0;
                
        // get list of files to process, and divide over parts roughly the same size
        ArrayList<Datafile> files = getFiles(ap.getStrings("input"));
        OrderedQueueMap<Long, ArrayList<Datafile>> parts = new OrderedQueueMap();
        for (int i = 0; i < partCount; i++) {
            parts.add(0l, new ArrayList());
        }
        for (Datafile file : files) {
            File f = new File(file.getCanonicalPath().substring(rootpath));
            Map.Entry<Long, ArrayList<Datafile>> part = parts.poll();
            long newLength = part.getKey() + file.getLength();
            part.getValue().add(file);
            parts.add(newLength, part.getValue());
        }

        // variables to report progress
        long size = getTotalSize(files);
        int count = 0;
        long processed = 0;
        int partNumber = 0;
        int countlength = Integer.toString(files.size()).length();
        int partlength = (int)Math.log10(partCount);
        String progressString = "part [%" + partlength + "d/%d] file [%" + countlength + "d/%d] perc [%5.1f%%] %s";
        
        // per part, create an archive and write the files to it
        for (Map.Entry<Long, ArrayList<Datafile>> entry : parts) {
            ArrayList<Datafile> partFiles = entry.getValue();
            ArchiveFileWriter writer = ArchiveFileWriter.getWriter(sprintf(ap.get("output"), partNumber++), compressionlevel);
            for (Datafile file : partFiles) {
                File f = new File(file.getCanonicalPath().substring(rootpath));
                writer.write(f);
                processed += file.getLength();
                Log.progress(progressString,
                        partNumber,
                        partCount,
                        ++count,
                        files.size(),
                        100 * processed / (double) size,
                        f.getPath());
            }
            writer.close();
        }
        // to make sure the last progress line is kept
        Log.print("");
    }

    public static void pack(ArgsParser ap) throws FileNotFoundException, IOException {
        int compressionlevel = ap.getBoolean("9") ? 9 : 1;
        ArchiveFileWriter writer = ArchiveFileWriter.getWriter(ap.get("output"), compressionlevel);
        ArrayList<Datafile> files = getFiles(ap.getStrings("input"));
        long size = getTotalSize(files);
        int count = 0;
        long processed = 0;
        int countlength = Integer.toString(files.size()).length();
        FSPath root = new FSPath("");
        int rootpath = root.getCanonicalPath().length() > 1 ? root.getCanonicalPath().length() + 1 : 0;
        String progressString = "file [%" + countlength + "d/%d] perc [%5.1f%%] %s";
        for (Datafile file : files) {
            File f = new File(file.getCanonicalPath().substring(rootpath));
            writer.write(f);
            processed += file.getLength();
            Log.progress(progressString,
                    ++count,
                    files.size(),
                    100 * processed / (double) size,
                    f.getPath());
        }
        Log.print("");
        writer.close();
    }

    /**
     * @param inputPaths
     * @return a list of Datafiles found in the given inputPaths, or if no
     * inpuPaths are given a list is read from stdin.
     */
    private static ArrayList<Datafile> getFiles(String[] inputPaths) {
        ArrayList<Datafile> files = new ArrayList();
        if (inputPaths == null || inputPaths.length == 0) {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String file : line.split("\\s+")) {
                    files.add(new Datafile(file));
                }
            }
        } else {
            for (String input : inputPaths) {
                FSPath path = new FSPath(input);
                for (DirComponent c : path.iterator()) {
                    if (c instanceof Datafile) {
                        files.add((Datafile) c);
                    } else {
                        for (DirComponent cc : ((FSPath) c).iteratorFiles()) {
                            files.add((Datafile) cc);
                        }
                    }
                }
            }
        }
        return files;
    }

    /**
     * @param files
     * @return sum of the length of the given files.
     */
    private static long getTotalSize(ArrayList<Datafile> files) {
        long size = 0;
        for (Datafile file : files) {
            size += file.getLength();
        }
        return size;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        ArgsParser ap = new ArgsParser(args, "--9 -o output -i {input} -p [parts]");
        if (ap.exists("parts")) {
            packParts(ap);
        } else {
            pack(ap);
        }
    }

    @Override
    protected int getDefaultCompressionLevel() {
        return 9;
    }
}
