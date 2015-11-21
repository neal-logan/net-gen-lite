/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Neal
 */
public class MarkovChain {

    public HashMap<String, SemanticNode> nodes = new HashMap<>();
    
    public SemanticNode getNode(String signature) {
        return nodes.get(signature);
    }
    
    //Uses simpleCompareNeighborhoods to build a network indicating level of synonimity between tokens present in the corpus
    public Network toSynonymityNetwork() {

        Network network = new Network();

        ArrayList<Token> filteredTokenSet = new ArrayList<>();
        filteredTokenSet.addAll(nodes.values());
        
        //Compare neighborhoods of every pair of tokens
        //in the filtered tokenset and add results to synonimity network
        for (int i = 0; i < filteredTokenSet.size() - 1; i++) {
            for (int j = i + 1; j < filteredTokenSet.size(); j++) {
                network.edges.put(new SemanticPair(filteredTokenSet.get(i).signature, filteredTokenSet.get(j).signature),
                        SemanticNode.compareNodes(
                                nodes.get(filteredTokenSet.get(i).signature),
                                nodes.get(filteredTokenSet.get(j).signature)));
            }
            
            if(i%100 == 0) {
                System.out.println("Finished comparing " + i + " tokens of " + filteredTokenSet.size());
            }
        }
        
        System.out.println("Synonymity network completed");
        
        return network;
    }
    
    //Creates a weighted directed network representing the markov chain's inward-leading edges
    public WeightedDirectedNetwork inEdgesToWeightedDirectedNetwork() {
        HashMap<OrderedSemanticPair, Double> map = new HashMap<>();
        
        for(String key : nodes.keySet()) {
            SemanticNode node = nodes.get(key);
            for(Token token : node.inProbability.keySet()) {
                map.put(
                        new OrderedSemanticPair(token.signature, key),
                        node.inProbability.get(token.signature));
            }
        }
        
        return new WeightedDirectedNetwork(map);
    }
    
    //Creates a weighted directed network representing the markov chain's outward-leading edges
    public WeightedDirectedNetwork outEdgesToWeightedDirectedNetwork() {
        HashMap<OrderedSemanticPair, Double> map = new HashMap<>();
        
        for(String key : nodes.keySet()) {
            SemanticNode node = nodes.get(key);
            for(Token token : node.outProbability.keySet()) {
                double probability = node.outProbability.get(token);
                
                //Debug
                if(probability > 1.01) {
                    System.out.println("High probability during conversion\n" +node.toString());
                }
                
                map.put(
                        new OrderedSemanticPair(key, token.signature),
                        probability);
            }
        }
        
        return new WeightedDirectedNetwork(map);
    }
    
    //OUTPUT METHODS
    //Writes the graph to an .dl file, weighted directed edge list format
    public void writeInEdgelist(String fileName) {
        this.inEdgesToWeightedDirectedNetwork().writeEdgelist(fileName);
    }
    
    public void writeOutEdgelist(String fileName) {
        this.outEdgesToWeightedDirectedNetwork().writeEdgelist(fileName);
    }
    
}
