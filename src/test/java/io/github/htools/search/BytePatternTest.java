/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.htools.search;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author jer
 */
public class BytePatternTest {

   public static Log log = new Log(BytePatternTest.class);

   public BytePatternTest() {
   }

   @Test
   public void test1() {
      log.info("test1");
      ByteSearch skip = ByteSearch.create("----\\s+(By\\s|Special\\sto\\s|A\\sWall\\sStreet\\Journal)");
      ByteSearch p = ByteSearch.create(">").QuoteSafe();
      int pos = p.find(" <test a=\">\" b='>' c=\"\\\">\" d='\\\'>' >".getBytes(), 0, 36);
      assertEquals(35, pos);
      log.info("end test1");
   }

   @Test
   public void test2() {
      log.info("test2");
      ByteSearch p = ByteSearch.create("Zickel");
      int pos = p.find(" zickel".getBytes(), 0, 7);
      assertEquals(1, pos);
      log.info("end test2");
   }

   public byte[] htmlFragment() {
      return ("<head profile=\"http://gmpg.org/xfn/11\">\n"
              + " 4587087    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
              + " 4587088    \n"
              + " 4587089    <title></title>                                                                                                                                             \n"
              + " 4587090    \n"
              + " 4587091    <link rel=\"stylesheet\" href=\"http://ryan19.learnerblogs.org/wp-content/themes/default/style.css\" type=\"text/css\" media=\"screen\" />\n"
              + " 4587092 "
              + "<title></title>   ").getBytes();
   }

   @Test
   public void test3() {
      log.info("test3");
      byte text[] = htmlFragment();
      ByteRegex starttag = new ByteRegex("<title");
      ByteSearch endtag = ByteSearch.create(">");
      ByteSearch endmarker = ByteSearch.create("</title").toSection(ByteSearch.create(">"));
      ByteSearchPosition findPos = starttag.findPos(text, 0, text.length);
      int endpos = endtag.findEndQuoteSafe(text, findPos.end, text.length);
      ByteSearchPosition end = endmarker.findPos(text, endpos, text.length);
      log.info("%d %d %d %d %d", findPos.start, findPos.end, endpos, end.start, end.end);
      assertEquals(154, end.start);
      log.info("end test3");
   }

   public byte[] metaFragment() {
      return ("<meta name=\"author\" content=\"Bernhard Bauder\">\n" + // 47
              "  <meta name=\"description\" content=\"Hier finden Sie ausgewlte Einstiger- und Profi- Entwicklungstools die Ihnen den Entwicklungsprozess beschleunigen bzw. vereinfachen knen. Ausgewlt wurden Tools die im direktem Zusammenhang mit der tlichen Entwicklungsarbeit von PHP und MySQL stehen.\">                                 \n"
              + "  <meta name=\"Keywords\" content=\"PHP, php4, templates, MYSQL, tools, Syntax-Highlighting, Editoren, irc, java, Software, Jobs, webmaster, bewerben, resource,ressource,download, Stellenanzeigen\">   ").getBytes();
   }

   @Test
   public void test4() {
      log.info("test4");
      BufferReaderWriter reader = new BufferReaderWriter(metaFragment());
      ByteSearch keywords = ByteSearch.create("\\sname\\s*=\\s*(keywords|description|'keywords'|'description'|\"keywords\"|\"description\")");
      ByteSearch starttag = ByteSearch.create("<meta(?=\\s)");
      ByteSearch endtag = ByteSearch.create(">").QuoteSafe();
      ByteSection metatag = new ByteSection(starttag, endtag);
      ByteSearch content = ByteSearch.create("\\scontent\\s*=\\s*\\Q");
      ByteSearch quote = ByteSearch.create("['\"]");
      ArrayList<String> results = new ArrayList<String>();
      do {
         log.info("readSection");
         ByteSearchSection section = reader.readSection(metatag);
         log.info("%s", section);
         if (!section.found()) {
            break;
         }
         log.info("end readSection");

         section = new ByteSearchSection(section.haystack, section.start, section.innerstart-1,
         section.innerend, section.end);
         if (keywords.exists(reader.buffer, section.innerstart, section.innerend)) {
               ByteSearchPosition c = content.findPos(reader.buffer, section.innerstart, section.innerend);
               if (c.found()) {
                  int cstart = quote.find(reader.buffer, c.start+8, c.end) + 1;
                  results.add(ByteTools.toString(reader.buffer, cstart, c.end - 1));
               }
         }
      } while (true);
      log.info("%s", results);
      log.info("end test4");
   }

   @Test
   public void test5() {
      log.info("test5");
      byte text[] = ("  content=\"aap\"").getBytes();
      ByteSearch endtag = ByteSearch.create("\\scontent\\s*=\\s*\\Q");
      ByteSearchPosition end = endtag.findPos(text, 0, text.length);
      log.info("%s %d %d", endtag.getClass().getSimpleName(), end.start, end.end);
      assertEquals(1, end.start);
      log.info("end test5");
   }

   @Test
   public void testFind() {
      log.info("testFind");
      String text = " WARC-TREC-ID: aap\n ";
      ByteSearch warcIDTag = ByteSearch.create("WARC\\-TREC\\-ID: ");
      ByteSearch eol = ByteSearch.create("\n");
      ByteSection warcID = new ByteSection(warcIDTag, eol);
      String match = warcID.getFirstString(text);
      assertEquals("aap", match);
      log.info("end testFind");
   }

   @Test
   public void testEnd() {
      log.info("testEnd");
      String text = " WARC-TREC-ID: aap\n ";
      ByteSearch warcIDTag = ByteSearch.create("WARC\\-TREC\\-ID: ");
      ByteSearch eol = ByteSearch.create("\n");
      ByteSection warcID = new ByteSection(warcIDTag, eol);
      String match = warcID.getFirstString(text);
      assertEquals("aap", match);
      log.info("end testEnd");
   }

   @Test
   public void testByteSectionScanned () {
      log.info("testByteSectionScanned");
      byte [] haystack = " a1b aab abb a11b aaab abbb aabb ".getBytes();
      int end = haystack.length;
      ByteSection s = new ByteSectionScanned("a", "1", "b");
      ArrayList<ByteSearchSection> findAllSections = s.findAllSections(haystack, 0, end);
      assertEquals( 5, findAllSections.size());
      s = new ByteSectionScanned("a", "a", "b");
      findAllSections = s.findAllSections(haystack, 0, end);
      assertEquals( 7, findAllSections.size());
      s = new ByteSection("a", "b");
      findAllSections = s.findAllSections(haystack, 0, end);
      assertEquals( 7, findAllSections.size());
      log.info("end testByteSectionScanned");
   }

   @Test
   public void testConfiguration() {
      log.info("testConfiguration");
      ByteRegex configuration = new ByteRegex("\\+?\\c\\w*(\\.\\c\\w*)+=\\S*$");
      String command = "param1 2 mapreduce.job.priority=HIGH retriever.strategy=AAP";
      String args[] = command.split(" ");
      assertEquals(true, configuration.startsWith(args[2]));
      assertEquals(true, configuration.startsWith(args[3]));
      assertEquals(false, configuration.startsWith(args[0]));
      assertEquals(false, configuration.startsWith(args[1]));
      log.info("end testConfiguration");
   }

}
