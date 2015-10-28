package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveFilteredWords extends ExtractorProcessor {
    
    private static Log log = new Log(RemoveFilteredWords.class);
    HashSet<String> words = new HashSet<String>();
    
    public RemoveFilteredWords(Extractor extractor, String process) {
        super(extractor, process);
    }
    
    public RemoveFilteredWords(Extractor extractor, Collection<String> words) {
        super(extractor, null);
        addWords(words);
    }
    
    public void addWords(Collection<String> words) {
        this.words.addAll(words);
    }
    
    @Override
    public void process(Content entity, ByteSearchSection pos, String attributename) {
        //log.fatal("process channel %s %d", channel.channel, channel.size());
        ExtractChannel attribute = entity.get(attributename);
        ArrayList<String> keep = new ArrayList(attribute.size());
        for (String word : attribute) {
            if (!words.contains(word)) {
                keep.add(word);
            }
        }
        attribute.set(keep);
    }
    
    public ArrayList<String> process(ArrayList<String> list) {
        ArrayList<String> keep = new ArrayList(list.size());
        for (String word : list) {
            if (!words.contains(word)) {
                keep.add(word);
            }
        }
        return keep;
    }
}
