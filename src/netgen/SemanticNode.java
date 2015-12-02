package netgen;

import java.util.HashMap;
import java.util.HashSet;

public class SemanticNode extends SemanticToken {

    public int outDegree = 0;
    public int inDegree = 0;
    public HashMap<Token, Double> outProbability = new HashMap<>();
    public HashMap<Token, Double> inProbability = new HashMap<>();

    public SemanticNode(String string, HashMap<OrderedSemanticPair, Double> edges) {
        super(string);

        //Calculate inDegree and outDegree from a set of weighted directed edges
        for (OrderedSemanticPair edge : edges.keySet()) {
            if (edge.getA().signature.equals(this.signature)) {
                outDegree += edges.get(edge);
            }
            if (edge.getB().signature.equals(this.signature)) {
                inDegree += edges.get(edge);
            }
        }

        //Calculate probabilities
        for (OrderedSemanticPair edge : edges.keySet()) {
            if (edge.getA().signature.equals(this.signature)) {
                outProbability.put(new SemanticToken(edge.getB().signature), edges.get(edge) / outDegree);
            }

            if (edge.getB().signature.equals(this.signature)) {
                inProbability.put(new SemanticToken(edge.getA().signature), edges.get(edge) / inDegree);
            }

        }

    }

    public double getTotalInProbability() {
        double n = 0;
        for (Token token : inProbability.keySet()) {
            n += inProbability.get(token);
        }
        return n;
    }

    public double getTotalOutProbability() {
        double n = 0;
        for (Token token : outProbability.keySet()) {
            n += outProbability.get(token);
        }
        return n;
    }

    public static double compareNodes(SemanticNode x, SemanticNode y) {

        //Get the intersection of the upstream and downstream neighbors
        HashSet<Token> outTokenSet = new HashSet<>();
        HashSet<Token> inTokenSet = new HashSet<>();
        outTokenSet.addAll(x.outProbability.keySet());
        outTokenSet.retainAll(y.outProbability.keySet());
        inTokenSet.addAll(x.inProbability.keySet());
        inTokenSet.retainAll(y.inProbability.keySet());

        //Get the match rates
        double upstreamMatchRate = 0;
        double downstreamMatchRate = 0;

        for (Token token : inTokenSet) {
            upstreamMatchRate += Math.min(x.inProbability.get(token), y.inProbability.get(token));
        }

        for (Token token : outTokenSet) {
            downstreamMatchRate += Math.min(x.outProbability.get(token), y.outProbability.get(token));
        }
        
        return ((upstreamMatchRate) + (downstreamMatchRate))/2.0;
    }

    @Override
    public String toString() {
        return "Out Degree: " + outDegree + "\nIn Degree: " + inDegree
                + "\nOut Edges: " + outProbability.keySet().size() + "\nIn Edges: " + inProbability.keySet().size()
                + "\nTotal out probability: " + getTotalOutProbability() + "\nTotal in probability: " + getTotalInProbability();
    }

}
