package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.collection.HashMapList;
import io.github.repir.tools.hadoop.Job;
import io.github.repir.tools.hadoop.io.backup.PathModifier;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.tools.lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;

public class FilePairInputFormat extends KVInputFormat<String, String> {

    public static Log log = new Log(FilePairInputFormat.class);

    @Override
    protected KVInputSplit<String, String> createSplit() {
        return new StringPairInputSplit();
    }

    public static void addPaths(Job job, PathModifier modifier, Collection<HDFSPath> path) {
        HashMapList<String, String> distributeFiles = HDFSPath.distributePath(HDFSPath.getFS(job.getConfiguration()), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                //log.info("%s %s", entry.getKey(), file);
                add(job, entry.getKey(), file, modifier.modify(file));
            }
        }
    }

    public static void addDatafiles(Job job, PathModifier modifier, Collection<Datafile> path) {
        HashMapList<String, String> distributeFiles = HDFSPath.distributeDatafiles(HDFSPath.getFS(job.getConfiguration()), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                add(job, entry.getKey(), file, modifier.modify(file));
            }
        }
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
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
    }

    private static void add(Job job, String host, Object key, Object value) {
        KVInputSplit currentsplit = getSplit(host);
        if (currentsplit == null) {
            currentsplit = getInputFormat(job).createSplit();
            putSplit(host, currentsplit);
        }
        currentsplit.add(key, value);
    }
}
