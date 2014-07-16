package io.github.repir.tools.ByteSearch;
import io.github.repir.tools.Buffer.BufferReaderWriter;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class W09 extends StructuredTextCSV {
  public static Log log = new Log( W09.class ); 
   public IntField number = this.addInt("number", "wt09-", ":", "", "");
   public StringField title = this.addString("title", "", "($|\n)", "", "");
  
  public W09(BufferReaderWriter r) {
     super(r);
  }
  
  public W09(Datafile r) {
     super(r);
  }
  
  public static W09 setup() {
     return new W09(new Datafile("/home/jer/Desktop/w09.1-50.topics.txt"));
  }
  
  public static W09 setup1() {
     StringBuilder sb = new StringBuilder();
     sb.append("wt09-1:obama family tree\n" +
"wt09-2:french lick resort and casino\n" +
"wt09-3:getting organized\n" +
"wt09-4:toilet\n" +
"wt09-5:mitchell college\n" +
"wt09-6:kcs\n" +
"wt09-7:air travel information\n" +
"wt09-8:appraisals\n" +
"wt09-9:used car parts\n" +
"wt09-10:cheap internet\n" +
"wt09-11:gmat prep classes\n" +
"wt09-12:djs\n" +
"wt09-13:map\n" +
"wt09-14:dinosaurs\n" +
"wt09-15:espn sports\n" +
"wt09-16:arizona game and fish\n" +
"wt09-17:poker tournaments\n" +
"wt09-18:wedding budget calculator\n" +
"wt09-19:the current\n" +
"wt09-20:defender\n" +
"wt09-21:volvo\n" +
"wt09-22:rick warren\n" +
"wt09-23:yahoo\n" +
"wt09-24:diversity\n" +
"wt09-25:euclid\n" +
"wt09-26:lower heart rate\n" +
"wt09-27:starbucks\n" +
"wt09-28:inuyasha\n" +
"wt09-29:ps 2 games\n" +
"wt09-30:diabetes education\n" +
"wt09-31:atari\n" +
"wt09-32:website design hosting\n" +
"wt09-33:elliptical trainer\n" +
"wt09-34:cell phones\n" +
"wt09-35:hoboken\n" +
"wt09-36:gps\n" +
"wt09-37:pampered chef\n" +
"wt09-38:dogs for adoption\n" +
"wt09-39:disneyland hotel\n" +
"wt09-40:michworks\n" +
"wt09-41:orange county convention center\n" +
"wt09-42:the music man\n" +
"wt09-43:the secret garden\n" +
"wt09-44:map of the united states\n" +
"wt09-45:solar panels\n" +
"wt09-46:alexian brothers hospital\n" +
"wt09-47:indexed annuity\n" +
"wt09-48:wilson antenna\n" +
"wt09-49:flame designs\n" +
"wt09-50:dog heat");
     return new W09(new BufferReaderWriter(sb.toString().getBytes()));
  }
  
   public static void main(String[] args) {
      W09 f = setup();
      //log.info("%b %d", f.datafile.exists(), f.datafile.getLength());
      f.openRead();
     while (f.next()) {
        log.info("%d, %s", f.number.get(), f.title.get());
     }
  }
}
