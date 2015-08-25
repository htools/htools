package io.github.htools.io.struct;

import io.github.htools.lib.Log;

/**
 * Strcutures data in a tab-delimited file for processing with Pig.
 * <p>
 * @author jeroen
 */
public abstract class StructuredTextPigTuple<F extends StructuredTextPig> {

   public static Log log = new Log(StructuredTextPigTuple.class);
   
   public abstract void write(F file);
}
