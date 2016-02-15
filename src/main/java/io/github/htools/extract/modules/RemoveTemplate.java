package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.RemovedException;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 * Removes redirect pages from Wikipedia XML source.
 * <p>
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
