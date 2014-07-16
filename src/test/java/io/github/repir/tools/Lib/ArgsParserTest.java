package io.github.repir.tools.Lib;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArgsParserTest {
    public static Log log = new Log(ArgsParserTest.class);
    ArgsParser parsedargs;

    public ArgsParserTest() { }

    @Test
    public void testFatal() {
        ArgsParser.log.setLevel(Log.NONE);
        System.out.println("dont worry, checking fatal exception, should return: run with parameter: param");
        parsedargs = new ArgsParser(new String[0], "param");
        assertEquals(parsedargs.parsedargs.size(), 0);
        parsedargs = new ArgsParser(new String[0], "[param]");
    }

    @Test
    public void testNonFatal() {
        System.out.println("checking non fatal exceptions");
        parsedargs = new ArgsParser(new String[0], "[param]");
        assertFalse(parsedargs.exists("param"));
        assertTrue(!parsedargs.exists("other"));
        parsedargs = new ArgsParser(new String[0], "{param}");
        assertNotNull(parsedargs.getRepeatedGroup("param"));
        parsedargs = new ArgsParser(new String[]{"aap"}, "param [opt]");
        assertTrue(parsedargs.exists("param"));
        assertFalse(parsedargs.exists("opt"));
        parsedargs = new ArgsParser(new String[]{"aap", "noot"}, "param [opt]");
        assertEquals(parsedargs.get("param"), "aap");
        assertEquals(parsedargs.get("opt"), "noot");
        parsedargs = new ArgsParser(new String[]{"aap", "1", "2.5"}, "param int double [opt]");
        assertEquals(parsedargs.getInt("int"), 1);
        assertEquals(parsedargs.getDouble("double"), 2.5, 0.001);
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
        assertEquals(2, parsedargs.getRepeatedGroup("third").length);
    }
    
    @Test
    public void optional1() {
        log.info("optional1");
        parsedargs = new ArgsParser("-a aap -b noot -b mies".split(" "), "-a first -b second -c [third]");
        assertEquals("aap", parsedargs.get("first"));
        assertFalse(parsedargs.exists("third"));
        assertEquals(2, parsedargs.getRepeatedGroup("second").length);
    }
    
    @Test
    public void optional2() {
        log.info("optional2");
        parsedargs = new ArgsParser("-a aap -b noot -c mies".split(" "), "-a first -b second -f -c [third]");
        assertEquals("aap", parsedargs.get("first"));
        assertEquals("noot", parsedargs.get("second"));
        assertEquals("mies", parsedargs.get("third"));
    }
    
    @Test
    public void booleanFlag() {
        log.info("booleanflag");
        //parsedargs = new ArgsParser("-a aap -b noot -c mies".split(" "), "-a first -b second -f -c [third]");
        //assertNull(parsedargs.get("f"));
        parsedargs = new ArgsParser("-a aap -b noot -c mies -f".split(" "), "-a first -b second -f -c [third]");
        assertEquals("true", parsedargs.get("f"));
        assertEquals("noot", parsedargs.get("second"));
        assertEquals("mies", parsedargs.get("third"));
    }
}
