package io.github.htools.extract;

import io.github.htools.extract.modules.ExtractorProcessor;
import io.github.htools.extract.modules.SectionMarker;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import io.github.htools.hadoop.io.archivereader.*;
import io.github.htools.lib.ByteTools;

/**
 * The Extractor is a generic processor that converts {@link Content}s submitted
 * by an {@link Reader} into extracted values to store as features.
 * <p>
 * Extraction proceeds in 3 phases. (1) the raw byte content of the
 * {@link Content} is pre-processed by the modules configured as
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
public class Extractor {

    public static Log log = new Log(Extractor.class);
    protected boolean neverused = true;
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
    }

    public void addPreProcessor(Class clazz) {
        try {
            this.preprocess.add(createUnboundProcessor("preprocess", clazz));
        } catch (ClassNotFoundException ex) {
            log.exception(ex, "addPreProcessor(%s)", clazz.getCanonicalName());
        }
    }

    /**
     * Creates a processor that is not bound to any process, which may be used
     * by other processors to create sub-processors.
     *
     * @param clazz
     */
    public <T extends ExtractorProcessor> T createUnboundProcessor(String identifier, Class<T> clazz) throws ClassNotFoundException {
        Constructor c = ClassTools.getAssignableConstructor(clazz, ExtractorProcessor.class, Extractor.class, String.class);
        return (T) ClassTools.construct(c, this, identifier);
    }

    public <T extends ExtractorProcessor> T createUnboundProcessor(String identifier, String clazzname) throws ClassNotFoundException {
        Class<T> clazz = stringToClass(clazzname);
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

    protected Class stringToClass(String classname) {
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

    protected void addSectionMarker(String sectionmarkername, String inputsection, String outputsection) {
        Class clazz = stringToClass(sectionmarkername);
        addSectionMarker(clazz, inputsection, outputsection);
    }

    /**
     * Add a SectionMarker, which will produce an outputsection based on match
     * areas in the inputsection. E.g. MarkMeta will mark &lt;meta ... &gt; tags in
     * the source section. mark the
     *
     * @param sectionmarker
     * @param inputsection
     * @param outputsection
     */
    public void addSectionMarker(Class sectionmarker, String inputsection, String outputsection) {
        try {
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
        } catch (ClassNotFoundException ex) {
            log.exception(ex, "addSectionMarker(%s, %s, %s)", sectionmarker, inputsection, outputsection);
        }
    }

    /**
     * Creates an ExtractorProcessor from the given class, and adds that to the
     * process pipeline of the named process.
     *
     * @param process
     * @param processor
     */
    public <T extends ExtractorProcessor> T addProcess(String process, Class<T> processor) {
        T p = null;
        try {
            p = createUnboundProcessor(process, processor);
            addProcess(process, p);
        } catch (ClassNotFoundException ex) {
            log.exception(ex, "addProcess(%s, %s)", process, processor.getCanonicalName());
        }
        return p;
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

    public <T extends ExtractorProcessor> T findProcessor(String process, Class<T> clazz) {
        for (ExtractorProcessor p : processor.get(process)) {
            if (clazz.equals(p.getClass())) {
                return (T)p;
            }
        }
        return null;
    }

    /**
     * Processes the content according to the configured extraction process.
     *
     * @param content
     */
    public void process(Content content) {
        //ShowContent showcontent = new ShowContent(this, "tokenize");
        if (content.content.length == 0) {
            return;
        }
        try {
            for (ExtractorProcessor proc : this.preprocess) {
                //log.info("PreProcess %s", proc.getClass().getCanonicalName());
                proc.process(content, content.getAll(), null);
            }
            this.processSectionMarkers(content);
            preProcess(content);
            for (SectionProcess p : this.processors) {
                for (ByteSearchSection section : content.getSectionPos(p.section)) {
                    //log.info("section %d %d", section.start, section.innerstart);
                    for (ExtractorProcessor proc : processor.get(p.process)) {
                        //log.info("process %s", proc.getClass().getCanonicalName());
                        proc.process(content, section, p.entityattribute);
                    }
                }
            }
        } catch (RemovedException ex) {
        }
    }
    
    protected void preProcess(Content content) {}

    public Content process(byte content[]) {
        Content entity = new Content();
        entity.setContent(content);
        this.process(entity);
        return entity;
    }

    public Content process(String text) {
        return process(ByteTools.toBytes(text));
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

    protected void processSectionMarkers(Content content) {
        //log.info("process sessionmarkers %d ", inputsections.size());
        if (neverused) {
            neverused = false;
            createPatternMatchers();
        }
        content.getAll();
        for (int section = 0; section < inputsections.size(); section++) {
            String sectionname = inputsections.get(section);
            ExtractorPatternMatcher patternmatcher = patternmatchers.get(section);
            //log.info("process sessionmarkers %s %d %s", sectionname, patternmatcher.markers.size(), entity.getSectionPos(sectionname));
            for (ByteSearchSection pos : content.getSectionPos(sectionname)) {
                patternmatcher.processSectionMarkers(content, pos);
            }
        }
    }

    /**
     * Creates a new Array of Section based on an existing Section name, from
     * which all positions that are occupied by any instance of an array of
     * other Section are removed. Typical usage is to use "all" as the container
     * section, and Extractor.sections as the other sections, which results in a
     * section that contains all positions except those marked in other
     * sections. Note that "all" is automatically excluded from the list of
     * other sections.
     *
     * @param entity
     * @param containersection
     * @param othersections
     * @param resultsection
     */
    protected void createUnmarkedSection(Content entity, String containersection, ArrayList<String> othersections, String resultsection) {
        TreeSet<ByteSearchSection> all = new TreeSet();
        for (ByteSearchSection section : entity.getSectionPos(containersection)) {
            all.add(new ByteSearchSection(section));
        }
        TreeSet<ByteSearchSection> other = new TreeSet();
        for (String section : othersections) {
            if (!section.equals(containersection)) {
                other.addAll(entity.getSectionPos(section));
            }
        }
        ByteSearchSection firstother = other.pollFirst();
        while (all.size() > 0) {
            ByteSearchSection s = all.pollFirst();
            while (s.innerstart < s.end) {
                int innerstart = s.innerstart;
                int start = s.start;
                for (; firstother != null && firstother.end < innerstart; firstother = other.pollFirst());
                if (firstother == null || firstother.innerstart >= s.end) {
                    entity.addSectionPos(resultsection, s.haystack, start, innerstart, s.innerend, s.end);
                    innerstart = s.end;
                } else {
                    if (firstother.start > s.innerstart) {
                        entity.addSectionPos(resultsection, s.haystack, start, innerstart, firstother.start, firstother.start);
                    }
                    innerstart = firstother.end;
                    start = firstother.end;
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
