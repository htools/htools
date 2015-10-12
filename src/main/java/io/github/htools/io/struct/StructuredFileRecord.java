package io.github.htools.io.struct;

import io.github.htools.lib.Log;
import java.io.IOException;

public interface StructuredFileRecord<F extends StructuredRecordFile> {
   public static Log log = new Log(StructuredFileRecord.class);

   public abstract void read( F file ) throws IOException;
   
   public abstract void write( F file ) throws IOException;
}
