package netgen;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import netgen.Token.TokenType;

/**
 *
 * Pretty much just for Markov chains * //TODO: Rename "MarkovChain" and extract
 * superclass? for the moment. //TODO: Rename "MarkovChain" and extract
 * superclass?
 */
public class WeightedDirectedNetwork {

    //Attributes for the markov chain
    public HashMap<OrderedSemanticPair, Integer> edgeCount = new HashMap<>(); //Map : (Token a, Token b) --> Int number of instances of each edge
//    public HashMap<OrderedSemanticPair, Double> markovEdgeOut = new HashMap<>();
//    public HashMap<OrderedSemanticPair, Double> markovEdgeIn = new HashMap<>();

    //Intermediate values
    //TODO: Replace with coherent pipeline?
    private HashMap<Token, HashSet<Token>> outEdges;
    private HashMap<Token, HashSet<Token>> inEdges;
    private HashSet<Token> tokenSet;
    private HashMap<Token, Integer> tokenCount;
    private HashMap<Token, HashSet<Token>> upstreamNeighborhoods;
    private HashMap<Token, HashSet<Token>> downstreamNeighborhoods;
    private HashMap<Token, Integer> outDegree; //Sum of out edges
    private HashMap<Token, Integer> inDegree; //Sum of in edges
    
    //Creates a markov chain of all semantic tokens in a series of semantic tokens
    public static HashMap<OrderedSemanticPair, Integer> generateEdgeCount(ArrayList<Token> input) {

        HashMap<OrderedSemanticPair, Integer> edges = new HashMap<>();

        for (int i = 1; i < input.size(); i++) {

            //Ignore annotation tokens since currently these are only sentence breaks
            if (input.get(i).type == TokenType.Semantic
                    && input.get(i - 1).type == TokenType.Semantic) {

                OrderedSemanticPair edge = new OrderedSemanticPair(input.get(i - 1).signature, input.get(i).signature);

                if (edges.containsKey(edge)) {
                    edges.put(edge, edges.get(edge) + 1);
                } else {
                    edges.put(edge, 1);
                }

            }
        }
        return edges;

    }

    //Call on instantiation. Populates all the private derivative variables once for convenient use during pipeline
    public void populateAdditionalAttributes(int minimumTokenCount, int streamDistance) {

        ArrayList<OrderedSemanticPair> edgeList = new ArrayList<>();
        edgeList.addAll(this.edgeCount.keySet());

        //Add in/out edges sets
        //For each edge
        for (OrderedSemanticPair edge : edgeList) {

            //Add outEdge
            if (outEdges.containsKey(edge.getA())) {
                outEdges.get(edge.getA()).add(new SemanticToken(edge.getB().signature));
            } else {
                HashSet<Token> tokenSet = new HashSet<>();
                tokenSet.add(new SemanticToken(edge.getB().signature));
                outEdges.put(edge.getA(), tokenSet);
            }

            //Add inEdge
            if (inEdges.containsKey(edge.getB())) {
                inEdges.get(edge.getB()).add(new SemanticToken(edge.getA().signature));
            } else {
                HashSet<Token> tokenSet = new HashSet<>();
                tokenSet.add(new SemanticToken(edge.getA().signature));
                inEdges.put(edge.getB(), tokenSet);
            }
        }

        //Populate tokenSet
        tokenSet = new HashSet<>();
        tokenSet.addAll(inEdges.keySet());
        tokenSet.addAll(outEdges.keySet());

        //Populate tokenCount
        tokenCount = new HashMap<>();
        for (OrderedSemanticPair pair : edgeCount.keySet()) {
            if (tokenCount.containsKey(pair.getA())) {
                tokenCount.put(pair.getA(), tokenCount.get(pair.getA()) + 1);
            } else {
                tokenCount.put(pair.getA(), 1);
            }

            if (tokenCount.containsKey(pair.getB())) {
                tokenCount.put(pair.getB(), tokenCount.get(pair.getB()) + 1);
            } else {
                tokenCount.put(pair.getB(), 1);
            }
        }

        //Get all relevant neighborhoods
        downstreamNeighborhoods = new HashMap<>();
        upstreamNeighborhoods = new HashMap<>();

        for (Token token : tokenSet) {
            if (tokenCount.get(token) > minimumTokenCount) {
                downstreamNeighborhoods.put(token, this.getDownstreamNeighborhood(token, streamDistance));
                upstreamNeighborhoods.put(token, this.getUpstreamNeighborhood(token, streamDistance));
            }
        }

        //Populate in/out degree
        for (Token base : tokenSet) {

            int out = 0;
            int in = 0;
            //count out degree
            for (Token down : outEdges.get(base)) {
                out += edgeCount.get(new OrderedSemanticPair(base.getSignature(), down.getSignature()));
            }
            //count in degree
            for (Token up : inEdges.get(base)) {
                in += edgeCount.get(new OrderedSemanticPair(up.getSignature(), base.getSignature()));
            }
            outDegree.put(base, out);
            inDegree.put(base, in);
        }

    }

