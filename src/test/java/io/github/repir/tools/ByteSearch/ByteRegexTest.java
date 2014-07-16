package io.github.repir.tools.ByteSearch;

import io.github.repir.tools.ByteSearch.ByteRegex;
import static io.github.repir.tools.ByteSearch.ByteRegex.log;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jeroen
 */
public class ByteRegexTest {
   public static Log log = new Log( ByteRegexTest.class );
   public String frag[] = {"<top> top </top>", "<top><head> head </top>", "aap.noot3=checks", "noot3=doesnt "};

   @Test
   public void testFindFirst_3args() {
      System.out.println("findFirst");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"<top> top </top>", "top><head> head </top>"},
              new testcase("<to+p>", new ByteSearchPosition(0,5), ByteSearchPosition.notFound()),
              new testcase(".top(?=>)", new ByteSearchPosition(0,4), ByteSearchPosition.notFound()),
              new testcase("top>", ByteSearchPosition.notFound(), new ByteSearchPosition(0,4)),
              new testcase("<?top>", new ByteSearchPosition(0,5), new ByteSearchPosition(0,4)),
              new testcase(".", new ByteSearchPosition(0,1), new ByteSearchPosition(0,1)),
              new testcase("^[<t]", new ByteSearchPosition(0,1), new ByteSearchPosition(0,1))
      ));
      for (find s : sets) {
         for (testcase c : s.testcase) {
            for (int r = 0; r < s.source.length; r++) {
               ByteSearchPosition result = c.regex.matchPos(s.bytesource[r], 0, s.bytesource[r].length);
               assertEquals(io.github.repir.tools.Lib.PrintTools.sprintf("failed source '%s' pattern '%s'", s.source[r], c.regex.pattern), c.expectedResult[r], result);
            }
         }
      }
   }

   @Test
   public void testFind_3args() {
      System.out.println("find");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"<top> top </top>", "<top><head> head </top>"},
              new testcase("<to+p>", new ByteSearchPosition(0,5), new ByteSearchPosition(0,5)),
              new testcase("<top(?=>)", new ByteSearchPosition(0,4), new ByteSearchPosition(0,4)),
              new testcase("</top>$", new ByteSearchPosition(10, 16, false), new ByteSearchPosition(17,23, false)),
              new testcase(" ", new ByteSearchPosition(5, 6), new ByteSearchPosition(11,12)),
              new testcase(".", new ByteSearchPosition(0,1), new ByteSearchPosition(0,1))));
      sets.add(new find(new String[]{"a3ap.noot3=checks", "noot3=doesnt ", "aap.noot", ""},
              new testcase("na*o*t", new ByteSearchPosition(5,9), new ByteSearchPosition(0,4), new ByteSearchPosition(4, 8), ByteSearchPosition.endReached()),
              new testcase("^noot", ByteSearchPosition.notFound(), new ByteSearchPosition(0,4), ByteSearchPosition.notFound(), ByteSearchPosition.endReached()),
              new testcase("\\c\\w*(\\.\\c\\w*)+\\=\\S*", new ByteSearchPosition(0,17, true), ByteSearchPosition.notFound(), ByteSearchPosition.notFound(), ByteSearchPosition.endReached()),
              new testcase("\\c\\w*(\\.\\c\\w*)+\\=\\S*$", new ByteSearchPosition(0,17, true), ByteSearchPosition.notFound(), ByteSearchPosition.notFound(), ByteSearchPosition.endReached())));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testChoice() {
      System.out.println("find");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"tiptoptip"},
              new testcase("(tip|top)t", new ByteSearchPosition(0,4))
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testfile() {
      System.out.println("find");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"trec6.ep"},
              new testcase(".decay.", ByteSearchPosition.notFound())
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testMatchPos() {
      System.out.println("matchPos");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"<top> top </top>", "top><head> head </top>"},
              new testcase("<to+p>", new ByteSearchPosition(0,5), ByteSearchPosition.notFound()),
              new testcase("<top(?=>)", new ByteSearchPosition(0,4), ByteSearchPosition.notFound()),
              new testcase("</top>$", new ByteSearchPosition(10,16,false), new ByteSearchPosition(16,22,false) ),
              new testcase(" ", new ByteSearchPosition(5,6), new ByteSearchPosition(10,11)),
              new testcase(".", new ByteSearchPosition(0, 1), new ByteSearchPosition(0,1))
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testLookBehind() {
      System.out.println("testLookBehind");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"test d'Arc", "robert's ", "o'brien", "'s"},
              new testcase("'(?<=\\c')", new ByteSearchPosition(6,7), new ByteSearchPosition(6,7), new ByteSearchPosition(1,2), ByteSearchPosition.notFound()),
              new testcase("['\\-]\\w((?<=\\W\\w['\\-]\\w)|(?<=^\\w['\\-]\\w))", new ByteSearchPosition(6,8), ByteSearchPosition.notFound(), new ByteSearchPosition(1,3), ByteSearchPosition.notFound()),
              new testcase("'s(?<=\\c's)", ByteSearchPosition.notFound(), new ByteSearchPosition(6,8), ByteSearchPosition.notFound(), ByteSearchPosition.notFound())
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testOptionalEnd() {
      System.out.println("testOptionalEnd");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"test d'Arc", "robert's ", "o'brien", "'s\\n"},
              new testcase("[^\\n]+\\n?", new ByteSearchPosition(0,10, true), new ByteSearchPosition(0,9,true), new ByteSearchPosition(0,7,true), new ByteSearchPosition(0,4, true)) // appearently \n is two bytes
              ));
      for (find s : sets) {
         s.test();
      }
   }


   /**
    * Test of findAllPos method, of class ByteRegex.
    */
   @Test
   public void testFindAllCount() {
      System.out.println("findAll");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"<top> top </top>", "top><head> head </top>", "aap13", ""},
              new testcase("<?head", 0, 2, 0, 0),
              new testcase("<", 2, 2, 0, 0),
              new testcase(">", 2, 3, 0, 0),
              new testcase("\\w+", 3, 4, 1, 0),
              new testcase("\\w*", 11, 13, 2, 1),
              new testcase("\n", 0, 0, 0, 0),
              new testcase("(\\c+|\\d+)", 3, 4, 2, 0)
              ));
      for (find s : sets) {
         for (testcase c : s.testcase) {
            for (int r = 0; r < s.source.length; r++) {
               ArrayList<ByteSearchPosition> result = c.regex.findAllPos(s.bytesource[r], 0, s.bytesource[r].length);
               assertEquals(io.github.repir.tools.Lib.PrintTools.sprintf("failed source '%s' pattern '%s'", s.source[r], c.regex.pattern), c.expectedResult[r], result.size());
            }
         }
      }
   }

   @Test
   public void testFindAllCountOverlap() {
      System.out.println("findAll");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"<top> top"},
              new testcase("\\w+", 2)
              ));
      for (find s : sets) {
         for (testcase c : s.testcase) {
            for (int r = 0; r < s.source.length; r++) {
               ArrayList<ByteSearchPosition> result = c.regex.findAllPos(s.bytesource[r], 0, s.bytesource[r].length);
               assertEquals(io.github.repir.tools.Lib.PrintTools.sprintf("failed source '%s' pattern '%s'", s.source[r], c.regex.pattern), c.expectedResult[r], result.size());
            }
         }
      }
   }

   @Test
   public void testFindNumber() {
      System.out.println("findNumber");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"10.0", "10.0 aap", "aap 10.0 aap", "aap 10.0e", "aap 10.0.2 a", "aap 10.0e3", "aap 10.0e-3"},
              new testcase("(^|(?<=[^\\w\\.]))\\d+\\.\\d+(e[\\-\\+]?\\d+)?($|(?=[^\\w\\.]))",
              new ByteSearchPosition(0,4,true), 
                      new ByteSearchPosition(0,4), 
                      new ByteSearchPosition(4,8), 
                      ByteSearchPosition.notFound(), 
                      ByteSearchPosition.notFound(), 
                      new ByteSearchPosition(4,10,true), 
                      new ByteSearchPosition(4,11,true))
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testFindNumber1() {
      System.out.println("findNumber");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"u.s.", "uu.s.", " .s."},
              new testcase("\\.((?<=^\\c\\.)|(?<=[^\\w\\.]\\c\\.))(\\c\\.)+",
              new ByteSearchPosition(1, 4, true), ByteSearchPosition.notFound(), ByteSearchPosition.notFound())
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testInvertedGroup() {
      System.out.println("findNumber");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"1", "a", " ", "."},
              new testcase("[^\\w\\.]",
              ByteSearchPosition.notFound(), ByteSearchPosition.notFound(), new ByteSearchPosition(0,1,false), ByteSearchPosition.notFound())
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testUngreedy() {
      System.out.println("unGreedy");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{"12345678901234567890"},
              new testcase("2.*?4", new ByteSearchPosition(1,4)), // ungreedy
              new testcase("2.*4", new ByteSearchPosition(1,14,true)) // greedy always trigger endReached
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testTagename() {
      System.out.println("unGreedy");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" <TITLE ", "<a href=> ", "<0 ", "< aap ",
                                        "<h0> ", "<title/>" },
              new testcase("</?[A-Za-z]\\w*?[/\\s>]",
              new ByteSearchPosition(1,8, false), new ByteSearchPosition(0,3), 
              ByteSearchPosition.notFound(), ByteSearchPosition.notFound(),
              new ByteSearchPosition(0,4), new ByteSearchPosition(0,7))
              ));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testEscapedQuote() {
      System.out.println("quote");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" '\\''", " \"\\\"\"", " ''", " \"\"" },
              new testcase("\\Q",
              new ByteSearchPosition(1,5, false),
              new ByteSearchPosition(1,5, false),
              new ByteSearchPosition(1,3, false),
              new ByteSearchPosition(1,3, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testEscapedQuote1() {
      System.out.println("quote1");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" <TITLE a=''>" },
              new testcase("\\s+\\w+=\\Q",
              new ByteSearchPosition(7,12, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testEscapedQuote2() {
      System.out.println("quote1");
      ArrayList<findall> sets = new ArrayList<findall>();
      sets.add(new findall(new String[]{" <TITLE a='' b=\"\\\"\" c=\"aap\">" },
              new testcase("\\s+\\w+=\\Q",
              3
              )));
      for (findall s : sets) {
         s.test();
      }
   }

   @Test
   public void testEscapedQuote3() {
      System.out.println("quote1");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" <TITLE a='' b=\"\\\"\" c=\"aap\">" },
              new testcase("<\\w+(\\s+\\w+=\\Q)*/?>",
              new ByteSearchPosition(1,28, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testEscapedQuote4() { 
      System.out.println("quote1");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" <TITLE a='' b=\"\\\"\" c=\"aap\">" },
              new testcase("<TITLE(\\s([^'\">]|\\Q)*)?>",
              new ByteSearchPosition(1,28, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

   @Test
   public void testContent() { 
      System.out.println("quote1");
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" <T name=\"description\"" },
              new testcase("\\sname\\s*=\\s*['\"]?(keywords|description)",
              new ByteSearchPosition(3,21, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

  @Test
   public void testMinus() { 
      ArrayList<find> sets = new ArrayList<find>();
      sets.add(new find(new String[]{" --az-za" },
              new testcase("-[a-z]+",
              new ByteSearchPosition(2,5, false)
              )));
      for (find s : sets) {
         s.test();
      }
   }

   class find {

      String source[];
      byte bytesource[][];
      testcase testcase[];

      public find(String sources[], testcase... test) {
         source = sources;
         testcase = test;
         bytesource = new byte[source.length][];
         for (int i = 0; i < source.length; i++) {
            bytesource[i] = source[i].getBytes();
         }
      }

      public void test() {
         for (testcase c : testcase) {
            for (int r = 0; r < source.length; r++) {
               ByteSearchPosition result = c.regex.findPos(bytesource[r], 0, bytesource[r].length);
               assertEquals(io.github.repir.tools.Lib.PrintTools.sprintf("failed source '%s' pattern '%s'", source[r], c.regex.pattern), c.expectedResult[r], result);
            }
         }
      }
   }

   class findall {

      String source[];
      byte bytesource[][];
      testcase testcase[];

      public findall(String sources[], testcase... test) {
         source = sources;
         testcase = test;
         bytesource = new byte[source.length][];
         for (int i = 0; i < source.length; i++) {
            bytesource[i] = source[i].getBytes();
         }
      }

      public void test() {
         for (testcase c : testcase) {
            for (int r = 0; r < source.length; r++) {
               ArrayList<ByteSearchPosition> result = c.regex.findAllPos(bytesource[r], 0, bytesource[r].length);
               assertEquals(io.github.repir.tools.Lib.PrintTools.sprintf("failed source '%s' pattern '%s'", source[r], c.regex.pattern), c.expectedResult[r], result.size());
            }
         }
      }
   }

   class testcase {

      ByteRegex regex;
      Object expectedResult[];

      public testcase(String r, Object... result) {
         regex = new ByteRegex(r);
         expectedResult = result;
      }
   }
}
