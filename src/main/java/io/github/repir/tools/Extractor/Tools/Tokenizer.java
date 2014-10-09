package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import java.util.ArrayList;

/**
 * A configurable tokenizer, that processes a section of byte content, storing
 * the tokens identified in the designated {@link EntityChannel}. The following
 * configuration settings can be used to control how tokens are identified.
 * <ul>
 * <li>extractor.sectionname.splittokenbefore : characters that indicate a new
 * token may start </li>
 * <li>extractor.sectionname.splittokenafter : characters that indicate a token
 * ends </li>
 * <li>extractor.sectionname.splitnumber=true/false : split when letters switch
 * to numbers and vice versa </li>
 * <li>extractor.sectionname.leavefirst : all other characters than these are
 * removed from the front of the token </li>
 * <li>extractor.sectionname.leavelast : all other characters than these are
 * removed from the end of the token </li>
 * <li>extractor.sectionname.maxtokenlength : all tokens that exceed this length
 * are not stored </li>
 * </ul>
 * For the configuration of characters, range =s may be specified, e.g. "A-Z
 * a-z". Whitespaces are added to splittokenbefore and splittokenafter by
 * default.
 *
 * @author jeroen
 */
public class Tokenizer extends ExtractorProcessor {

    public static Log log = new Log(Tokenizer.class);
    public int bufferpos = 0, bufferend = 0;
    public boolean[] tokensplitbefore = new boolean[128];
    public boolean[] tokensplitafter = new boolean[128];
    public boolean[] tokenpoststripper = new boolean[128];
    public boolean[] tokenprestripper = new boolean[128];
    public boolean[][] splitpeek = new boolean[128][128];
    public final char[] tokenchar = new char[256];
    protected final char maxbyte = Byte.MAX_VALUE;
    protected final char minbyte = 0x0;
    protected int maxtokenlength;
   // The following variables are for use within the tokenizer. Using class variables
    // is faster than using local variables.
    boolean readingToken;
    byte byte0, byte1;
    boolean splittoken = false, splitafter = false;
    boolean skip = false;

    public Tokenizer(Extractor extractor, String process) {
        this(extractor, process,
                extractor.getConfigurationString(process, "splittokenafter", ""),
                extractor.getConfigurationString(process, "splittokenbefore", ""),
                extractor.getConfigurationString(process, "leavefirst", ""),
                extractor.getConfigurationString(process, "leavelast", ""),
                extractor.getConfigurationBoolean(process, "splitnumbers", false),
                extractor.getConfigurationBoolean(process, "lowercase", false),
                extractor.getConfigurationInt(process, "maxtokenlength", Integer.MAX_VALUE));
    }

    public Tokenizer(Extractor extractor, String process, String splitbefore, String splitafter, String leavefirst, String leavelast, boolean splitnumbers, boolean lowercase, int maxtokenlength) {
        super(extractor, process);
        setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
        setBooleanArray(tokensplitafter, minbyte, maxbyte, false);
        addTokenSplitBefore("< \t\n\r");
        setBooleanArray(tokenpoststripper, minbyte, maxbyte, true);
        setBooleanArray(tokenpoststripper, 'A', 'Z', false);
        setBooleanArray(tokenpoststripper, '0', '9', false);
        setBooleanArray(tokenpoststripper, 'a', 'z', false);
        setBooleanArray(tokenpoststripper, false, minbyte);
        setBooleanArray(tokenprestripper, minbyte, maxbyte, true);
        setBooleanArray(tokenprestripper, 'A', 'Z', false);
        setBooleanArray(tokenprestripper, 'a', 'z', false);
        setBooleanArray(tokenprestripper, '0', '9', false);
        setBooleanArray(tokenprestripper, false, '.', '-', '$', '&');
        setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
        setBooleanArray(tokensplitbefore, true, ' ', '\t', '\n', '.', '!', '?', '-', ':', '"', '/', '\\');

        setBooleanArray(tokensplitbefore, minbyte, maxbyte, false);
        addTokenSplitBefore("\r\n\t ");
        addTokenSplitAfter("\r\n\t ");
        //log.info("tokensplitbefore %s", extractor.getConfigurationString(process, "splittokenafter", ""));
        addTokenSplitAfter(splitafter);
        addTokenSplitBefore(splitbefore);
        setBooleanArray(tokenpoststripper, minbyte, maxbyte, true);
        setBooleanArray(tokenprestripper, minbyte, maxbyte, true);
        setLeaveFirst(leavefirst);
        setLeaveLast(leavelast);
        if (splitnumbers) {
            setSplitNumbers();
        }
        this.maxtokenlength = maxtokenlength;
        setCharacterTranslation(lowercase);
    }

    protected void setLeaveFirst(String leavefirst) {
        String args[] = io.github.repir.tools.Lib.StrTools.split(leavefirst, " ");
        for (String set : args) {
            if (set.indexOf('-') == 1 && set.length() == 3) {
                setBooleanArray(tokenprestripper, set.charAt(0), set.charAt(2), false);
            } else {
                for (char c : set.toCharArray()) {
                    setBooleanArray(tokenprestripper, c, c, false);
                }
            }
        }
    }

