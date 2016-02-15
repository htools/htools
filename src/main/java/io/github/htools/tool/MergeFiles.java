package io.github.htools.tool;

import io.github.htools.hadoop.Conf;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;

import java.io.IOException;

/**
 * Created by iloen on 28-01-16.
 */
public class MergeFiles {
    public static void main(String[] args) throws IOException {
        Conf conf = new Conf(args, "input");
        HDFSPath out = new HDFSPath(conf, conf.get("input"));
        Datafile f = new Datafile(conf, conf.get("input") + ".file");
        HDFSPath.mergeFiles(f, out.getFiles());
        out.trash();
        f.move(new Datafile(conf, conf.get("input")));
    }

}
