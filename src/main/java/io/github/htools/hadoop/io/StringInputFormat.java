package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;

public class StringInputFormat extends ConstInputFormat<String, String> {
   public static Log log = new Log(StringInputFormat.class);

    @Override
    protected MRInputSplit<String, String> createSplit(String key) {
        return new StringStringInputSplit(key);
    }
   
}
