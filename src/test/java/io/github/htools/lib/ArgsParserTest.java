package io.github.htools.lib;

import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import io.github.htools.lib.ArgsParser.Parameter;
import static org.junit.Assert.*;
import org.junit.Test;

public class ArgsParserTest {
    public static Log log = new Log(ArgsParserTest.class);
    ArgsParser parsedargs;

    public ArgsParserTest() { }

    @Test
    public void testFatal() {
//        ArgsParser.log.setLevel(Log.NONE);
//        System.out.println("dont worry, checking fatal exception, should return: run with parameter: param");
//        parsedargs = new ArgsParser(new String[0], "param");
//        assertEquals(parsedargs.getParameters().size(), 1);
//        parsedargs = new ArgsParser(new String[0], "[param]");
    }

    @Test
    public void testNonFatal() {
        System.out.println("checking non fatal exceptions");
        parsedargs = new ArgsParser(new String[0], "[param]");
        assertFalse(parsedargs.exists("param"));
        assertTrue(!parsedargs.exists("other"));
        parsedargs = new ArgsParser(new String[0], "{param}");
        assertEquals(0, parsedargs.getParameter("param").size());
        parsedargs = new ArgsParser(new String[]{"aap"}, "param [opt]");
        for (Parameter p : parsedargs.getParameters())
            log.info("param %s=%s", p.getName(), p.getValues());
        assertTrue(parsedargs.exists("param"));
        assertFalse(parsedargs.exists("opt"));
        parsedargs = new ArgsParser(new String[]{"aap", "noot"}, "param [opt]");
        assertEquals(parsedargs.get("param"), "aap");
        assertEquals(parsedargs.get("opt"), "noot");
    }

    @Test
    public void flags1() {
        log.info("flags1");
        parsedargs = new ArgsParser("aap -c noot mies".split(" "), "-a first -b second -c third");
        assertEquals("aap", parsedargs.get("first"));
        assertEquals("noot", parsedargs.get("third"));
        assertEquals("mies", parsedargs.get("second"));
    }
    
    @Test
    public void flags2() {
        log.info("flags2");
        parsedargs = new ArgsParser("-a aap -c noot -c mies".split(" "), "-a first -b [second] -c third");
        assertEquals("aap", parsedargs.get("first"));
        assertFalse(parsedargs.exists("second"));
        assertEquals(2, parsedargs.getParameter("third").size());
    }
    
    @Test
    public void optional1() {
        log.info("optional1");
        parsedargs = new ArgsParser("-a aap -b noot -b mies".split(" "), "-a first -b second -c [third]");
        assertFalse(parsedargs.exists("third"));
        assertEquals(2, parsedargs.getParameter("second").size());
    }
    
    @Test
    public void optional2() {
        log.info("optional2");
        parsedargs = new ArgsParser("-a aap -b noot -c mies".split(" "), "-a first -b second -c [third]");
        assertEquals("noot", parsedargs.get("second"));
        assertEquals("mies", parsedargs.get("third"));
    }
    
    @Test
    public void repeated() {
        log.info("repeated");
        parsedargs = new ArgsParser("-a aap noot -b".split(" "), "-a {first} -b {second}");
        assertEquals("aap", parsedargs.get("first"));
        assertEquals("noot", parsedargs.getParameter("first").get(1));
        assertEquals(0, parsedargs.getParameter("second").size());
    }
    
    @Test
    public void repeated2() {
        log.info("repeated");
        parsedargs = new ArgsParser("-a -b aap noot ".split(" "), "-a {first} -b {second}");
        assertEquals("aap", parsedargs.get("second"));
        assertEquals("noot", parsedargs.getParameter("second").get(1));
        assertEquals(0, parsedargs.getParameter("first").size());
    }
    
    @Test
    public void booleanFlag() {
        log.info("booleanflag");
        parsedargs = new ArgsParser("-a aap -b noot -c mies".split(" "), "-a first -b second -c [third]");
        assertEquals(0, parsedargs.getParameter("f").size());
        parsedargs = new ArgsParser("-a aap -b noot -c mies --f".split(" "), "-a first -b second -c [third] --f");
        assertEquals("true", parsedargs.get("f"));
        assertEquals(true, parsedargs.getBoolean("f"));
        assertEquals("noot", parsedargs.get("second"));
        assertEquals("mies", parsedargs.get("third"));
    }
    
    @Test
    public void booleanFlag2() {
        log.info("booleanflag");
        parsedargs = new ArgsParser("aap noot".split(" "), "-i input -o output");
        //assertEquals(false, parsedargs.getBoolean("v"));
        //assertEquals(false, parsedargs.getBoolean("r"));
        assertEquals("aap", parsedargs.get("input"));
        assertEquals("noot", parsedargs.get("output"));
    }
    
    @Test
    public void testSpace() {
        log.info("booleanflag");
        parsedargs = new ArgsParser(new String[]{"-a", "aap noot", "-b", "noot"}, "-a first -b second");
        assertEquals("aap noot", parsedargs.get("first"));
        assertEquals("noot", parsedargs.get("second"));
    }
}
