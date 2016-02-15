package io.github.htools.hadoop;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.github.htools.lib.ArgsParserTest.log;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author jeroen
 */
public class ConfTest {
    Conf conf;
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testNonFatal() {
        System.out.println("checking non fatal exceptions");
        conf = new Conf(new String[0], "[param]");
        assertNull(conf.get("param"));
        conf = new Conf(new String[0], "{param}");
        assertEquals(0, conf.getStrings("param").length);
        conf = new Conf(new String[]{"aap"}, "param [opt]");
        assertEquals("aap", conf.get("param"));
        assertNull(conf.get("opt"));
        conf = new Conf(new String[]{"aap", "noot"}, "param [opt]");
        assertEquals("aap", conf.get("param"));
        assertEquals("noot", conf.get("opt"));
        conf = new Conf(new String[]{"aap", "1", "2.5"}, "param int double [opt]");
        assertEquals(1, conf.getInt("int", 0));
        assertEquals(2.5, conf.getDouble("double", 0), 0.001);
    }

    @Test
    public void flags1() {
        log.info("flags1");
        conf = new Conf("aap -c noot mies".split(" "), "-a first -b second -c third");
        assertEquals("aap", conf.get("first"));
        assertEquals("noot", conf.get("third"));
        assertEquals("mies", conf.get("second"));
    }
    
    @Test
    public void flags2() {
        log.info("flags2");
        conf = new Conf("-a aap -c noot -c mies".split(" "), "-a first -b [second] -c third");
        assertEquals("aap", conf.get("first"));
        assertNull(conf.get("second"));
        assertEquals(2, conf.getStrings("third").length);
    }
    
    @Test
    public void optional1() {
        log.info("optional1");
        conf = new Conf("-a aap -b noot -b mies".split(" "), "-a first -b second -c [third]");
        assertEquals("aap", conf.get("first"));
        assertNull(conf.get("third"));
        assertEquals(2, conf.getStrings("second").length);
    }
    
    @Test
    public void optional2() {
        log.info("optional2");
        conf = new Conf("-a aap -b noot -c mies".split(" "), "-a first -b second -c [third]");
        assertEquals("aap", conf.get("first"));
        assertEquals("noot", conf.get("second"));
        assertEquals("mies", conf.get("third"));
    }

    @Test
    public void booleanFlag() {
        log.info("booleanflag");
//        conf = new Conf("-a aap -b noot -c mies".split(" "), "-a first -b second -f -c [third]");
//        assertFalse(conf.getBoolean("f", false));
//        conf = new Conf("-a aap -b noot -c mies -f".split(" "), "-a first -b second -f -c [third]");
//        assertTrue("true", conf.getBoolean("f", false)); 
//        assertEquals("noot", conf.get("second"));
//        assertEquals("mies", conf.get("third"));
    }
}
