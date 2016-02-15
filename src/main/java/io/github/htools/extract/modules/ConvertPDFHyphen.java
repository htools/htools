package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * 
 * @author jbpvuurens
 */
public class ConvertPDFHyphen extends ExtractorProcessor {

    public static Log log = new Log(ConvertPDFHyphen.class);
    ByteRegex hyphen = new ByteRegex("\\C\\-\\s*\\n\\s*[a-z]");

    public ConvertPDFHyphen(Extractor extractor, String process) {
        super(extractor, process);
    }

    public void process(Content entity, ByteSearchSection pos, String attribute) {
        ArrayList<ByteSearchPosition> positions = hyphen.findAllPos(entity.content, pos.innerstart, pos.innerend);
        for (ByteSearchPosition p : positions) {
            for (int i = p.start; i < p.end && entity.content[i] < 'A'; i++) {
                entity.content[i] = 0;
            }
        }
    }
}
