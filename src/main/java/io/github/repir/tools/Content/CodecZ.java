package io.github.repir.tools.Content;

import org.apache.hadoop.io.compress.GzipCodec;

public class CodecZ extends GzipCodec {

   public String getDefaultExtension() {
      return ".z";
   }
}
