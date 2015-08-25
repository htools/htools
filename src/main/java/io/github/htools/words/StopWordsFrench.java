package io.github.htools.words;

import io.github.htools.lib.Log;
import java.util.HashSet;

/**
 * Stop word list of 429 terms from
 * https://code.google.com/p/stop-words/ .
 */
public class StopWordsFrench {

    public static Log log = new Log(StopWordsFrench.class);

    public static String filterarray[] = {
        "a", "à", "â", "abord", "afin", "ah", "ai", "aie", "ainsi", "allaient", 
        "allo", "allô", "allons", "après", "assez", "attendu", "au", "aucun", 
        "aucune", "aujourd", "auquel", "aura", "auront", "aussi", "autre", 
        "autres", "aux", "auxquelles", "auxquels", "avaient", "avais", "avait", 
        "avant", "avec", "avoir", "ayant", "b", "bah", "beaucoup", "bien", "bigre", 
        "boum", "bravo", "brrr", "c", "ça", "car", "ce", "ceci", "cela", "celle", 
        "celles", "celui", "cent", "cependant", "certain", "certaine", "certaines", 
        "certains", "certes", "ces", "cet", "cette", "ceux", "chacun", "chaque", 
        "cher", "chère", "chères", "chers", "chez", "chiche", "chut", "ci", "cinq", 
        "cinquantaine", "cinquante", "cinquantième", "cinquième", "clac", "clic", 
        "combien", "comme", "comment", "compris", "concernant", "contre", "couic", 
        "crac", "d", "da", "dans", "de", "debout", "dedans", "dehors", "delà", 
        "depuis", "derrière", "des", "dès", "désormais", "desquelles", "desquels", 
        "dessous", "dessus", "deux", "deuxième", "deuxièmement", "devant", "devers", 
        "devra", "différent", "différente", "différentes", "différents", "dire", 
        "divers", "diverse", "diverses", "dix", "dixième", "doit", "doivent", "donc", 
        "dont", "douze", "douzième", "dring", "du", "duquel", "durant", "e", "effet", 
        "eh", "elle", "elles", "en", "encore", "entre", "envers", "environ", "es", 
        "ès", "est", "et", "etant", "étaient", "étais", "était", "étant", "etc", "été", 
        "etre", "être", "eu", "euh", "eux", "excepté", "f", "façon", "fais", "faisaient", 
        "faisant", "fait", "feront", "fi", "flac", "floc", "font", "g", "gens", "h", 
        "ha", "hé", "hein", "hélas", "hem", "hep", "hi", "ho", "holà", "hop", "hormis", 
        "hors", "hou", "houp", "hue", "hui", "huit", "huitième", "hum", "hurrah", "i", 
        "il", "ils", "importe", "j", "je", "jusqu", "jusque", "k", "l", "la", "là", 
        "laquelle", "las", "le", "lequel", "les", "lès", "lesquelles", "lesquels", 
        "leur", "leurs", "longtemps", "lorsque", "lui", "m", "ma", "maint", "mais", 
        "malgré", "me", "même", "mêmes", "merci", "mes", "mien", "mienne", "miennes", 
        "miens", "mille", "mince", "moi", "moins", "mon", "moyennant", "n", "na", "ne", 
        "néanmoins", "neuf", "neuvième", "ni", "nombreuses", "nombreux", "non", "nos", 
        "notre", "nôtre", "nôtres", "nous", "nul", "o", "ô", "oh", "ohé", "olé", "ollé", 
        "on", "ont", "onze", "onzième", "ore", "ou", "où", "ouf", "ouias", "oust", 
        "ouste", "outre", "p", "paf", "pan", "par", "parmi", "partant", "particulier", 
        "particulière", "particulièrement", "pas", "passé", "pendant", "personne", "peu",
        "peut", "peuvent", "peux", "pff", "pfft", "pfut", "pif", "plein", "plouf", "plus", 
        "plusieurs", "plutôt", "pouah", "pour", "pourquoi", "premier", "première", 
        "premièrement", "près", "proche", "psitt", "puisque", "q", "qu", "quand", "quant", 
        "quanta", "quarante", "quatorze", "quatre", "quatrième", "quatrièmement", 
        "que", "quel", "quelconque", "quelle", "quelles", "quelque", "quelques", "quelqu", 
        "quels", "qui", "quiconque", "quinze", "quoi", "quoique", "r", "revoici", 
        "revoilà", "rien", "s", "sa", "sacrebleu", "sans", "sapristi", "sauf", "se", 
        "seize", "selon", "sept", "septième", "sera", "seront", "ses", "si", "sien", 
        "sienne", "siennes", "siens", "sinon", "six", "sixième", "soi", "soit", "soixante", 
        "son", "sont", "sous", "stop", "suis", "suivant", "sur", "surtout", "t", "ta", 
        "tac", "tant", "te", "té", "tel", "telle", "tellement", "telles", "tels", "tenant", 
        "tes", "tic", "tien", "tienne", "tiennes", "tiens", "toc", "toi", "ton", 
        "touchant", "toujours", "tous", "tout", "toute", "toutes", "treize", "trente", 
        "très", "trois", "troisième", "troisièmement", "trop", "tsoin", "tsouin", "tu", 
        "u", "un", "une", "unes", "uns", "v", "va", "vais", "vas", "vé", "vers", "via", 
        "vif", "vifs", "vingt", "vivat", "vive", "vives", "vlan", "voici", "voilà", 
        "vont", "vos", "votre", "vôtre", "vôtres", "vous", "vu", "w", "x", "y", "z", "zut"
    };

    public static HashSet<String> getUnstemmedFilterSet() {
        HashSet<String> set = new HashSet<String>();
        for (String s : filterarray) {
            set.add(s);
        }
        return set;
    }
}
