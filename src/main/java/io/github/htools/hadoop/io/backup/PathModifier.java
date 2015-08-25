package io.github.htools.hadoop.io.backup;

import org.apache.hadoop.conf.Configurable;

/**
 *
 * @author jeroen
 */
public interface PathModifier extends Configurable {
   public String modify(String input);

}
