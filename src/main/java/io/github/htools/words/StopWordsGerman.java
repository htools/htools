package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.HashSet;

/**
 * Stop word list of 429 terms from
 * https://code.google.com/p/stop-words/ .
 */
public class StopWordsGerman {

    public static Log log = new Log(StopWordsGerman.class);

    public static HashSet<String> unstemmedFilterSet = StopWords.getWords("de");

    public static HashSet<String> getUnstemmedFilterSet() {
        return unstemmedFilterSet;
    }
}
