package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.Vector;

/**
 * LancasterStemmer: Implements the Lancaster (Paice/Husk) word stemmer.
 *
 * <p>
 * Paice/Husk Stemmer - License Statement.
 * </p>
 *
 * <p>
 * This software was designed and developed at Lancaster University, Lancaster,
 * UK, under the supervision of Dr Chris Paice. It is fully in the public
 * domain, and may be used or adapted by any organisation or individual. Neither
 * Dr Paice nor Lancaster University accepts any responsibility whatsoever for
 * its use by other parties, and makes no guarantees, expressed or implied,
 * about its quality, reliability, or any other characteristic.
 * </p>
 *
 * <p>
 * It is assumed that, as a matter of professional courtesy, anyone who
 * incorporates this software into a system of their own, whether for commercial
 * or research purposes, will acknowledge the source of the code.
 * </p>
 *
 * <p>
 * Modified from the original Java programs written by Christopher O'Neill and
 * Rob Hooper for use in WordHoard.
 * </p>
 */
public class LancasterStemmer {

    public static Log log = new Log(LancasterStemmer.class);
    private static LancasterStemmer singleton;

    /**
     * Prefixes to remove from words before stemming.
     */
    public static final String[] prefixes
            = {
                "intra",
                "kilo",
                "mega",
                "micro",
                "milli",
                "nano",
                "pico",
                "pseudo",
                "ultra",};

