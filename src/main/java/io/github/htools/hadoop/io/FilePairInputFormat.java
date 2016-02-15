package io.github.htools.hadoop.io;

import io.github.htools.collection.HashMapList;
import io.github.htools.hadoop.Job;
import io.github.htools.hadoop.io.backup.PathModifier;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.htools.lib.Log;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FilePairInputFormat extends KVInputFormat<String, String> {

    public static Log log = new Log(FilePairInputFormat.class);

    @Override
    protected KVInputSplit<String, String> createSplit() {
        return new StringPairInputSplit();
    }

    public static void addPaths(Job job, PathModifier modifier, Collection<HDFSPath> path) throws IOException {
        HashMapList<String, String> distributeFiles = HDFSPath.distributePath(job.getFileSystem(), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                //log.info("%s %s", entry.getKey(), file);
                add(job, entry.getKey(), file, modifier.modify(file));
            }
        }
    }

    public static void addPaths(Job job, Datafile df, Datafile df2) throws IOException {
        add(job, "" + FilePairInputFormat.size(), df.getCanonicalPath(), df2.getCanonicalPath());
    }

    public static void addDatafiles(Job job, PathModifier modifier, Collection<Datafile> path) throws IOException {
        modifier.setConf(job.getConfiguration());
        HashMapList<String, String> distributeFiles = HDFSPath.distributeDatafiles(job.getFileSystem(), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                add(job, entry.getKey(), file, modifier.modify(file));
            }
        }
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        if (cansplit) {
            ArrayList<InputSplit> splits = new ArrayList();
            for (Map.Entry<Object, KVInputSplit> s : map.entrySet()) {
                int splitsneeded = (s.getValue().size() / 49) + 1;
                if (splitsneeded == 1) {
                    s.getValue().hosts = new String[]{(String) s.getKey()};
                    splits.add(s.getValue());
                } else {
                    ArrayList<KVInputSplit<String, String>> newsplits = new ArrayList();
                    for (int i = 0; i < splitsneeded; i++) {
                        KVInputSplit<String, String> newsplit = createSplit();
                        newsplit.hosts = new String[]{(String) s.getKey()};
                        newsplits.add(newsplit);
                        splits.add(newsplit);
                    }
                    for (int i = 0; i < s.getValue().size(); i++) {
                        Map.Entry<String, String> pair = s.getValue().get(i);
                        newsplits.get(i % splitsneeded).add(pair.getKey(), pair.getValue());
                    }
                }
            }
            return splits;
        } else {
            return super.getSplits(context);
        }

    }

    private static void add(Job job, String host, String key, String value) {
        KVInputSplit currentsplit = getSplit(host);
        if (currentsplit == null) {
            currentsplit = getInputFormat(job).createSplit();
            putSplit(host, currentsplit);
        }
        currentsplit.add(key, value);
    }
}
