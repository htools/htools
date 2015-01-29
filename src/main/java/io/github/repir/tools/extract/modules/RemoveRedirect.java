package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.RemovedException;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

/**
 * Removes redirect pages from Wikipedia XML source.
 * <p/>
 * @author jeroen
 */
public class RemoveRedirect extends ExtractorProcessor {

    public static Log log = new Log(RemoveRedirect.class);
    public ByteRegex text = new ByteRegex("<text(\\s|>)");
    public ByteRegex start = new ByteRegex("<redirect(\\s|/>)");

    private RemoveRedirect(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) throws RemovedException {
        ByteSearchPosition findPos = text.findPos(section);
        if (!findPos.found() || start.exists(entity.content, section.innerstart, findPos.start)) {
            if (findPos.found())
               log.info("remove Redirect %s", new String(section.haystack, section.innerstart, findPos.start - section.innerstart));
            throw new RemovedException();
        }
    }
}
