package io.github.repir.tools.hadoop.io.backup;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author jeroen
 */
public class ConfigurableModifier implements PathModifier {
    private static final String SEARCH = "configurablemodifier.search";
    private static final String REPLACE = "configurablemodifier.replace";
    Configuration conf;
    String search;
    String replace;
    
    public static void setSearchReplace(Configuration conf, String search, String replace) {
        conf.set(SEARCH, search);
        conf.set(REPLACE, replace);
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        search = conf.get(SEARCH);
        replace = conf.get(REPLACE);
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public ConfigurableModifier() {}
    
    public ConfigurableModifier(String search, String replace) {
       this.search = search;
       this.replace = replace;
    }
    
    @Override
    public String modify(String input) {
        return input.replace(search, replace);
    }
}