    /**
     * Default stemming rules.
     *
     * <p>
     * These rules MUST be stored in ascending alphanumeric order of the first
     * character.
     * </p>
     */
    public static final String[] defaultStemmingRules
            = new String[]{
                "ai*2.     { -ia > -   if intact }",
                "a*1.      { -a > -    if intact }",
                "bb1.      { -bb > -b   }",
                "city3s.   { -ytic > -ys }",
                "ci2>      { -ic > -    }",
                "cn1t>     { -nc > -nt  }",
                "dd1.      { -dd > -d   }",
                "dei3y>    { -ied > -y  }",
                "deec2ss.  { -ceed > -cess }",
                "dee1.     { -eed > -ee }",
                "de2>      { -ed > -    }",
                "dooh4>    { -hood > -  }",
                "e1>       { -e > -     }",
                "feil1v.   { -lief > -liev }",
                "fi2>      { -if > -    }",
                "gni3>     { -ing > -   }",
                "gai3y.    { -iag > -y  }",
                "ga2>      { -ag > -    }",
                "gg1.      { -gg > -g   }",
                "ht*2.     { -th > -   if intact }",
                "hsiug5ct. { -guish > -ct }",
                "hsi3>     { -ish > -   }",
                "i*1.      { -i > -    if intact }",
                "i1y>      { -i > -y    }",
                "ji1d.     { -ij > -id   --  see nois4j> & vis3j> }",
                "juf1s.    { -fuj > -fus }",
                "ju1d.     { -uj > -ud  }",
                "jo1d.     { -oj > -od  }",
                "jeh1r.    { -hej > -her }",
                "jrev1t.   { -verj > -vert }",
                "jsim2t.   { -misj > -mit }",
                "jn1d.     { -nj > -nd  }",
                "j1s.      { -j > -s    }",
                "lbaifi6.  { -ifiabl > - }",
                "lbai4y.   { -iabl > -y }",
                "lba3>     { -abl > -   }",
                "lbi3.     { -ibl > -   }",
                "lib2l>    { -bil > -bl }",
                "lc1.      { -cl > c    }",
                "lufi4y.   { -iful > -y }",
                "luf3>     { -ful > -   }",
                "lu2.      { -ul > -    }",
                "lai3>     { -ial > -   }",
                "lau3>     { -ual > -   }",
                "la2>      { -al > -    }",
                "ll1.      { -ll > -l   }",
                "mui3.     { -ium > -   }",
                "mu*2.     { -um > -   if intact }",
                "msi3>     { -ism > -   }",
                "mm1.      { -mm > -m   }",
                "nois4j>   { -sion > -j }",
                "noix4ct.  { -xion > -ct }",
                "noi3>     { -ion > -   }",
                "nai3>     { -ian > -   }",
                "na2>      { -an > -    }",
                "nee0.     { protect  -een }",
                "ne2>      { -en > -    }",
                "nn1.      { -nn > -n   }",
                "pihs4>    { -ship > -  }",
                "pp1.      { -pp > -p   }",
                "re2>      { -er > -    }",
                "rae0.     { protect  -ear }",
                "ra2.      { -ar > -    }",
                "ro2>      { -or > -    }",
                "ru2>      { -ur > -    }",
                "rr1.      { -rr > -r   }",
                "rt1>      { -tr > -t   }",
                "rei3y>    { -ier > -y  }",
                "sei3y>    { -ies > -y  }",
                "sis2.     { -sis > -s  }",
                "si2>      { -is > -    }",
                "ssen4>    { -ness > -  }",
                "ss0.      { protect  -ss }",
                "suo3>     { -ous > -   }",
                "su*2.     { -us > -   if intact }",
                "s*1>      { -s > -    if intact }",
                "s0.       { -s > -s    }",
                "tacilp4y. { -plicat > -ply }",
                "ta2>      { -at > -    }",
                "tnem4>    { -ment > -  }",
                "tne3>     { -ent > -   }",
                "tna3>     { -ant > -   }",
                "tpir2b.   { -ript > -rib }",
                "tpro2b.   { -orpt > -orb }",
                "tcud1.    { -duct > -duc }",
                "tpmus2.   { -sumpt > -sum }",
                "tpec2iv.  { -cept > -ceiv }",
                "tulo2v.   { -olut > -olv }",
                "tsis0.    { protect  -sist }",
                "tsi3>     { -ist > -   }",
                "tt1.      { -tt > -t   }",
                "uqi3.     { -iqu > -   } ",
                "ugo1.     { -ogu > -og }",
                "vis3j>    { -siv > -j  }",
                "vie0.     { protect  -eiv }",
                "vi2>      { -iv > -    }",
                "ylb1>     { -bly > -bl }",
                "yli3y>    { -ily > -y  }",
                "ylp0.     { protect  -ply }",
                "yl2>      { -ly > -    }",
                "ygo1.     { -ogy > -og }",
                "yhp1.     { -phy > -ph }",
                "ymo1.     { -omy > -om }",
                "ypo1.     { -opy > -op }",
                "yti3>     { -ity > -   }",
                "yte3>     { -ety > -   }",
                "ytl2.     { -lty > -l  }",
                "yrtsi5.   { -istry > - }",
                "yra3>     { -ary > -   }",
                "yro3>     { -ory > -   }",
                "yfi3.     { -ify > -   }",
                "ycn2t>    { -ncy > -nt }",
                "yca3>     { -acy > -   }",
                "zi2>      { -iz > -    }",
                "zy1s.     { -yz > -ys  }",
                "end0."
            };

    /**
     * Character for "0" digit.
     */
    protected final static char zeroDigit = '0';

    /*	Array of rules. */
    protected Vector ruleTable;

    /*	Index to rule table.
     *
     *	<p>
     *	For each letter 'a' through 'z', contains the index in
     *	ruleTable for the first rule beginning with the
     *	corresponding letter.  Position 0 is for letter 'a',
     *	position 1 for letter 'b', and so on.  In the default table above,
     *	ruleTableIndex[ 0 ] = 0, ruleTableIndex[ 1 ] = 2, etc.
     *	The index for Letters without a rule are assigned the index
     *	of the next letter which has a rule.
     *	</p>
     */
    protected int[] ruleTableIndex;

    /*	True to remove prefixes when word length is greater than two. */
    protected boolean preStrip;

    /**
     * Create a Paice/Husk stemmer using the default stemming rules.
     *
     * @throws	StemmerException if something goes wrong.
     *
     * <p>
     * Prefixes are automatically removed from words with more than two
     * characters.
     * </p>
     */
    private LancasterStemmer() {
        this.preStrip = true;

        loadRules(defaultStemmingRules);
    }

