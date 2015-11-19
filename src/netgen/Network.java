package netgen;

import netgen.WeightedEdge;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.Random;

public class Network {

    public HashMap<SemanticPair, Double> edgeSet;
    public Calendar calendar;

    //CONSTRUCTORS
    public Network() {
        edgeSet = new HashMap<SemanticPair, Double>();
        calendar = new GregorianCalendar();
    }

    public Network(HashMap<SemanticPair, Double> edgeSet, Calendar date) {
        this.edgeSet = edgeSet;
        this.calendar = date;
    }

    //BASIC GRAPH METHODS
    //Returns a new graph which is sum of the two argument graphs
    //Public because it's okay to sum graphs produced with different methods, 
    //for example to produce a graph which is the mean of two graph generation methods
    public static HashMap<SemanticPair, Double> sum(HashMap<SemanticPair, Double> a, HashMap<SemanticPair, Double> b) {
        HashMap<SemanticPair, Double> sum = new HashMap<>();

        sum.putAll(a);

        for (Entry<SemanticPair, Double> entry : b.entrySet()) {
            if (!sum.containsKey((SemanticPair) entry.getKey())) {
                sum.put((SemanticPair) entry.getKey(), (double) entry.getValue());
            } else {
                sum.put((SemanticPair) entry.getKey(), (double) entry.getValue() + sum.get((SemanticPair) entry.getKey()));
            }
        }

        return sum;
    }

    //Scales the graph argument by the specified scalar multiple
    public void scale(HashMap<SemanticPair, Double> graph, double scalar) {
        for (Entry<SemanticPair, Double> entry : graph.entrySet()) {
            entry.setValue((double) entry.getValue() * scalar);
        }
    }

    //OUTPUT METHODS
    //Writes the graph to an .dl file, weighted edge list format
    public void writeEdgelist(String fileName) {

//        System.out.println("Writing edgelist...");
        try {
            File file = new File(fileName + ".dl");
            FileWriter writer = null;
            writer = new FileWriter(file);
            writer.write("dl\nformat = edgelist1\t\nn=" + getEdgeset().size() + "\t\ndata:");

            for (Entry<SemanticPair, Double> edge : getEdgeset().entrySet()) {
                writer.write("\n" + edge.getKey().getA() + " " + edge.getKey().getB() + " " + getEdgeset().get(edge.getKey()) + "\t");
                //Debug
//                System.out.println(pair.getA().getSignature() + " " + pair.getB().getSignature() + " " + edges.get(pair) + "\t");
            }

            writer.close();

        } catch (Exception e) {
            System.out.println("Failed to complete output file. Exiting.");
            System.exit(-1);
        }

    }

    //Writes the graph to an .dl file, weighted edge list format
    public void writeEdgelistWithAdditionalMetadata(String fileName) {

//        System.out.println("Writing edgelist...");
        try {
            File file = new File(fileName + ".dl");
            FileWriter writer = null;
            writer = new FileWriter(file);
            writer.write("dl\nYearMonthDate " + calendar.get(Calendar.YEAR) + " "
                    + calendar.get(Calendar.MONTH) + " " + calendar.get(Calendar.DATE) + "\nformat = edgelist1\t\nn=" + getEdgeset().size() + "\t\ndata:");

            for (Entry<SemanticPair, Double> edge : getEdgeset().entrySet()) {
                writer.write("\n" + edge.getKey().getA() + " " + edge.getKey().getB() + " " + getEdgeset().get(edge.getKey()) + "\t");
                //Debug
//                System.out.println(pair.getA().getSignature() + " " + pair.getB().getSignature() + " " + edges.get(pair) + "\t");
            }

            writer.close();

        } catch (Exception e) {
            System.out.println("Failed to complete output file. Exiting.");
            System.exit(-1);
        }

    }

    /**
     * @return the edgeSet
     */
    public HashMap<SemanticPair, Double> getEdgeset() {
        return edgeSet;
    }

    public double meanEdge() {

        double mean = 0;

        for (Entry<SemanticPair, Double> entry : this.edgeSet.entrySet()) {
            mean += (double) entry.getValue();
        }
        mean /= this.edgeSet.size();
        return mean;
    }

    public double heaviestEdge() {
        double highest = 0;
        for (Entry<SemanticPair, Double> entry : this.edgeSet.entrySet()) {
            if ((double) entry.getValue() > highest) {
                highest = (double) entry.getValue();
            }
        }
        return highest;
    }

    public void addNoise(double intensity) {
        Random random = new Random();

        for (Entry<SemanticPair, Double> entry : this.edgeSet.entrySet()) {
            entry.setValue((double) entry.getValue() + random.nextDouble() * intensity);
        }
    }

    //Normalizes all edge weights on a (0,1.0] scale, with 1.0 being reserved for the heaviest edge
    //TODO: Check
    public void normalizeToHighestEdge() {

        double scale = 1.0 / this.heaviestEdge();
        scale(this.edgeSet, scale);

    }

//    public void normalizeToMeanEdge() {
//
//        scale(this.edgeSet, 1.0 / this.meanEdge());
//    }

    public void filterEdgesBelow(double minWeight) {
        ArrayList<WeightedEdge> edgeList = this.getEdgesAsList();

        edgeList.sort(null);

        HashMap<SemanticPair, Double> filteredEdges = new HashMap<>();

        for (WeightedEdge edge : edgeList) {
            if (edge.getWeight() >= minWeight) {
                filteredEdges.put(edge.getIncidentTokens(), edge.getWeight());
            }
        }

        this.edgeSet = filteredEdges;
    }

