package netgen;

import netgen.stemmers.Porter2;
import netgen.stemmers.TokenProcessor;
import java.util.ArrayList;
import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
//        Scripts.generateAndExportWeightedEdgelistFromArticles("edgelist_unstemmed");
        
        for(int i = 1; i <= 10; i++) {
            Scripts.generateAndExportWeightedEdgelistFromText(
                    "NewsArticle" + i + ".txt", 
                    "FullArticle" + i + "Edgelist",
                    false, //Stem
                    true); //Stopword removal
            Scripts.generateAndExportWeightedEdgelistFromText(
                    "PartialNewsArticle" + i + ".txt", 
                    "PartialArticle" + i + "Edgelist",
                    false, //Stem
                    true); //Stopword removal
            Scripts.generateAndExportMarkovChainAndSynonymityNetwork(
                "FullArticle" + i + "edgelist.dl", 
                "FullArticleMarkovChain" + i, 
                "FullArticleSynNet" + i,
                0, //Token threshold
                0); //Edge threshold
            Scripts.generateAndExportMarkovChainAndSynonymityNetwork(
                "PartialArticle" + i + "edgelist.dl", 
                "PartialArticleMarkovChain" + i, 
                "PartialArticleSynNet" + i,
                0, //Token threshold
                0); //Edge threshold
        }        
        
        
    }
    
}
