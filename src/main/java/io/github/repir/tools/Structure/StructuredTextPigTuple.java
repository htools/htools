package io.github.repir.tools.Structure;

import io.github.repir.tools.Lib.Log;

/**
 * Strcutures data in a tab-delimited file for processing with Pig.
 * <p/>
 * @author jeroen
 */
public abstract class StructuredTextPigTuple<F extends StructuredTextPig> {

   public static Log log = new Log(StructuredTextPigTuple.class);
   
   public abstract void write(F file);
}
