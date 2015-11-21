package io.github.htools.words;

import io.github.htools.extract.DefaultTokenizer;
import io.github.htools.io.Datafile;
import io.github.htools.io.ResourceDataIn;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Jeroen
 */
public enum StopWords {

    ;
        
    public static Log log = new Log(StopWords.class);
        
    private static Datafile getDatafile(String resourceFilename) {
        String path = "resources/" + resourceFilename;
        return new Datafile(new ResourceDataIn(StopWordsSmart.class, path));
    }

    protected static HashSet<String> getWords(String resourceFilename) {
        Datafile df = getDatafile(resourceFilename);
        byte data[] = df.readFully();
        String s = ByteTools.toString(data);
        //log.printf("%s", PrintTools.memoryDump(data));
        HashSet<String> words = new HashSet();
        for (String word : s.split("\\s+")) {
            words.add(word);
//            if (word.startsWith("celle-l")) {
//                log.info("%s", s);
//                log.info("%s", word);
//                log.info("%s", ArrayTools.toString(ByteTools.toBytes(word)));
//            }
        }
        return words;
    }

    protected static HashSet<String> getStemmedFilterSet(Collection<String> terms) {
        DefaultTokenizer extractor = new DefaultTokenizer();
        HashSet stemmedfilterset = new HashSet<String>();
        for (String s : terms) {
            ArrayList<String> tokenize = extractor.tokenize(s);
            if (tokenize.size() == 1) {
                stemmedfilterset.add(tokenize.get(0));
            }
        }
        return stemmedfilterset;
    }
}
