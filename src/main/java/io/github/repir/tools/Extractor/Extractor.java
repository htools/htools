package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Tools.ExtractorProcessor;
import io.github.repir.tools.Extractor.Tools.SectionMarker;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.hadoop.conf.Configuration;

/**
 * The Extractor is a generic processor that converts {@link Entity}s submitted
 * by an {@link EntityReader} into extracted values to store as features.
 * <p/>
 * Extraction proceeds in 3 phases. (1) the raw byte content of the
 * {@link Entity} is pre-processed by the modules configured as
 * "extractor.preprocess". Typical operations are converting tagnames to
 * lowercase, converting unicodes to ASCII, and removing irrelevant parts, to
 * simplify further processing. (2) mark sections in the content using the
 * modules configured with "extractor.sectionmarker". One default section is
 * "all" to indicate all content. Other {@link SectionMarker}s can process an
 * existing section, to mark subsections. (3) each section can have its own (set
 * of) processing pipeline(s), configured with "extractor.sectionprocess". For
 * the marked sections the modules configured with "extractor.<processname>" are
 * performed sequentially.
 *
 * @author jeroen
 */
public class Extractor {

    public static Log log = new Log(Extractor.class);
    public Configuration conf;
    private boolean neverused = true;
    protected ArrayList<ExtractorProcessor> preprocess = new ArrayList();
    protected HashMap<String, ArrayList<ExtractorProcessor>> processor = new HashMap();
    protected HashSet<String> processes = new HashSet();
    protected ArrayList<String> inputsections = new ArrayList();
    protected ArrayList<String> allsections = new ArrayList();
    protected HashMap<String, ArrayList<SectionMarker>> sectionmarkers = new HashMap();
    protected ArrayList<ExtractorPatternMatcher> patternmatchers = new ArrayList();
    protected ArrayList<SectionMarker> markers = new ArrayList();
    protected ArrayList<SectionProcess> processors = new ArrayList();
    protected ByteRegex sectionstart;

    /**
     * Creates an extractor that must be configured with calls to
     * addPreProcessor, addSectionProcess, addSectionMarker and addProcess.
     * Components cannot use the non-existing Configuration, and therefore must
     * provide a constructor that allows to set the required parameters.
     */
    public Extractor() {
        conf = new Configuration();
    }

