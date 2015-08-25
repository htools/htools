package io.github.htools.hadoop.io.backup;

import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 *
 * @author jeroen
 */
public class ConfigurableModifier implements PathModifier {

    public static Log log = new Log(ConfigurableModifier.class);
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
        if (search == null) {
            search = conf.get(SEARCH);
            replace = conf.get(REPLACE);
        }
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public ConfigurableModifier() {
    }

    public ConfigurableModifier(Configuration conf, String search, String replace) {
        setSearchReplace(conf, search, replace);
        setConf(conf);
    }

    @Override
    public String modify(String input) {
        return input.replaceFirst(search, replace);
    }
}
