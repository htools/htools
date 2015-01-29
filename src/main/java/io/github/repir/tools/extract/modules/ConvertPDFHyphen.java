package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import java.util.ArrayList;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -> usa). Other dots are replaced by
 * spaces.
 * <p/>
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
