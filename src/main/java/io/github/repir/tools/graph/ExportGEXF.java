package io.github.repir.tools.graph;

import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.StrTools;
import java.util.HashMap;
/**
 *
 * @author jeroen
 */
public class ExportGEXF<N, A> {
   public static final Log log = new Log( ExportGEXF.class );
   Graph<N, A> graph;
   HashMap<N, Integer> nodeids = new HashMap();
   
   public ExportGEXF(Graph<N, A> graph) {
       this.graph = graph;
       mapNodes();
   }
   
   public void write(Datafile df) {
       df.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
       df.print("<gexf xmlns=\"http://www.gexf.net/1.1draft\" version=\"1.1\">\n");
       df.print("<graph mode=\"static\" defaultedgetype=\"undirected\">\n");
       df.print("<nodes>\n");
       for (N node : graph.nodes.keySet()) {
           df.printf("<node id=\"%s\" label=\"%s\"/>\n", nodeids.get(node), node.toString().replace("\"", ""));
       }
       df.print("</nodes>\n");
       df.print("<edges>\n");
       for (Edge<N, A> edge : graph.edges.keySet()) {
           df.printf("<edge source=\"%d\" target=\"%d\" weight=\"%s\" label=\"%s\"/>\n", 
                   nodeids.get(edge.source), nodeids.get(edge.dest), edge.attr.toString(), edge.attr.toString());
       }
       df.print("</edges>\n");
       df.print("</graph>\n");
       df.print("</gexf>\n");
       df.closeWrite();
   }
   
   public void mapNodes() {
       for (N node : graph.nodes.keySet())
           nodeids.put(node, nodeids.size());
   }
   
   
   
   
   
}
