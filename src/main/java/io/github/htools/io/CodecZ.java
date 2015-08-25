package io.github.htools.io;

import org.apache.hadoop.io.compress.GzipCodec;

public class CodecZ extends GzipCodec {

   public String getDefaultExtension() {
      return ".z";
   }
}