    /**
     * Create a Paice/Husk stemmer from a string list of rules.
     *
     * @param	rules	The stemming rules as an array of String.
     *
     * <p>
     * Prefixes are automatically removed from words with more than two
     * characters.
     * </p>
     */
    private LancasterStemmer(String[] rules) {
        this.preStrip = true;

        loadRules(rules);
    }

    /**
     * Create a Paice/Husk stemmer from a string list of rules.
     *
     * @param	rules	The stemming rules as an array of String.
     * @param	preStrip	True to remove prefixes from words with more than two
     * characters.
     *
     * <p>
     * Prefixes are automatically removed from words with more than two
     * characters.
     * </p>
     */
    private LancasterStemmer(String[] rules, boolean preStrip) {
        this.preStrip = preStrip;

        loadRules(rules);
    }

    public static LancasterStemmer get() {
        if (singleton == null)
            singleton = new LancasterStemmer();
        return singleton;
    }
    
    /**
     * Loads the stemming rules.
     *
     * @param	rules	String array of rules.
     */
    protected void loadRules(String[] rules) {
        //	Table of rules.

        ruleTable = new Vector();

        //	Maps letter to index of first rule
        //	in rule table starting with that letter.
        ruleTableIndex = new int[26];

        for (int i = 0; i < 25; i++) {
            ruleTableIndex[i] = 0;
        }
        //	Loop over rules and add each
        //	to rule table.

        for (int i = 0; i < rules.length; i++) {
            //	Remove blanks from rule and add it
            //	to rule table.

            ruleTable.addElement(rules[i].replaceAll(" ", ""));
        }
        //	Get starting index of rule
        //	for each letter.  Letters without
        //	any rules get the index of the
        //	next letter with a rule.
        char ch = 'a';

        for (int i = 0; i < (rules.length - 1); i++) {
            while (((String) ruleTable.elementAt(i)).charAt(0) != ch) {
                ch++;
                ruleTableIndex[charCode(ch)] = i;
            }
        }
    }

    /**
     * Returns index of first vowel in string.
     *
     * @param	s	String to search for vowel.
     * @param	last	Last position to search for vowel.
     *
     * @return	Zero-based index of first vowel in string.
     */
    protected int firstVowel(String s, int last) {
        char prevChar = 'a';
        int i;

        for (i = 0;
                (i < last)
                && (!(vowel(s.charAt(i), prevChar)));
                i++) {
            prevChar = s.charAt(i);
        }

        return Math.min(i, last);
    }