    protected void setLeaveLast(String leavelast) {
        String args[] = io.github.repir.tools.Lib.StrTools.split(leavelast, " ");
        for (String set : args) {
            if (set.indexOf('-') == 1 && set.length() == 3) {
                setBooleanArray(tokenpoststripper, set.charAt(0), set.charAt(2), false);
            } else {
                for (char c : set.toCharArray()) {
                    setBooleanArray(tokenpoststripper, c, c, false);
                }
            }
        }
    }

    protected void setSplitNumbers() {
        this.splitpeek('a', 'z', '0', '9');
        this.splitpeek('A', 'Z', '0', '9');
        this.splitpeek('0', '9', 'a', 'z');
        this.splitpeek('0', '9', 'A', 'Z');
    }

    protected void setCharacterTranslation(boolean lowercase) {
        if (lowercase) {
            for (int i = 0; i < 256; i++) {
                tokenchar[i] = (char) ((i >= 'A' && i <= 'Z') ? (i | 32) : i);
            }
        } else {
            for (int i = 0; i < 256; i++) {
                tokenchar[i] = (char) i;
            }
        }
    }

    protected void splitpeek(char firststart, char firstend, char secondstart, char secondend) {
        for (int f = firststart; f <= firstend; f++) {
            for (int s = secondstart; s <= secondend; s++) {
                splitpeek[f][s] = true;
            }
        }
    }

    @Override
    public void process(Entity entity, Entity.Section section, String attribute) {
        this.bufferpos = section.open;
        this.bufferend = section.close;
        if (bufferpos >= bufferend) {
            return;
        }
        initialize();
        ArrayList<String> list = loadTokens(entity.content);
        entity.get(attribute).addAll(list);
    }

    protected void initialize() {
        readingToken = false;
        skip = false;
        splittoken = false;
    }

    public void setBooleanArray(boolean[] a, char from, char to, boolean s) {
        for (; from <= to; from++) {
            a[from] = s;
        }
    }

    public void setBooleanArray(boolean a[], boolean s, char... c) {
        for (char b : c) {
            a[b] = s;
        }
    }

    public void addTokenSplitBefore(String s) {
        byte tokensepbyte[] = s.getBytes();
        for (byte b : tokensepbyte) {
            this.tokensplitbefore[b] = true;
        }
    }

    public void addTokenSplitAfter(String s) {
        byte tokensepbyte[] = s.getBytes();
        for (byte b : tokensepbyte) {
            this.tokensplitafter[b] = true;
        }
    }

    protected ArrayList<String> loadTokens(byte buffer[]) {
        ArrayList<String> chunks = new ArrayList<String>();
        int pos = 0, tokenStart = 0;
        boolean inToken = false;
        byte peek = (bufferpos < bufferend) ? buffer[bufferpos] : 0;
        for (pos = bufferpos; pos < bufferend; pos++) {
            byte0 = peek;
            peek = (pos + 1 < bufferend) ? buffer[pos + 1] : 0;
            if (!inToken) {
                if (!this.tokenprestripper[byte0]) {
                    tokenStart = pos;
                    inToken = true;
                    if (this.tokensplitafter[byte0]) {
                        addToken(buffer, chunks, tokenStart, pos + 1);
                        inToken = false;
                    } else if (splitpeek[byte0][peek]) {
                        addToken(buffer, chunks, tokenStart, pos + 1);
                        tokenStart = pos + 1;
                    }
                }
            } else {
                if (this.tokensplitbefore[byte0]) {
                    addToken(buffer, chunks, tokenStart, pos);
                    tokenStart = pos;
                    inToken = !tokenprestripper[byte0];
                }
                if (this.tokensplitafter[byte0]) {
                    addToken(buffer, chunks, tokenStart, pos + 1);
                    inToken = false;
                } else if (splitpeek[byte0][peek]) {
                    addToken(buffer, chunks, tokenStart, pos + 1);
                    tokenStart = pos + 1;
                }
            }
        }
        if (inToken) {
            addToken(buffer, chunks, tokenStart, bufferend);
        }
        return chunks;
    }

    private void addToken(byte buffer[], ArrayList<String> list, int tokenStart, int tokenend) {
        char c[];
        while (--tokenend >= tokenStart && this.tokenpoststripper[buffer[tokenend]]);
        if (tokenend >= tokenStart) {
            int realchars = 0;
            for (int p = tokenStart; p <= tokenend; p++) {
                if (buffer[p] > 0) {
                    realchars++;
                }
            }
            if (realchars > 0 && realchars < maxtokenlength) {
                c = new char[realchars];
                for (int cnr = 0, p = tokenStart; p <= tokenend; p++) {
                    if (buffer[p] > 0) {
                        // by using tokenchar, characters can be automaticaly mapped, e.g. to lowercase
                        c[cnr++] = tokenchar[buffer[p] & 0xFF];
                    }
                }
                list.add(new String(c));
            }
        }
    }
}
