package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.HashSet;

/**
 * Stop word list of 429 terms from
 * http://www.lextek.com/manuals/onix/stopwords1.html which is the original list
 * of stop words Salton &amp; Buckley orginally used for the SMART system at
 * Cornell University, which was slightly trimmed down.
 */
public class StopWordsSmart {

    public static Log log = new Log(StopWordsSmart.class);
    static HashSet<String> stemmedFilterSet;
    
    public static HashSet<String> unstemmedFilterSet = StopWords.getWords("smart");

    public static HashSet<String> getUnstemmedFilterSet() {
        return unstemmedFilterSet;
    }
    
    public static HashSet<String> getStemmedFilterSet() {
        if (stemmedFilterSet == null) {
            stemmedFilterSet = StopWords.getStemmedFilterSet(unstemmedFilterSet);
        }
        return stemmedFilterSet;
    }
}
