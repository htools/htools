package io.github.htools.words;

import io.github.htools.extract.DefaultTokenizer;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * List of stop words, which is not stored as a feature, but rather configured.
 *
 * @author jeroen
 */
public class StopWordsMultiLang {

    public static Log log = new Log(StopWordsMultiLang.class);
    public static StopWordsMultiLang singleton;
    public static HashSet<String> unstemmedfilterset;
    private static HashSet<String> stemmedfilterset;
    private static HashSet<String> lancasterstemmedfilterset;

    protected StopWordsMultiLang(Configuration conf) {
        for (String s : conf.getStrings("retriever.stopword", new String[0])) {
            this.getUnstemmedFilterSet().add(s);
        }
    }
    
    protected StopWordsMultiLang() {
    }
    
    public static StopWordsMultiLang get(Configuration conf) {
        if (singleton == null) {
            singleton = new StopWordsMultiLang(conf);
        }
        return singleton;
    }

    public static StopWordsMultiLang get() {
        if (singleton == null) {
            singleton = new StopWordsMultiLang();
        }
        return singleton;
    }

    public void addNumbers() {
        for (int i = 0; i < 10; i++) {
            String n = "" + i;
            this.getUnstemmedFilterSet().add(n);
        }
    }

    public static HashSet<String> getStemmedFilterSet() {
        if (stemmedfilterset == null) {
            DefaultTokenizer extractor = new DefaultTokenizer();
            stemmedfilterset = new HashSet<String>();
            for (String s : getUnstemmedFilterSet()) {
                ArrayList<String> tokenize = extractor.tokenize(s);
                if (tokenize.size() == 1) {
                    stemmedfilterset.add(tokenize.get(0));
                }
            }
        }
        return stemmedfilterset;
    }

    public static HashSet<String> getLancasterStemmedFilterSet() {
        if (lancasterstemmedfilterset == null) {
            LancasterStemmer stemmer = LancasterStemmer.get();
            lancasterstemmedfilterset = new HashSet<String>();
            for (String s : getUnstemmedFilterSet()) {
                    lancasterstemmedfilterset.add(stemmer.stem(s));
            }
        }
        return lancasterstemmedfilterset;
    }

    public static HashSet<String> getUnstemmedFilterSet() {
        if (unstemmedfilterset == null) {
            unstemmedfilterset = new HashSet<String>();
            unstemmedfilterset.addAll(StopWordsSmart.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsContractions.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsLetter.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsSpanish.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsFrench.getUnstemmedFilterSet());
            unstemmedfilterset.addAll(StopWordsGerman.getUnstemmedFilterSet());
        }
        return unstemmedfilterset;
    }

    public static boolean isUnstemmedStopWord(String s) {
        return getUnstemmedFilterSet().contains(s);
    }

    public boolean isStemmedStopWord(String s) {
        return this.getStemmedFilterSet().contains(s);
    }
}
