package io.github.repir.tools.hadoop.io.backup;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Conf;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.DirComponent;
import io.github.repir.tools.io.HDFSPath;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author jeroen
 */
public class BackupJob {

    private static final Log log = new Log(BackupJob.class);

    public static void main(String[] args) throws Exception {

        Conf conf = new Conf(args, "-s source -d dest");
        conf.setQueue("express");
        HDFSPath in = new HDFSPath(conf, conf.get("source"));
        HDFSPath out = new HDFSPath(conf, conf.get("dest"));
        backup(conf, in, out);
    }
    
    public static void backup(Conf conf, HDFSPath source, HDFSPath dest) throws IOException, ClassNotFoundException, InterruptedException {
        ArrayList<Datafile> fileList = fileList(new ArrayList(), source);
        if (fileList.size() > 0) {
           CopyJob.copyDatafiles(conf, source.getCanonicalPath(), dest.getCanonicalPath(), fileList);
        }
    }
    
    public static ArrayList<Datafile> fileList(ArrayList<Datafile> files, HDFSPath source) throws IOException {
        for (DirComponent c : source.get()) {
            if (c instanceof Datafile)
                files.add((Datafile)c);
            else if (c instanceof HDFSPath)
                fileList(files, (HDFSPath)c);
        }
        return files;
    }
}
