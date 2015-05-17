package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;

public class StringInputFormat extends ConstInputFormat<String, String> {
   public static Log log = new Log(StringInputFormat.class);

    @Override
    protected MRInputSplit<String, String> createSplit(String key) {
        return new StringStringInputSplit(key);
    }
   
}