    /**
     * Strip suffixes from a string.
     *
     * @param	s	The string from which to remove suffixes.
     *
     * @return	The string with suffixes removed.
     */
    protected String stripSuffixes(String s) {
        //	Is the current rule OK.

        int ruleOK = 0;

        //	Are we done stemming a string.
        int done = 0;

        //	Position of last letter in string.
        int lastLetterPos = 0;

        //	Counter for number of characters
        //	to be replaced and length of stemmed
        //	string if rule was applied.
        int replacedCharCount = 0;

        //	Position of first vowel in string.
        int firstVowelPos = 0;

        //	Index into rule table.
        int currentRuleIndex = 0;

        //	Index of current rule.
        int ruleCharPos = 0;

        //	Index of word.
        int wordCharPos = 0;

        //	Last letter in string.
        char lastLetter = 0;

        //	Holds current stemming rule.
        String rule = "";

        //	True if the input string has not yet
        //	been stemmed.
        boolean intact = true;

        //	"stem" contains the stemmed input
        //	string in as the stemming process
        //	proceeds.
        //
        //	Start by cleaning the input string
        //	of non-letters.
        String stem = clean(s.toLowerCase());

        //	Set lastLetterPos to the index of the
        //	last letter in the string.  Normally
        //	we will have removed all non-letters
        //	from the string before we get here,
        //	so usually posLastletter will just be
        //	one less than the length of the string.
        lastLetterPos = 0;

        while (((lastLetterPos + 1) < stem.length())
                && isLetter(stem.charAt(lastLetterPos + 1))) {
            lastLetterPos++;
        }

        if (lastLetterPos < 1) {
            done = -1;
        } else //	Find position of first vowel in string.
        {
            firstVowelPos = firstVowel(stem, lastLetterPos);
            wordCharPos = stem.length() - 1;
        }
        //	Repeat rule processing until
        //	no more rules apply, i.e.,
        //	stemming is complete.

        while (done != -1) {
            //	Look for rule for new final letter.
            done = 0;
            //	Get last letter in string.

            lastLetter = stem.charAt(lastLetterPos);

            //	Are there are any possible rules
            //	for stemming for this letter?
            if (isLetter(lastLetter)) {
                currentRuleIndex = ruleTableIndex[charCode(lastLetter)];
            } else {
                currentRuleIndex = -1;
            }
            //	No rule available -- stemming done.

            if (currentRuleIndex == -1) {
                done = -1;
                continue;
            }
            //	Pick up first pontentially matching
            //	rule.

            rule = (String) ruleTable.elementAt(currentRuleIndex);

            while (done == 0) {
                ruleOK = 0;

                if (rule.charAt(0) != lastLetter) {
                    //	Rule letter changed.  We're done
                    //	with this letter.

                    done = -1;
                    ruleOK = -1;
                }
                //	Index in rule: second character.

                ruleCharPos = 1;

                //	Index in stemmed string:
                //	next to last letter.
                wordCharPos = lastLetterPos - 1;

                //	Loop over rules and try to find
                //	a rule that is acceptable.
                while (ruleOK == 0) {
                    //	Is rule fully matched?

                    if (isDigit(rule.charAt(ruleCharPos))) {
                        ruleOK = 1;
                    } else if (rule.charAt(ruleCharPos) == '*') {
                        //	Match only if word intact.

                        if (intact) {
                            //	Move forwards in rule.

                            ruleCharPos++;
                            ruleOK = 1;
                        } else {
                            ruleOK = -1;
                        }
                    } //	Mismatch of letters.
                    else if (rule.charAt(ruleCharPos)
                            != stem.charAt(wordCharPos)) {
                        ruleOK = -1;
                    } //	Insufficient stem remaining.
                    else if (wordCharPos <= firstVowelPos) {
                        ruleOK = -1;
                    } //	Compare next pair of letters.
                    //	Move forwards in rule and
                    //	backwards in string.
                    else {
                        ruleCharPos++;
                        wordCharPos--;
                    }
                }
                //	If the rule that has just been checked
                //	is valid for the current stem value,
                //	check the acceptability conditions
                //	for the current stem value.

                if (ruleOK == 1) {
                    //	Count replacement letters.

                    replacedCharCount = 0;

                    while (!((rule.charAt(
                            ruleCharPos + replacedCharCount + 1)
                            >= '.')
                            && (rule.charAt(
                                    ruleCharPos + replacedCharCount + 1)
                            <= '>'))) {
                        replacedCharCount++;
                    }

                    replacedCharCount
                            = lastLetterPos + replacedCharCount + zeroDigit
                            - ((int) (rule.charAt(ruleCharPos)));

                    //	Position of last letter if rule used.
                    if (firstVowelPos == 0) {
                        //	If word starts with vowel...

                        if (replacedCharCount < 1) {
                            //	... minimal stem is 2 letters.

                            ruleOK = -1;
                        }
                    } //	If word starts with a consonant,
                    //	minimal stem is 3 letters
                    //	including one or more vowels.
                    else if ((replacedCharCount < 2)
                            || (replacedCharCount < firstVowelPos)) {
                        ruleOK = -1;
                    }
                }
                //	If using rule passes the assertion
                //	tests, apply the matching rule.

                if (ruleOK == 1) {
                    //	Input string is no longer intact.

                    intact = false;

                    //	Move end of string marker to position
                    //	given by the numeral in the rule.
                    lastLetterPos
                            = lastLetterPos + zeroDigit
                            - ((int) (rule.charAt(ruleCharPos)));

                    ruleCharPos++;

                    stem = stem.substring(0, (lastLetterPos + 1));

                    //	Append any letters following numeral
                    //	to the string.
                    while ((ruleCharPos < rule.length())
                            && isLetter(rule.charAt(ruleCharPos))) {
                        stem += rule.charAt(ruleCharPos);

                        ruleCharPos++;
                        lastLetterPos++;
                    }
                    //	Rule ends with '.'.  We're done.

                    if ((rule.charAt(ruleCharPos)) == '.') {
                        done = -1;
                    } else {
                        //	Here if rule ends with '>'.  Continue.

                        done = 1;
                    }
                } else {
                    //	Rule did not match.
                    //	Try next rule in rule table.

                    currentRuleIndex++;

                    rule
                            = (String) ruleTable.elementAt(currentRuleIndex);

                    //	When the initial letter changes,
                    //	there are no more rules to try.
                    if (rule.charAt(0) != lastLetter) {
                        done = -1;
                    }
                }
            }
        }

        return stem;
    }