    /**
     * Creates an extractor using the configuration settings.
     *
     * @param configuration
     */
    public Extractor(Configuration configuration) {
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

    public void addPreProcessor(Class clazz) {
        this.preprocess.add(createUnboundProcessor("preprocess", clazz));
    }

    /**
     * Creates a processor that is not bound to any process, which may be used
     * by other processors to create sub-processors.
     *
     * @param clazz
     */
    public ExtractorProcessor createUnboundProcessor(String identifier, Class clazz) {
        Constructor c = ClassTools.getAssignableConstructor(clazz, ExtractorProcessor.class, Extractor.class, String.class);
        return (ExtractorProcessor) ClassTools.construct(c, this, identifier);
    }

    public ExtractorProcessor createUnboundProcessor(String identifier, String clazzname) {
        Class clazz = stringToClass(clazzname);
        return createUnboundProcessor(identifier, clazz);
    }

    public void addPreProcessor(ExtractorProcessor processor) {
        this.preprocess.add(processor);
    }

    private void createPatternMatchers() {
        for (String section : inputsections) {
            patternmatchers.add(new ExtractorPatternMatcher(this, section, sectionmarkers.get(section)));
        }
    }

    /**
     * Add a process (in name) that is executed on a section, and of which the
     * result is stored as an attribute. The process then needs to be defined
     *
     * @param section either "all" for the whole raw content of the entity, or
     * the name of a section created by a section marker.
     * @param processname name of a process that must be defined
     * @param attribute
     */
    public void addSectionProcess(String section, String processname, String attribute) {
        processors.add(new SectionProcess(section, processname, attribute));
        processes.add(processname);
    }

    private Class stringToClass(String classname) {
        return ClassTools.toClass(classname, 
                getClass().getPackage().getName(),
                Extractor.class.getPackage().getName(),
                ExtractorProcessor.class.getPackage().getName());
    }
    
    /**
     * Add a process that is executed on a section, that has no resulting
     * attribute.
     */
    public void addSectionProcess(String section, String processname) {
        addSectionProcess(section, processname, null);
    }

    private void addSectionMarker(String sectionmarkername, String inputsection, String outputsection) {
        Class clazz = stringToClass(sectionmarkername);
        addSectionMarker(clazz, inputsection, outputsection);
    }

    /**
     * Add a SectionMarker, which will produce an outputsection based on match
     * areas in the inputsection. E.g. MarkMeta will mark <meta ... > tags in
     * the source section. mark the
     *
     * @param sectionmarker
     * @param inputsection
     * @param outputsection
     */
    public void addSectionMarker(Class sectionmarker, String inputsection, String outputsection) {
        Constructor c = ClassTools.getAssignableConstructor(sectionmarker, SectionMarker.class, Extractor.class, String.class, String.class);
        SectionMarker marker = (SectionMarker) ClassTools.construct(c, this, inputsection, outputsection);
        ArrayList<SectionMarker> list = sectionmarkers.get(inputsection);
        if (list == null) {
            list = new ArrayList<SectionMarker>();
            sectionmarkers.put(inputsection, list);
        }
        list.add(marker);
        if (!inputsections.contains(inputsection)) {
            inputsections.add(inputsection);
        }
        if (!allsections.contains(inputsection)) {
            allsections.add(inputsection);
        }
        if (!allsections.contains(outputsection)) {
            allsections.add(outputsection);
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

    /**
     * Creates an ExtractorProcessor from the given class, and adds that to the
     * process pipeline of the named process.
     *
     * @param process
     * @param processor
     */
    public void addProcess(String process, Class processor) {
        addProcess(process, createUnboundProcessor(process, processor));
    }

    /**
     * Adds an ExtractorProcessor from the given class, and adds that to the
     * process pipeline of the named process. The processors are executed in the
     * same order the were added to the process pipeline.
     *
     * @param process
     * @param processor
     */
    public void addProcess(String process, ExtractorProcessor processor) {
        ArrayList<ExtractorProcessor> list = this.processor.get(process);
        if (list == null) {
            list = new ArrayList();
            this.processor.put(process, list);
        }
        list.add(processor);
    }

    public ExtractorProcessor findProcessor(String process, Class clazz) {
        for (ExtractorProcessor p : processor.get(process)) {
            if (clazz.equals(p.getClass())) {
                return p;
            }
        }
        return null;
    }

    public Section getAll(Entity entity) {
        ArrayList<Section> list = entity.getSectionPos("all");
        if (list.size() == 0) {
            entity.addSectionPos("all", 0, 0, entity.content.length, entity.content.length);
            list = entity.getSectionPos("all");
        }
        return list.get(0);
    }

    /**
     * Processes the entity according to the configured extraction process.
     *
     * @param entity
     */
    public void process(Entity entity) {
        if (neverused) {
            neverused = false;
            createPatternMatchers();
        }
        //ShowContent showcontent = new ShowContent(this, "tokenize");
        int bufferpos = 0;
        int bufferend = entity.content.length;
        //log.info("extract() bufferpos %d bufferend %d", bufferpos, bufferend);
        if (bufferpos >= bufferend) {
            return;
        }
        try {
            for (ExtractorProcessor proc : this.preprocess) {
                proc.process(entity, getAll(entity), null);
            }
            //showcontent.process(entity, new Section(0, 0, bufferend, bufferend), "all");
            this.processSectionMarkers(entity, bufferpos, bufferend);
            for (SectionProcess p : this.processors) {
                for (Section section : entity.getSectionPos(p.section)) {
                    for (ExtractorProcessor proc : processor.get(p.process)) {
                        proc.process(entity, section, p.entityattribute);
                  //if (p.process.equals("tokenize"))
                        //   showcontent.process(entity, section, proc.getClass().getSimpleName());
                    }
                }
            }
        } catch (RemovedException ex) {
        }
    }

    public void removeProcessor(String process, Class processclass) {
        ArrayList<ExtractorProcessor> get = processor.get(process);
        Iterator<ExtractorProcessor> iter = get.iterator();
        while (iter.hasNext()) {
            ExtractorProcessor p = iter.next();
            if (processclass.isAssignableFrom(p.getClass())) {
                iter.remove();
            }
        }
    }

    void processSectionMarkers(Entity entity, int bufferpos, int bufferend) {
        //entity.addSectionPos( "all", bufferpos, bufferpos, bufferend, bufferend );
        for (int section = 0; section < inputsections.size(); section++) {
            String sectionname = inputsections.get(section);
            //log.info("processSectionMarkers %s", sectionname);
            ExtractorPatternMatcher patternmatcher = patternmatchers.get(section);
            for (Section pos : entity.getSectionPos(sectionname)) {
                patternmatcher.processSectionMarkers(entity, pos.open, pos.close);
            }
        }
    }

    /**
     * Creates a new Array of Section based on an existing Section name, 
     * from which all positions that are occupied by any instance of an array
     * of other Section are removed. Typical usage is to use "all" as the
     * container section, and Extractor.sections as the other sections, which
     * results in a section that contains all positions except those marked
     * in other sections. Note that "all" is automatically excluded from the
     * list of other sections.
     * @param entity
     * @param containersection
     * @param othersections
     * @param resultsection 
     */
    protected void createUnmarkedSection(Entity entity, String containersection, ArrayList<String> othersections, String resultsection) {
        TreeSet<Section> all = new TreeSet();
        for (Section section : entity.getSectionPos(containersection)) {
            all.add((Section)section.clone());
        }
        TreeSet<Section> other = new TreeSet();
        for (String section : othersections) {
            if (!section.equals(containersection)) {
                other.addAll(entity.getSectionPos(section));
            }
        }
        Section firstother = other.pollFirst();
        while (all.size() > 0) {
            Section s = all.pollFirst();
            while (s.open < s.closetrail) {
                log.info("section %d %d", s.open, s.closetrail);
                for (; firstother != null && firstother.closetrail < s.open; firstother = other.pollFirst());
                if (firstother == null || firstother.open >= s.closetrail) {
                    log.info("add1 %d %d %d %d", s.openlead, s.open, s.close, s.closetrail);
                    entity.addSectionPos(resultsection, s.openlead, s.open, s.close, s.closetrail);
                    s.open = s.closetrail;
                } else {
                    if (firstother.openlead > s.open) {
                        log.info("add2 %d %d", s.open, firstother.openlead);
                        entity.addSectionPos(resultsection, s.openlead, s.open, firstother.openlead, firstother.openlead);
                    }
                    s.open = firstother.closetrail;
                    s.openlead = firstother.closetrail;
                    firstother = other.pollFirst();
                }
            }
        }
    }
    
    
    private class SectionProcess {

        String section;
        String process;
        String entityattribute;

        public SectionProcess(String section, String process, String entityattribute) {
            this.section = section;
            this.process = process;
            this.entityattribute = entityattribute;
        }
    }
}
