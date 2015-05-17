package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.search.ByteSearchSection;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlScript extends RemoveNestedTagsQuoteSafe {

    public static Log log = new Log(RemoveHtmlScript.class);

    private RemoveHtmlScript(Extractor extractor, String process) {
        super(extractor, process, "<script[^A-za-z]", "<\\\\?\\s*/\\s*script\\s*>");
    }
}
