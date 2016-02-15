package io.github.htools.extract;

import io.github.htools.extract.modules.SectionMarker;
import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;

/**
 * The Extractor is a generic processor that converts content submitted
 * by an {@link Reader} into extracted values to store as features.
 * <p>
 * Extraction proceeds in 3 phases. (1) the raw byte content of the
 * content is pre-processed by the modules configured as
 * "extractor.preprocess". Typical operations are converting tagnames to
 * lowercase, converting unicodes to ASCII, and removing irrelevant parts, to
 * simplify further processing. (2) mark sections in the content using the
 * modules configured with "extractor.sectionmarker". One default section is
 * "all" to indicate all content. Other {@link SectionMarker}s can process an
 * existing section, to mark subsections. (3) each section can have its own (set
 * of) processing pipeline(s), configured with "extractor.sectionprocess". For
 * the marked sections the modules configured with "extractor.&lt;processname&gt;" are
 * performed sequentially.
 *
 * @author jeroen
 */
public class ExtractorConf extends Extractor {

    public static Log log = new Log(ExtractorConf.class);
    public Configuration conf;

    /**
     * Creates an extractor that must be configured with calls to
     * addPreProcessor, addSectionProcess, addSectionMarker and addProcess.
     * Components cannot use the non-existing Configuration, and therefore must
     * provide a constructor that allows to set the required parameters.
     */
    public ExtractorConf() {
        this(new Configuration());
    }

    /**
     * Creates an extractor using the configuration settings.
     *
     * @param configuration
     */
    public ExtractorConf(Configuration configuration) {
        conf = configuration;
        init();
    }

    void init() {
        for (String p : conf.getStrings("extractor.preprocess", new String[0])) {
            Class clazz = stringToClass(p);
            addPreProcessor(clazz);
        }
        for (String p : conf.getStrings("extractor.sectionprocess", new String[0])) {
            String part[] = p.split(" +");
            addSectionProcess(part[0], part[1], (part.length > 2) ? part[2] : null);
        }
        for (String sectionmarker : conf.getStrings("extractor.sectionmarker", new String[0])) {
            String part[] = sectionmarker.split(" +");
            addSectionMarker(part[2], part[0], part[1]);
        }
        for (String process : processes) {
            createProcess(process);
        }
    }

    public String getConfigurationString(String process, String identifier, String defaultstring) {
        return conf.get("extractor." + process + "." + identifier, defaultstring);
    }

    public String[] getConfigurationStrings(String process, String identifier, String defaultstring[]) {
        return conf.getStrings("extractor." + process + "." + identifier, defaultstring);
    }

    public boolean getConfigurationBoolean(String process, String identifier, boolean defaultboolean) {
        return conf.getBoolean("extractor." + process + "." + identifier, defaultboolean);
    }

    public int getConfigurationInt(String process, String identifier, int defaultint) {
        return conf.getInt("extractor." + process + "." + identifier, defaultint);
    }

    public float getConfigurationFloat(String process, String identifier, float defaultfloat) {
        return conf.getFloat("extractor." + process + "." + identifier, defaultfloat);
    }

    void createProcess(String process) {
        for (String processor : conf.getStrings("extractor." + process, new String[]{process})) {
            Class clazz = stringToClass(processor);
            addProcess(process, clazz);
            //log.info("createProcess %s %s", process, processor);
        }
    }
}
