/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import netgen.stemmers.Porter2;
import netgen.stemmers.TokenProcessor;

/**
 *
 * @author Neal
 */
public class Scripts {

    public static void generateAndExportIndividualNetworksForEachArticle(String name) {

        //Initialize Stemmer
        TokenProcessor stemmer = new Porter2();

        //Import stopwords
        HashSet<Token> stopwords = new HashSet<>();

        String stops = IO.readFileAsString("stopwords_combined.txt");
        stops = Filter.nonpermittedCharacters(stops);
        stopwords.addAll(Tokenizer.tokenizeLine(stops));

        //Import database dumps as articles
        System.out.println("Importing and splitting into corpora");
        ArrayList<Article> corpora = IO.importArticles(IO.readFileAsString(name + ".csv"), name);
        System.out.println("Split into " + corpora.size() + " articles");

        //Put the corpora in chronological order
        corpora.sort(null);

        int i = 0;
        for (Article article : corpora) {

            //Split on sentences, filter nonpermitted characters, and tokenizes
            article.processedText = Tokenizer.tokenize(Filter.nonpermittedCharacters(SentenceSplitter.splitSentences(article.text)));

            //Remove stopwords
            article.removeAll(stopwords);

            //Stems all semantic tokens
            article.stem(stemmer);

//            System.out.println("Unique tokens: " + corpus.getTokenSet().size());
//            System.out.println("Tokenized Sentences: " + corpus.getProcessedText().size());
            Network network = new Network(
                    Network.generateAssociativeSemanticNetworkByMultiSentenceSlidingWindow(article.processedText, 3, 75),
                    article.calendar);

            network.limitEdges(2000);
//            network.normalizeToHighestEdge();
//            network.filterEdgesBelow(0.15);
            network.writeEdgelistWithAdditionalMetadata("" + i);
            i++;

            if (i % 100 == 0) {
                System.out.println("Created " + i + " networks so far");
            }

        }

        System.out.println("Finished. Created " + i + " networks");
    }

    
    //Hopefully pretty much does what it says
    public static void generateAndExportMarkovChain(String name) {

        //Initialize Stemmer
        TokenProcessor stemmer = new Porter2();

        //Import database dumps as articles
        System.out.println("Importing and splitting into corpora");
        ArrayList<Article> corpus = new ArrayList<>();
        corpus.addAll(IO.importArticles(IO.readFileAsString("ktuu.csv"), "ktuu"));
        corpus.addAll(IO.importArticles(IO.readFileAsString("adn.csv"), "adn"));
        corpus.addAll(IO.importArticles(IO.readFileAsString("tribune.csv"), "tribune"));

        System.out.println("Split into " + corpus.size() + " articles");

        ArrayList<Token> list = new ArrayList<>();

        for (Article article : corpus) {

            //Split on sentences, filter nonpermitted characters, and tokenize
            article.processedText = Tokenizer.tokenize(
                    Filter.nonpermittedCharacters(
                            SentenceSplitter.splitSentences(article.text)));

            //Stems all semantic tokens
            article.stem(stemmer);

            //Put all tokens in a single long list
            list.addAll(article.processedText);
        }

        
        WeightedDirectedNetwork markovChain = new WeightedDirectedNetwork();

        markovChain.edgeCount = WeightedDirectedNetwork.generateEdgeCount(list);
        
        markovChain.writeEdgelist(name);
    }

    
    
    //Simple version based on markov chain
    public static void generateAndExportSynonymityNetwork (String markovChainFileName, String outputFileName) {
        
        WeightedDirectedNetwork markovChain = new WeightedDirectedNetwork();
        
        //TODO: Collapse this into constructor?
        HashMap<OrderedSemanticPair, Double> edges = IO.importWeightedDirectedEdgelist(IO.readFileAsLines(markovChainFileName));
        
        for(OrderedSemanticPair pair : edges.keySet()) {
            markovChain.edgeCount.put(pair, edges.get(pair).intValue());
        }
        
        markovChain.populateAdditionalAttributes(10, 1);
        
        Network synonimityNetwork = markovChain.extractSynonimityNetwork();
        
        synonimityNetwork.writeEdgelist(outputFileName);
    }

   

}
