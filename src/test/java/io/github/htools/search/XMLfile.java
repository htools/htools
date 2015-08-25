package io.github.htools.search;

import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextFile;
import io.github.htools.io.struct.StructuredTextXML;
import io.github.htools.lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class XMLfile extends StructuredTextXML {

   public static Log log = new Log(XMLfile.class);
   public FolderNode session = getRoot();
   public IntField sessionnum = this.addInt(session, "num");
   public StringField sessionstarttime = this.addString(session, "starttime");
   public FolderNode topic = this.addNode(session, "topic");
   public StringField title = this.addString(topic, "title");
   public StringField description = this.addString(topic, "desc");
   public StringField narrative = this.addString(topic, "narr");
   public FolderNode interaction = this.addNode(session, "interaction");
   public IntField interactionnum = this.addInt(interaction, "num");
   public StringField interactionstarttime = this.addString(interaction, "starttime");
   public StringField interactionquery = this.addString(interaction, "query");
   public FolderNode results = this.addNode(interaction, "results");
   public FolderNode result = this.addNode(results, "result");
   public IntField rank = this.addInt(result, "rank");
   public StringField url = this.addString(result, "url");
   public StringField clueweb09id = this.addString(result, "clueweb09id");
   public StringField resulttitle = this.addString(result, "title");
   public StringField snippet = this.addString(result, "snippet");
   public FolderNode clicked = this.addNode(interaction, "clicked");
   public FolderNode click = this.addNode(clicked, "click");
   public IntField clicknum = this.addInt(click, "num");
   public StringField clickstarttime = this.addString(click, "starttime");
   public StringField clickendtime = this.addString(click, "endtime");
   public IntField clickrank = this.addInt(click, "rank");
   public FolderNode currentquery = this.addNode(session, "currentquery");
   public StringField currenttime = this.addString(currentquery, "starttime");
   public StringField query = this.addString(currentquery, "query");

   public XMLfile(BufferReaderWriter r) {
      super(r);
   }

   public XMLfile(Datafile r) {
      super(r);
   }

   @Override
   public FolderNode createRoot() {
      return addNode(null, "session");
   }

   public static void main(String[] args) {
      XMLfile f = new XMLfile(new Datafile("/home/jer/Desktop/st11.topics.txt"));
      f.datafile.setBufferSize((int) f.datafile.getLength());
      f.openRead();
      while (f.nextRecord()) {
         log.info("%s %s", f.title.get(), f.sessionstarttime.get());
         log.info("%d", f.click.size());
         for (NodeValue i : f.interaction) {
            log.info("interaction %d", i.get(f.interactionnum));
            if (i.get(f.results) != null) {
               for (NodeValue r : i.get(f.results).getListNode(f.result)) {
                  log.info("rank %s", r.get(f.rank)); 
               }
            }
         }
      }
   }

   public static String getXMLData() {
      return "   <session num=\"17\" starttime=\"10:21:55.141117\">\n"
              + "      <topic>\n"
              + "         <title>berkley insurance</title>\n"
              + "         <desc>Find information on berkley insurance</desc>\n"
              + "         <narr>When was the company founded? who founded it? What is the company profile? What is the company's history? What insurance products do they sell?</narr>\n"
              + "      </topic>\n"
              + "      <interaction num=\"1\" starttime=\"10:22:41.233975\">\n"
              + "         <query>berkley insurance</query>\n"
              + "         <results>\n"
              + "            <result rank=\"1\">\n"
              + "               <url>http://www.berkely.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0007-88-36069</clueweb09id>\n"
              + "               <title>Berkely Group</title>\n"
              + "               <snippet>Administers passenger protection programs, travel accident insurance for cruise lines, tour operators and other travel organizations.</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"2\">\n"
              + "               <url>http://www.wrbeurope.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0042-01-30629</clueweb09id>\n"
              + "               <title>W. R. Berkley Insurance (Europe), Limited</title>\n"
              + "               <snippet>W. R. Berkley Insurance (Europe), Limiteds (WRBIEL) global insurance model is founded ... W. R. Berkley Insurance (Europe), Limited is rated A (Excellent) by A. M. Best ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"3\">\n"
              + "               <url>http://www.berkleysum.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0034-02-07570</clueweb09id>\n"
              + "               <title>Berkley Specialty Underwriting Managers</title>\n"
              + "               <snippet>Berkley Specialty Underwriting Managers, a subsidiary of the , ... insurance products in the specialty insurance market while maintaining the highest level of integrity, ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"4\">\n"
              + "               <url>http://www.berkleyah.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0005-81-13147</clueweb09id>\n"
              + "               <title>Berkley Accident and Health - Home Page</title>\n"
              + "               <snippet>Berkley Accident and Health, LLC is a direct-writer for a broad range of accident &amp;amp; health insurance products &amp;amp; services. Our product lines include: ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"5\">\n"
              + "               <url>http://www.referenceforbusiness.com/history/Vi-Z/W-R-Berkley-Corporation.html</url>\n"
              + "               <clueweb09id>clueweb09-en0010-13-05677</clueweb09id>\n"
              + "               <title>W.R. Berkley Corporation Company Profile, Information ...</title>\n"
              + "               <snippet>W.R. Berkley Corporation is an insurance holding company with five ... Berkley achieved above average returns from his insurance companies during the 1970s and ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"6\">\n"
              + "               <url>http://www.brac-ins.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0077-94-09415</clueweb09id>\n"
              + "               <title>Berkley Risk Administrators Company</title>\n"
              + "               <snippet>Berkley Risk. Through BRACs Berkley Risk division, it provides bundled and unbundled program management and insurance services on a nationwide basis. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"7\">\n"
              + "               <url>http://dentalinsurance.dentalplans.com/berkley/index.html</url>\n"
              + "               <clueweb09id>clueweb09-en0044-53-21557</clueweb09id>\n"
              + "               <title>Berkley Dental Insurance Alternatives from Dental Plans .com</title>\n"
              + "               <snippet>Save on dental checkups, teeth cleanings, braces and more with dental discount plans, alternatives to Berkley dental insurance for individuals and families. Discount ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"8\">\n"
              + "               <url>http://www.fundinguniverse.com/company-histories/WR-Berkley-Corp-Company-History.html</url>\n"
              + "               <clueweb09id>clueweb09-en0022-59-35426</clueweb09id>\n"
              + "               <title>W.R. Berkley Corp. -- Company History</title>\n"
              + "               <snippet>W.R. Berkley Corp. is an insurance holding company active in four ... Berkleys strategy in the insurance business during the 1970s was multi-faceted. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"9\">\n"
              + "               <url>http://www.berkleyrisk.com/policy-issuing-carriers.html</url>\n"
              + "               <clueweb09id>clueweb09-en0004-36-27527</clueweb09id>\n"
              + "               <title>Policy Issuing Carriers - Risk Management Services</title>\n"
              + "               <snippet>Therefore, we also partner with a number of other insurance companies, including W. R. Berkley Corporation insurance companies as well as non-owned companies. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"10\">\n"
              + "               <url>http://peoplesfirstinsurance.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0116-21-09633</clueweb09id>\n"
              + "               <title>Peoples First Insurance &#x2014; Rock Hill, SC</title>\n"
              + "               <snippet>Whether your need is personal insurance, protection for your business, or an ... Amerisure Insurance. 800-532-6230. Berkley. 800-283-1153. Builders Mutual ...</snippet>\n"
              + "            </result>\n"
              + "         </results>\n"
              + "         <clicked>\n"
              + "            <click num=\"1\" starttime=\"10:22:46.734005\" endtime=\"10:22:59.332772\">\n"
              + "               <rank>1</rank>\n"
              + "            </click>\n"
              + "         </clicked>\n"
              + "      </interaction>\n"
              + "      <interaction num=\"2\" starttime=\"10:23:14.342218\">\n"
              + "         <query>berkley insurance about us</query>\n"
              + "         <results>\n"
              + "            <result rank=\"1\">\n"
              + "               <url>http://www.wrbeurope.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0042-01-30629</clueweb09id>\n"
              + "               <title>W. R. Berkley Insurance (Europe), Limited</title>\n"
              + "               <snippet>W. R. Berkley Insurance (Europe), Limiteds (WRBIEL) global insurance model is founded on three essential ... by W. R. Berkley Corporation, which has over US$16.8 billion in ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"2\">\n"
              + "               <url>http://www.wrberkley.com/our_business/berkley_accident/index.shtml</url>\n"
              + "               <clueweb09id>clueweb09-en0013-70-17312</clueweb09id>\n"
              + "               <title>W. R. Berkley Corporation</title>\n"
              + "               <snippet>About W. R. Berkley Corporation. W. R. Berkley Corporation, founded in 1967, is one of the nations premier commercial lines property casualty insurance providers. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"3\">\n"
              + "               <url>http://www.dentalliability.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0131-66-02054</clueweb09id>\n"
              + "               <title>Berkley Risk Services of Colorado</title>\n"
              + "               <snippet>Multiline Insurance Agency with Life, Health, Auto, BOP, General Liability, Commerical Property, General Liability, Employment ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"4\">\n"
              + "               <url>http://www.berkleysum.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0034-02-07570</clueweb09id>\n"
              + "               <title>Berkley Specialty Underwriting Managers</title>\n"
              + "               <snippet>Berkley Specialty Underwriting Managers, a subsidiary of the , operates as an ... and responsive provider of insurance products in the specialty insurance ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"5\">\n"
              + "               <url>http://www.berkely.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0007-88-36069</clueweb09id>\n"
              + "               <title>Berkely Group</title>\n"
              + "               <snippet>Administers passenger protection programs, travel accident insurance for cruise lines, tour operators and other travel organizations.</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"6\">\n"
              + "               <url>http://www.berkleyrisk.com/about-us.html</url>\n"
              + "               <clueweb09id>clueweb09-en0077-30-07670</clueweb09id>\n"
              + "               <title>Berkley Risk - Risk Management Services</title>\n"
              + "               <snippet>About Us. Berkley Risk operates as a division of Berkley Risk ... Additionally, we structure loss sensitive insurance products which allow our customers to ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"7\">\n"
              + "               <url>http://www.berkleyrisk.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0004-36-27525</clueweb09id>\n"
              + "               <title>Berkley Risk - Risk Management Services</title>\n"
              + "               <snippet>Traditional insurance programs benefit the insurance company. Berkley Risk programs ... Berkley Risk is a nationwide provider of insurance management services. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"8\">\n"
              + "               <url>http://www.berkleysum.com/about_us.htm</url>\n"
              + "               <clueweb09id>clueweb09-en0020-84-04831</clueweb09id>\n"
              + "               <title>About Us</title>\n"
              + "               <snippet>About Us. Berkley Specialty Underwriting Managers consists of three units. ... and responsive provider of insurance products in the specialty insurance ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"9\">\n"
              + "               <url>http://www.berkleyah.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0005-81-13147</clueweb09id>\n"
              + "               <title>Berkley Accident and Health - Home Page</title>\n"
              + "               <snippet>ABOUT US. Company At A Glance. Company Leadership. Competitive Advantage ... Berkley Accident and Health, LLC is a direct-writer for a broad range of accident ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"10\">\n"
              + "               <url>http://www.americanmining.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0004-51-05644</clueweb09id>\n"
              + "               <title>American Mining Insurance Company: A W. R. Berkley Company</title>\n"
              + "               <snippet>American Mining Insurance Company provides multi-line coverage for mining and mine related companies.</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"11\">\n"
              + "               <url>http://www.hillusher.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0050-45-36750</clueweb09id>\n"
              + "               <title>Hill &amp;amp; Usher. Insurance. Bonds. Benefits.</title>\n"
              + "               <snippet>Leading insurance and surety agency with a complete line of commercial- and personal-insurance and surety products. Licensed in most states.</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"12\">\n"
              + "               <url>http://www.clermonthid.com/about.html</url>\n"
              + "               <clueweb09id>clueweb09-en0049-44-17464</clueweb09id>\n"
              + "               <title>About Us</title>\n"
              + "               <snippet>About Us. Clermont Specialty Managers, Ltd. is an underwriting arm of Admiral Indemnity ... Insurance Company, member companies of W.R. Berkley Corporation. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"13\">\n"
              + "               <url>http://www.deanshomer.com/companies.php</url>\n"
              + "               <clueweb09id>clueweb09-en0005-39-15972</clueweb09id>\n"
              + "               <title>Companies</title>\n"
              + "               <snippet>about us. About Deans &amp;amp; Homer. Insurance Companies We Represent ... Berkley Insurance Company. A+. XV. A+. 1993. 35408. Delos America Insurance Company. A- VIII. NR ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"14\">\n"
              + "               <url>http://www.carolinacas.com/resources.htm</url>\n"
              + "               <clueweb09id>clueweb09-en0005-11-32931</clueweb09id>\n"
              + "               <title>Carolina Casualty Insurance Company Resources</title>\n"
              + "               <snippet>Visit our site to learn more about our business and to find the nearest available agent. ... Contact Us. Employment Opportunities. Resources. W. R. Berkley Corporation - Our parent ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"15\">\n"
              + "               <url>http://www.lifeinsurance.net/insurance-carriers-name-changes-2000.htm</url>\n"
              + "               <clueweb09id>clueweb09-en0107-46-20500</clueweb09id>\n"
              + "               <title>Life and Health Insurance Carriers - Name Changes 2000 - Life ...</title>\n"
              + "               <snippet>Quotes from local agents on whole and term life insurance policies. Insurance education, calculators, articles and company directory. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"16\">\n"
              + "               <url>http://www.carolinacas.com/about.htm</url>\n"
              + "               <clueweb09id>clueweb09-en0005-11-32922</clueweb09id>\n"
              + "               <title>Carolina Casualty Insurance Company About Us</title>\n"
              + "               <snippet>For more information about W. R. Berkley Corporation, please visit their website. ... For more information about how to access us, view our Contact Information. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"17\">\n"
              + "               <url>http://www.referenceforbusiness.com/history/Vi-Z/W-R-Berkley-Corporation.html</url>\n"
              + "               <clueweb09id>clueweb09-en0010-13-05677</clueweb09id>\n"
              + "               <title>W.R. Berkley Corporation Company Profile, Information ...</title>\n"
              + "               <snippet>W.R. Berkley Corporation is an insurance holding company with five ... Berkleys strategy in the insurance business during the 1970s was multi-faceted. ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"18\">\n"
              + "               <url>http://www.keystoneinsgrp.com/carriers_say.html</url>\n"
              + "               <clueweb09id>clueweb09-en0051-88-26386</clueweb09id>\n"
              + "               <title>What our carriers say - Keystone Insurers Group</title>\n"
              + "               <snippet>What our carriers say is very important to us. We would like to spread the word on what others think about KIG. Choose a company from the list below to hear what they ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"19\">\n"
              + "               <url>http://cwgins.com/</url>\n"
              + "               <clueweb09id>clueweb09-en0058-30-09314</clueweb09id>\n"
              + "               <title>Welcome to CWG</title>\n"
              + "               <snippet>Do you have questions about the Agency Portal? Click here to find the ... Berkley Corporation&#xAE;, recognized as one of the most respected names in the property casualty insurance ...</snippet>\n"
              + "            </result>\n"
              + "            <result rank=\"20\">\n"
              + "               <url>http://dentalinsurance.dentalplans.com/berkley/index.html</url>\n"
              + "               <clueweb09id>clueweb09-en0044-53-21557</clueweb09id>\n"
              + "               <title>Berkley Dental Insurance Alternatives from Dental Plans .com</title>\n"
              + "               <snippet>Save on dental checkups, teeth cleanings, braces and more with dental discount plans, alternatives to Berkley dental insurance for individuals and families. Discount ...</snippet>\n"
              + "            </result>\n"
              + "         </results>\n"
              + "         <clicked>\n"
              + "            <click num=\"1\" starttime=\"10:23:34.254990\" endtime=\"10:23:46.993593\">\n"
              + "               <rank>6</rank>\n"
              + "            </click>\n"
              + "         </clicked>\n"
              + "      </interaction>\n"
              + "      <currentquery starttime=\"10:24:26.046853\">\n"
              + "         <query>\"berkley insurance\" \"about us\"</query>\n"
              + "      </currentquery>\n"
              + "   </session>";
   }

}
