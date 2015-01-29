package io.github.repir.tools.Words;

import io.github.repir.tools.lib.Log;
import java.util.HashSet;
import io.github.repir.tools.extract.DefaultTokenizer;
import java.util.ArrayList;
import org.apache.hadoop.conf.Configuration;

/**
 * List of stop words, which is not stored as a feature, but rather configured.
 *
 * @author jeroen
 */
public class StopWords {

    public static Log log = new Log(StopWords.class);
    public static StopWords singleton;
    public static HashSet<String> unstemmedfilterset;
    private static HashSet<String> stemmedfilterset;

    protected StopWords(Configuration conf) {
        for (String s : conf.getStrings("retriever.stopword", new String[0])) {
            getUnstemmedFilterSet().add(s);
        }
    }
    
    public static StopWords get(Configuration conf) {
        if (singleton == null) {
            singleton = new StopWords(conf);
        }
        return singleton;
    }

    public void addNumbers() {
        for (int i = 0; i < 10; i++) {
            String n = "" + i;
            getUnstemmedFilterSet().add(n);
        }
        stemmedfilterset = null;
    }

    public HashSet<String> getStemmedFilterSet() {
        if (stemmedfilterset == null) {
            DefaultTokenizer extractor = new DefaultTokenizer();
            stemmedfilterset = new HashSet<String>();
            for (String s : this.getUnstemmedFilterSet()) {
                ArrayList<String> tokenize = extractor.tokenize(s);
                if (tokenize.size() == 1) {
                    stemmedfilterset.add(tokenize.get(0));
                }
            }
        }
        return stemmedfilterset;
    }

    public HashSet<String> getUnstemmedFilterSet() {
        if (unstemmedfilterset == null) {
            unstemmedfilterset = new HashSet<String>();
            unstemmedfilterset.addAll(StopWordsSmart.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsLetter.getUnstemmedFilterSet());
        }
        return unstemmedfilterset;
    }

    public boolean isUnstemmedStopWord(String s) {
        return getUnstemmedFilterSet().contains(s);
    }

    public boolean isStemmedStopWord(String s) {
        return getStemmedFilterSet().contains(s);
    }
}
