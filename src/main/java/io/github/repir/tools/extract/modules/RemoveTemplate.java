package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.RemovedException;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

/**
 * Removes redirect pages from Wikipedia XML source.
 * <p/>
 * @author jeroen
 */
public class RemoveTemplate extends ExtractorProcessor {

    public static Log log = new Log(RemoveTemplate.class);
    public ByteSection title = ByteSection.create("<title>", "</title\\s*>");
    public ByteRegex template = new ByteRegex("\\s*Template:");

    private RemoveTemplate(Extractor extractor, String process) {
        super(extractor, process);
    }

    @Override
    public void process(Content entity, ByteSearchSection section, String attribute) throws RemovedException {
        ByteSearchSection titlesection = title.findPos(section);
        if (titlesection.found() && template.match(section)) {
            log.info("remove template %s", titlesection.toString());
            throw new RemovedException();
        }
    }
}