    //Returns a set of tokens corresponding to the upstream tokens from the target
    public HashSet<Token> getUpstreamNeighborhood(Token target, int distance) {
        HashSet<Token> upstream = new HashSet<>();
        upstream.add(target);
        //Adds another layer upstream at each iteration. Not super efficient but doesn't matter really
        for (int i = 0; i < distance; i++) {
            HashSet<Token> additionalUp = new HashSet<>();
            for (Token token : upstream) {
                additionalUp.addAll(inEdges.get(token));
            }
            upstream.addAll(additionalUp);
        }
        return upstream;
    }

    //Returns a set of tokens corresponding to the downstream tokens from the target
    public HashSet<Token> getDownstreamNeighborhood(Token target, int distance) {

        HashSet<Token> downstream = new HashSet<>();
        downstream.add(target);
        //Adds another layer downstream at each iteration. Not super efficient but doesn't matter really
        for (int i = 0; i < distance; i++) {
            HashSet<Token> additionalDown = new HashSet<>();

            for (Token token : downstream) {
                additionalDown.addAll(outEdges.get(token));
            }
            downstream.addAll(additionalDown);
        }

        return downstream;
    }

    //Checks immediate upstream and immedate downstream, 
    //returns the product of (intersectionDegree/unionDegree) 
    //calculated for upstream and downstream
    public double simpleCompareNeighborhoods(Token a, Token b) {

        //get the union and intersection of the upstream neighborhoods of a and b
        HashSet<Token> upstreamIntersection = new HashSet<>();
        upstreamIntersection.addAll(upstreamNeighborhoods.get(a));
        upstreamIntersection.retainAll(upstreamNeighborhoods.get(b));

        HashSet<Token> upstreamUnion = new HashSet<>();
        upstreamUnion.addAll(upstreamNeighborhoods.get(a));
        upstreamUnion.addAll(upstreamNeighborhoods.get(b));

        //get the union and intersection of the downstream neighborhoods of a and b
        HashSet<Token> downstreamIntersection = new HashSet<>();
        downstreamIntersection.addAll(downstreamNeighborhoods.get(a));
        downstreamIntersection.retainAll(downstreamNeighborhoods.get(b));

        HashSet<Token> downstreamUnion = new HashSet<>();
        upstreamUnion.addAll(downstreamNeighborhoods.get(a));
        upstreamUnion.addAll(downstreamNeighborhoods.get(b));

        
        //get the sum of degrees of downstream and upstream intersections and unions
        double upstreamUnionDegree = inDegree.get(a) + inDegree.get(b);
        double downstreamUnionDegree = outDegree.get(a) + outDegree.get(b);
        double upstreamIntersectionDegree = 0;
        double downstreamIntersectionDegree = 0;
        
        for(Token token : upstreamIntersection) {
            upstreamIntersectionDegree += edgeCount.get(new OrderedSemanticPair(token, a));
            upstreamIntersectionDegree += edgeCount.get(new OrderedSemanticPair(token, b));
        }
        
        for(Token token : downstreamIntersection) {
            downstreamIntersectionDegree += edgeCount.get(new OrderedSemanticPair(a, token));
            downstreamIntersectionDegree += edgeCount.get(new OrderedSemanticPair(b, token));
        }
        
        double upstreamMatchRate = upstreamIntersectionDegree / upstreamUnionDegree;
        double downstreamMatchRate = downstreamIntersectionDegree / downstreamUnionDegree;
        
        
        return (upstreamMatchRate * downstreamMatchRate);

    }

    //Uses simpleCompareNeighborhoods to build a network indicating level of synonimity between tokens present in the corpus
    //TODO: Probably set a minimum number of tokens 
    public Network extractSynonimityNetwork() {
        //Settings

        Network network = new Network();

        
        ArrayList<Token> limitedKeyList = new ArrayList<>();
        limitedKeyList.addAll(tokenSet);
        
        //Compare neighborhoods of every pair of tokens
        //in the filtered keyset and add results to synonimity network
        for (int i = 0; i < limitedKeyList.size() - 1; i++) {
            for (int j = i + 1; j < limitedKeyList.size(); j++) {
                network.edgeSet.put(new SemanticPair(limitedKeyList.get(i).signature, limitedKeyList.get(j).signature),
                        simpleCompareNeighborhoods(limitedKeyList.get(i), limitedKeyList.get(j)));
            }
        }

        return network;
    }

    //OUTPUT METHODS
    //Writes the graph to an .dl file, weighted directed edge list format
    public void writeEdgelist(String fileName) {

        try {
            File file = new File(fileName + ".dl");
            FileWriter writer = null;
            writer = new FileWriter(file);
            writer.write("dl\nformat = edgelist1\t\nn=" + edgeCount.size() + "\t\ndata:");

            for (OrderedSemanticPair edge : edgeCount.keySet()) {
                writer.write("\n" + edge.getA() + " " + edge.getB() + " " + edgeCount.get(edge) + "\t");
            }

            writer.close();

        } catch (Exception e) {
            System.out.println("Failed to complete output file. Exiting.");
            System.exit(-1);
        }

    }

}
