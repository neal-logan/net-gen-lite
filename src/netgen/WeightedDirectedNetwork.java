package netgen;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import netgen.Token.TokenType;

/**
 *
 * Pretty much just for Markov chains TODO: Rename "MarkovChain" and extract
 * superclass?
 *
 */
public class WeightedDirectedNetwork {

    //VARIABLES
    //Map : (Token a, Token b) --> Int number of instances of each edge
    public HashMap<OrderedSemanticPair, Double> edges = new HashMap<>();

    //CONSTRUCTORS
    //Takes an edgeset representing a markov chain as argument, stores and processes it
    public WeightedDirectedNetwork(HashMap<OrderedSemanticPair, Double> input) {
        for (OrderedSemanticPair pair : input.keySet()) {
            edges.put(
                    new OrderedSemanticPair(pair.getA().signature, pair.getB().signature),
                    input.get(pair));
        }
    }

    //ACCESSORS/MUTATORS
    public HashMap<Token, HashSet<Token>> getAllOutEdges() {
        HashSet<Token> tokenSet = this.getTokenSet();

        HashMap<Token, HashSet<Token>> outEdges = new HashMap<>();

        //Initialize in/out edge sets
        for (Token token : tokenSet) {
            outEdges.put(token, new HashSet<>());
        }

        //For each edge, add out edge
        for (OrderedSemanticPair edge : edges.keySet()) {
            outEdges.get(edge.getA()).add(new SemanticToken(edge.getB().signature));
        }
        return outEdges;
    }

    public HashMap<Token, HashSet<Token>> getAllInEdges() {
        HashSet<Token> tokenSet = this.getTokenSet();

        HashMap<Token, HashSet<Token>> inEdges = new HashMap<>();

        //Initialize in/out edge sets
        for (Token token : tokenSet) {
            inEdges.put(token, new HashSet<>());
        }
        
        //For each edge
        for (OrderedSemanticPair edge : edges.keySet()) {
            //Add inEdge
            inEdges.get(edge.getB()).add(new SemanticToken(edge.getA().signature));
        }
        return inEdges;
    }

    //Returns a set containing all tokens in the edgelist
    public HashSet<Token> getTokenSet() {
        //Populate tokenSet
        HashSet<Token> tokenSet = new HashSet<>();
        for (OrderedSemanticPair edge : edges.keySet()) {
            tokenSet.add(edge.getA());
            tokenSet.add(edge.getB());
        }
        return tokenSet;
    }

    //Returns a map containing the degree each token
    public HashMap<Token, Double> getTokenCountMap() {
        //Populate tokenCount
        HashMap<Token, Double> tokenCount = new HashMap<>();

        for (OrderedSemanticPair pair : edges.keySet()) {
            if (tokenCount.containsKey(pair.getA())) {
                tokenCount.put(pair.getA(), tokenCount.get(pair.getA()) + 1);
            } else {
                tokenCount.put(pair.getA(), 1.0);
            }

            if (tokenCount.containsKey(pair.getB())) {
                tokenCount.put(pair.getB(), tokenCount.get(pair.getB()) + 1);
            } else {
                tokenCount.put(pair.getB(), 1.0);
            }
        }

        return tokenCount;
    }

    //Takes a text as argument and produces an edge count
    //as a markov chain precursor from its tokens
    public WeightedDirectedNetwork(ArrayList<Token> input) {
        edges = generateEdgeWeight(input);
    }

    //Creates a ordered adjacency of all semantic tokens in a series of semantic tokens
    private static HashMap<OrderedSemanticPair, Double> generateEdgeWeight(ArrayList<Token> input) {

        HashMap<OrderedSemanticPair, Double> edges = new HashMap<>();

        for (int i = 1; i < input.size(); i++) {

            //Ignore annotation tokens since currently these are only sentence breaks
            if (input.get(i).type == TokenType.Semantic
                    && input.get(i - 1).type == TokenType.Semantic) {

                OrderedSemanticPair edge = new OrderedSemanticPair(input.get(i - 1).signature, input.get(i).signature);

                if (edges.containsKey(edge)) {
                    edges.put(edge, edges.get(edge) + 1);
                } else {
                    edges.put(edge, 1.0);
                }

            }
        }
        System.out.println("Edge count generated");
        return edges;

    }

    public void removeEdgesBelow(int threshold) {
        int startingEdges = edges.size();
        HashMap<OrderedSemanticPair, Double> tempEdges = new HashMap<>();
        for (OrderedSemanticPair pair : edges.keySet()) {
            if (edges.get(pair) >= threshold) {
                tempEdges.put(pair, edges.get(pair));
            }
        }
        edges = tempEdges;
        System.out.println("Removed " + (startingEdges - edges.size()) + " edges below edge weight threshold");
    }

    public void removeTokensBelow(int threshold) {
        HashMap<Token, Double> tokenCount = getTokenCountMap();

        HashSet<Token> tokensToRemove = new HashSet<>();

        for (Token token : tokenCount.keySet()) {
            if (tokenCount.get(token) < threshold) {
                tokensToRemove.add(token);
            }
        }

        HashMap<OrderedSemanticPair, Double> filteredEdges = new HashMap<>();

        int startingEdges = edges.size();

        for (OrderedSemanticPair edge : edges.keySet()) {
            if (!tokensToRemove.contains(edge.getA())
                    && !tokensToRemove.contains(edge.getB())) {
                filteredEdges.put(edge, edges.get(edge));
            }
        }

        edges = filteredEdges;
        System.out.println("Removed " + (startingEdges - edges.size()) + " edges below token count threshold");
    }

    public MarkovChain toMarkovChain() {

        HashSet<Token> tokenSet = this.getTokenSet();

        MarkovChain markovChain = new MarkovChain();

        int i = 0;
        for (Token token : tokenSet) {
            SemanticNode node = new SemanticNode(token.signature, edges);

            markovChain.nodes.put(token.signature, node);

            //DEBUG
            if (node.getTotalInProbability() > 1.01 || node.getTotalOutProbability() > 1.01) {
                System.out.println("Warning: total probability high");
                System.out.println(node.toString());
            }

            if (i % 100 == 0) {
                System.out.println("Created " + i + " of " + tokenSet.size() + " nodes");
//                System.out.println(node.toString());
            }
            i++;

        }

        System.out.println("Markov Chain Instantiated with " + markovChain.nodes.size() + " nodes.");

        return markovChain;
    }

    //OUTPUT METHODS
    //Writes the graph to an .dl file, weighted directed edge list format
    public void writeEdgelist(String fileName) {

        try {
            File file = new File(fileName + ".dl");
            FileWriter writer = null;
            writer = new FileWriter(file);
            writer.write("dl\nformat = edgelist1\t\nn=" + edges.size() + "\t\ndata:");
            int edgesWritten = 0;
            for (OrderedSemanticPair edge : edges.keySet()) {

                edgesWritten++;
                writer.write("\n" + edge.getA().getSignature() + " " + edge.getB().getSignature() + " " + edges.get(edge) + "\t");
//                } 
            }

            System.out.println("Unique edges written: " + edgesWritten);
            writer.close();

        } catch (Exception e) {
            System.out.println("Failed to complete output file. Exiting.");
            System.exit(-1);
        }

    }

}
