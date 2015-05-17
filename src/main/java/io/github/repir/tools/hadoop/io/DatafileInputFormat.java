package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.collection.HashMapList;
import io.github.repir.tools.hadoop.Job;
import static io.github.repir.tools.hadoop.io.ConstInputFormat.map;
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

public class DatafileInputFormat extends ConstInputFormat<String, String> {

    public static Log log = new Log(DatafileInputFormat.class);

    @Override
    protected MRInputSplit<String, String> createSplit(String key) {
        return new StringStringInputSplit(key);
    }

    public static void add(Job job, Collection<HDFSPath> path) {
        HashMapList<String, String> distributeFiles = HDFSPath.distributePath(HDFSPath.getFS(job.getConfiguration()), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                add(job, entry.getKey(), file);
            }
        }
    }

    public static void addFiles(Job job, Collection<Datafile> path) throws IOException {
        HashMapList<String, String> distributeFiles = HDFSPath.distributeDatafiles(HDFSPath.getFS(job.getConfiguration()), path);
        for (Map.Entry<String, ArrayList<String>> entry : distributeFiles.entrySet()) {
            for (String file : entry.getValue()) {
                add(job, entry.getKey(), file);
            }
        }
    }

    @Override
    public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
        ArrayList<InputSplit> splits = new ArrayList();
        if (cansplit) {
            for (Map.Entry<Object, MRInputSplit> s : map.entrySet()) {
                int splitsneeded = s.getValue().size() / 50 + 1;
                if (splitsneeded == 1) {
                    s.getValue().hosts = new String[]{(String) s.getKey()};
                } else {
                    int splitsize = s.getValue().size() / splitsneeded;
                    int p = 0;
                    for (int i = 0; i < splitsneeded; i++) {
                        MRInputSplit<String, String> newsplit = createSplit((String) s.getKey());
                        for (; p < i * splitsize && p < s.getValue().size(); p++) {
                            newsplit.add((String) s.getValue().get(p));
                        }
                        newsplit.hosts = new String[]{(String) s.getKey()};
                        splits.add(newsplit);
                    }
                }
            }
        } else {
            MRInputSplit singlesplit = createSplit("");
            for (MRInputSplit s : map.values()) {
                singlesplit.addAll(s.list);
            }
            splits.add(singlesplit);
        }
        return splits;
    }
}
