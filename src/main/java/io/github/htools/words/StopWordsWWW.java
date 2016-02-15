package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.HashSet;

/**
 * Stop word that compensate for tokenized url's, which do not work well with
 * PRF
 */
public class StopWordsWWW {

    public static Log log = new Log(StopWordsWWW.class);

    public static HashSet<String> unstemmedFilterSet = StopWords.getWords("www");

    public static HashSet<String> getUnstemmedFilterSet() {
        return unstemmedFilterSet;
    }
}
