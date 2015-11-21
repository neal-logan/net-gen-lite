package netgen;

import netgen.stemmers.Porter2;
import netgen.stemmers.TokenProcessor;
import java.util.ArrayList;
import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
//        Scripts.generateAndExportWeightedEdgelist("edgelist");
        Scripts.generateAndExportMarkovChainAndSynonymityNetwork("edgelist.dl", "markov", "syn-net");
    }
    
}
