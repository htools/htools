package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Extractor;

public abstract class ExtractorPreProcessor extends ExtractorProcessor {
   
   public ExtractorPreProcessor(Extractor extractor) {
      super(extractor, null);
   }
}
