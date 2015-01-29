package io.github.repir.tools.Words;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.Words.englishStemmer;
import java.util.HashSet;

/**
 * Stop word list of 429 terms from http://www.lextek.com/manuals/onix/stopwords1.html
 * which is the original list of stop words Salton & Buckley orginally used for
 * the SMART system at Cornell University, which was slightly trimmed down.
 */
public class StopWordsSmart {
   public static Log log = new Log( StopWordsSmart.class );
   
   public static String filterarray[] = {
      "a", "about", "above", "across", "after", "again", "against", "all", "almost", "alone", 
      "along", "already", "also", "although", "always", "among", "an", "and", "another", "any", 
      "anybody", "anyone", "anything", "anywhere", "are", "area", "areas", "around", "as", "ask", 
      "asked", "asking", "asks", "at", "away", "back", "backed", "backing", "backs", "be", 
      "became", "because", "become", "becomes", "been", "before", "began", "behind", "being", 
      "beings", "best", "better", "between", "big", "both", "but", "by", "came", "can", 
      "cannot", "case", "cases", "certain", "certainly", "clear", "clearly", "come", "could", 
      "did", "differ", "different", "differently", "do", "does", "done", "down", "down", 
      "downed", "downing", "downs", "during", "each", "early", "either", "end", "ended", 
      "ending", "ends", "enough", "even", "evenly", "ever", "every", "everybody", "everyone", 
      "everything", "everywhere", "face", "faces", "fact", "facts", "far", "felt", "few", 
      "find", "finds", "first", "for", "four", "from", "full", "fully", "further", "furthered", 
      "furthering", "furthers", "gave", "general", "generally", "get", "gets", "give", 
      "given", "gives", "go", "going", "good", "goods", "got", "great", "greater", "greatest", 
      "group", "grouped", "grouping", "groups", "had", "has", "have", "having", "he", "her",
      "here", "herself", "high", "high", "high", "higher", "highest", "him", "himself", "his", 
      "how", "however", "if", "important", "in", "interest", "interested", "interesting", 
      "interests", "into", "i", "is", "it", "its", "itself", "just", "keep", "keeps", "kind", 
      "knew", "know", "known", "knows", "large", "largely", "last", "later", "latest", 
      "least", "less", "let", "lets", "like", "likely", "long", "longer", "longest", "made", 
      "make", "making", "man", "many", "may", "me", "member", "members", "m", "men", "might", "more", 
      "most", "mostly", "mr", "mrs", "much", "must", "my", "myself", "necessary", "need", 
      "needed", "needing", "needs", "never", "new", "newer", "newest", "next", "no", 
      "nobody", "non", "noone", "not", "nothing", "now", "nowhere", "number", "numbers", 
      "of", "off", "often", "old", "older", "oldest", "on", "once", "one", "only", "open", 
      "opened", "opening", "opens", "or", "order", "ordered", "ordering", "orders", "other", 
      "others", "our", "out", "over", "part", "parted", "parting", "parts", "per", "perhaps", 
      "place", "places", "point", "pointed", "pointing", "points", "possible", "present", 
      "presented", "presenting", "presents", "problem", "problems", "put", "puts", "quite", 
      "rather", "really", "right", "right", "room", "rooms", "said", "same", "saw", 
      "say", "says", "second", "seconds", "see", "seem", "seemed", "seeming", "seems", "sees", 
      "several", "shall", "she", "should", "show", "showed", "showing", "shows", "side", "sides", 
      "since", "small", "smaller", "smallest", "so", "some", "somebody", "someone", "something", 
      "somewhere", "state", "states", "still", "still", "such", "sure", "take", "taken", 
      "than", "that", "the", "their", "them", "then", "there", "therefore", "these", "they", 
      "thing", "things", "think", "thinks", "this", "those", "though", "thought", "thoughts", 
      "three", "through", "thus", "to", "today", "together", "too", "took", "toward", "turn", 
      "turned", "turning", "turns", "two", "under", "until", "up", "upon", "use", "us",
      "used", "uses", "very", "want", "wanted", "wanting", "wants", "was", "way", 
      "ways", "we", "well", "wells", "went", "were", "what", "when", "where", "whether", "which", 
      "while", "who", "whole", "whose", "why", "will", "with", "within", "without", "work", 
      "worked", "working", "works", "would", "year", "years", "yet", "you", "young",
      "younger", "youngest", "your", "yours"
   };

   public static HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
         set.add(s);
      }
      return set;
   }
}
