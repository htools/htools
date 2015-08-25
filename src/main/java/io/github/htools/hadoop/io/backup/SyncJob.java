package io.github.htools.hadoop.io.backup;

import io.github.htools.lib.Log;
import io.github.htools.hadoop.Conf;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author jeroen
 */
public class SyncJob {

    private static final Log log = new Log(SyncJob.class);

    public static void main(String[] args) throws Exception {

        Conf conf = new Conf(args, "-s source -d dest");
        conf.setQueue("express");
        HDFSPath in = new HDFSPath(conf, conf.get("source"));
        HDFSPath out = new HDFSPath(conf, conf.get("dest"));
        backup(conf, in, out);
    }
    
    public static void backup(Conf conf, HDFSPath source, HDFSPath dest) throws IOException, ClassNotFoundException, InterruptedException {
        ArrayList<Datafile> fileList = fileList(new ArrayList(), source, dest);
        if (fileList.size() > 0) {
           UpdateJob.copyDatafiles(conf, source.toString(), source.toString(), fileList);
        }
    }
    
    public static ArrayList<Datafile> fileList(ArrayList<Datafile> files, HDFSPath source, HDFSPath dest) throws IOException {
        files.addAll(source.getFilesNewerThan(dest));
        for (Datafile df : dest.getFilesNonExist(source)) {
            df.trash();
        }
        for (HDFSPath p : source.getDirs()) {
            fileList(files, p, dest.getSubdir(p.getName()));
        }
        return files;
    }
}