    /**
     * Determine if character is a vowel or not.
     *
     * @param	ch	The potential vowel.
     *
     * @return	true if the character is a vowel (a, e, i, o, u).
     */
    protected boolean isVowel(char ch) {
        return (ch == 'a') || (ch == 'e') || (ch == 'i')
                || (ch == 'o') || (ch == 'u');
    }

    /**
     * Determine if character is a vowel or not.
     *
     * @param	ch	The potential vowel.
     * @param	prev	The previous character.
     *
     * @return	true if the character is a vowel.
     *
     * <p>
     * When the character is a "y", the previous character is checked to see if
     * it is a vowel. If so, "y" is not considered a vowel.
     * </p>
     */
    protected boolean vowel(char ch, char prev) {
        boolean result = isVowel(ch);

        if (!result && (ch == 'y')) {
            result = !isVowel(prev);
        }

        return result;
    }

    /**
     * Determine if character is a digit.
     *
     * @param	ch	The character to check.
     *
     * @return	true if "ch" is a digit ('0' .. '9').
     */
    protected boolean isDigit(char ch) {
        return ((ch >= '0') && (ch <= '9'));
    }

    /**
     * Determine if character is a letter.
     *
     * @param	ch	The character to check.
     *
     * @return	true if "ch" is a letter ('a' .. 'z').
     */
    protected boolean isLetter(char ch) {
        return ((ch >= 'a') && (ch <= 'z'));
    }

    /**
     * Converts a lower case letter to an index.
     *
     * @param	ch	The character. Must be in the range 'a' .. 'z'.
     *
     * @return	The index, where 'a' = 0 .
     */
    protected int charCode(char ch) {
        return ((int) ch) - 'a';
    }

    /**
     * Removes prefixes from a string.
     *
     * @param	s	The string from which to remove prefixes.
     *
     * @return	The string with prefixes removed.
     */
    protected String stripPrefixes(String s) {
        String result = s;
        String sLower = s.toLowerCase();

        //	Remove any prefix from string
        //	as long as the string is longer
        //	than the prefix.
        for (int i = 0; i < prefixes.length; i++) {
            if ((sLower.startsWith(prefixes[i]))
                    && (sLower.length() > prefixes[i].length())) {
                result = s.substring(prefixes[i].length());
                break;
            }
        }

        return result;
    }

    /**
     * Remove non-letters from a string.
     *
     * @param	s	String from which to remove non-letters.
     *
     * @return	String with non-letters removed.
     */
    protected String clean(String s) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            if (isLetter(s.charAt(i))) {
                result.append(s.charAt(i));
            }
        }

        return result.toString();
    }

    /**
     * Stem a specified string.
     *
     * @param	s	The string to stem.
     *
     * @return	The stemmed string.
     */
    public String stem(String s) {
        //	Copy input string to be stemmed.

        String result = s;

        //	Remove prefixes if the input string
        //	is longer than three characters and
        //	prefix stripping was requested.
        if ((result.length() > 3) && preStrip) {
            result = stripPrefixes(result);
        }
        //	Remove suffixes if the string
        //	is longer than three characters.

        if (result.length() > 3) {
            result = stripSuffixes(result);
        }

        return result;
    }

    public static void main(String[] args) {
        LancasterStemmer stemmer = new LancasterStemmer();
        for (String term : new String[]{"man", "european"}) {
            log.info("%s %s", term, stemmer.stem(term));
        }
    }
}