    public void limitEdges(int maxEdges) {

        ArrayList<WeightedEdge> edgeList = this.getEdgesAsList();

        edgeList.sort(null);

        HashMap<SemanticPair, Double> filteredEdges = new HashMap<>();

        for (int i = 0; i < maxEdges && i < edgeList.size(); i++) {
            WeightedEdge edge = edgeList.get(i);
            filteredEdges.put(edge.getIncidentTokens(), edge.getWeight());
        }

        this.edgeSet = filteredEdges;
    }

    /*
     //ACCESSORS AND MUTATORS
 
     /*
    
     * @return a list of Edge objects consisting of all edges in this network
     */
    public ArrayList<WeightedEdge> getEdgesAsList() {
        ArrayList<WeightedEdge> edgelist = new ArrayList<>();
        for (Entry entry : this.edgeSet.entrySet()) {
            edgelist.add(new WeightedEdge((SemanticPair) entry.getKey(), (double) entry.getValue()));
        }
        return edgelist;
    }

    //Multi-sentence-complete sliding window
    public static HashMap<SemanticPair, Double> generateAssociativeSemanticNetworkByMultiSentenceSlidingWindow(ArrayList<Token> input,
            int maxWindowSentences, int maxWindowTokens) {

        //ADAPTER SECTION
        //Horrible hack of October 18th
        //TODO: Fix/replace this whole method later
        ArrayList<ArrayList<Token>> lines = new ArrayList<>();
        ArrayList<Token> currentLine = new ArrayList<>();
        for (Token token : input) {
            if (token.type == Token.TokenType.Semantic) {

                currentLine.add(token);

            } else if (token.type == Token.TokenType.Annotation
                    && token.signature.equalsIgnoreCase("<SentenceSplit>")) {

                lines.add(currentLine);
                currentLine = new ArrayList<>();

            } else {
                //TODO: Probably something
            }
        }

        //OLD METHOD
        HashMap<SemanticPair, Double> edgeSet = new HashMap<>();
        int minWindowTokens = 1 + maxWindowTokens / 5;

        //For each window...
        //(includes smaller-size windows towards the beginning and end of the lines)
        for (int i = 1 - maxWindowSentences; i < lines.size(); i++) {
            ArrayList<Token> windowTokens = new ArrayList<>();
            //Add sentences to the current window until the sentence or token maximum is met
            for (int j = i; j < i + maxWindowSentences; j++) {
                if (j >= 0 && j < lines.size() //Don't add out-of-bounds sentences
                        && (windowTokens.size() + lines.get(j).size() < maxWindowTokens //Don't exceed max tokens
                        || windowTokens.size() < minWindowTokens)) {    //Unless necessary to reach min tokens
                    windowTokens.addAll(lines.get(j));
                } else {
                    break;
                }
            }

            //Generate and attenuate the window-level network based on window size
            HashMap<SemanticPair, Double> windowNetwork = generateAssociativeNetworkOfSingleSentence(windowTokens);
            for (Entry<SemanticPair, Double> edge : windowNetwork.entrySet()) {
                edge.setValue((double) edge.getValue() / (1 + windowNetwork.size()));
            }

            //Add the window-level network to the main network
            edgeSet = Network.sum(edgeSet, windowNetwork);

        }
        return edgeSet;
    }

    /*
     Forms a complete graph of a window which slides through each line. Returns the sum of all of these graphs. 
     Tokens will never be linked to themselves (so multiple instances of a token in a sentence will not result in reflexive edges).
     Tokens occurring more than once in a window will be weighted proportionally to the number of times they appear
     */
    //TODO: Fix
//    private static void generateByTokenwiseSlidingWindow(ArrayList<ArrayList<Token>> lines, int windowSize) {
//        System.out.println("The corpii size is " + lines.size() + " articles long");
//        for (ArrayList<Token> line : lines) {
//            for (int i = 0; i < line.size() - windowSize; i++) { //For Token in Sentences(Line)
//                for (int j = i + 1; j < i + windowSize; j++) {	// Look forward comparing current token to all within the windowsize
//                    if (!line.get(i).equals(line.get(j))) {		// Only record a Pair if they are different words. Does equals() actually compare string text here?
//
//                        SemanticTokenPair pair = new SemanticTokenPair(line.get(i).getSignature(), line.get(j).getSignature());
//
//                        if (edgeSet.containsKey(pair)) {
//                            edgeSet.put(pair, edgeSet.get(pair) + 1);    // Increment edge weight by one
//                        } else {
//                            edgeSet.put(pair, 1.0);						// Or create a new entry if first occurrence
//                        }
//                    }
//                }
//            }
//        }
//    }
    //Forms a complete graph of a single window
    private static HashMap<SemanticPair, Double> generateAssociativeNetworkOfSingleSentence(ArrayList<Token> line) {
        HashMap<SemanticPair, Double> edgeSet = new HashMap<>();

        for (int i = 0; i < line.size() - 1; i++) {
            for (int j = i + 1; j < line.size(); j++) {
                if (!line.get(i).equals(line.get(j))) {
                    SemanticPair pair = new SemanticPair(line.get(i).getSignature(), line.get(j).getSignature());
                    if (edgeSet.containsKey(pair)) {
                        edgeSet.put(pair, (double) edgeSet.get(pair) + 1.0);
                    } else {
                        edgeSet.put(pair, 1.0);
                    }
                }
            }
        }
        return edgeSet;
    }

}
