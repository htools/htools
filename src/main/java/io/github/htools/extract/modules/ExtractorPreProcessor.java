package io.github.htools.extract.modules;

import io.github.htools.extract.Extractor;

public abstract class ExtractorPreProcessor extends ExtractorProcessor {
   
   public ExtractorPreProcessor(Extractor extractor) {
      super(extractor, null);
   }
}
