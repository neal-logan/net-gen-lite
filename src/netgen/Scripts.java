/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import netgen.Token.TokenType;
import netgen.stemmers.Porter2;
import netgen.stemmers.TokenProcessor;

/**
 *
 * @author Neal Logan
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
            article.stemUsing(stemmer);

//            System.out.println("Unique tokens: " + corpus.getTokenSet().size());
//            System.out.println("Tokenized Sentences: " + corpus.getProcessedText().size());
            Network network = new Network(
                    Network.generateAssociativeSemanticNetworkByMultiSentenceSlidingWindow(article.processedText, 3, 75),
                    article.calendar);

//            network.limitEdges(2000);
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

    //Takes the KTUU, Tribune and ADN .csv files, creates a single adjacency network and exports it as a .dl
    //Stemming and stopword removal may be omitted
    public static void generateAndExportWeightedEdgelistFromArticles(String name) {

        //Initialize Stemmer
        TokenProcessor stemmer = new Porter2();

        //Import database dumps as articles
        System.out.println("Importing and splitting into corpora");
        ArrayList<Article> corpus = new ArrayList<>();
        corpus.addAll(IO.importArticles(IO.readFileAsString("ktuu.csv"), "ktuu"));
        corpus.addAll(IO.importArticles(IO.readFileAsString("adn.csv"), "adn"));
        corpus.addAll(IO.importArticles(IO.readFileAsString("tribune.csv"), "tribune"));

        System.out.println("Split into " + corpus.size() + " articles");

        //Import stopwords
        HashSet<Token> stopwords = new HashSet<>();
        String stops = IO.readFileAsString("stopwords_combined.txt");
        stops = Filter.nonpermittedCharacters(stops);
        stopwords.addAll(Tokenizer.tokenizeLine(stops));

        //Process articles into a list of tokens
        ArrayList<Token> list = new ArrayList<>();
        for (Article article : corpus) {

            //Split on sentences, filter nonpermitted characters, and tokenize
            article.processedText = Tokenizer.tokenize(
                    Filter.nonpermittedCharacters(
                            SentenceSplitter.splitSentences(article.text)));

            //Remove stopwords
//            article.processedText.removeAll(stopwords);

            //Stems all semantic tokens
//            article.stemUsing(stemmer);

            //Put all tokens in a single long list
            list.addAll(article.processedText);
        }
        
        //Filter tokens with only one letter
        ArrayList<Token> filteredList = new ArrayList<>();
        for(Token token : list) {
            if (token.signature.length() > 1) {
                filteredList.add(token);
            }
        }
        list = filteredList;
        System.out.println("Tokens with less than two characters removed");
        

        int sizeWithoutAnnotators = 0;

        for (Token token : list) {
            if (token.type == TokenType.Semantic) {
                sizeWithoutAnnotators++;
            }
        }

        System.out.println("List size: " + sizeWithoutAnnotators + "/" + list.size() + " tokens");

        HashSet<Token> tokenSet = new HashSet<>();
        tokenSet.addAll(list);

        System.out.println("Unique tokens in list: " + tokenSet.size() + " tokens");

        WeightedDirectedNetwork weightedEdgelist = new WeightedDirectedNetwork(list);

        weightedEdgelist.writeEdgelist(name);
    }

    
    //Tokenizes the text in the input file, creates an adjacency network and exports that as a .dl
    //Messy due to adaptation
    //No stemming or stopword removal, currently
    public static void generateAndExportWeightedEdgelistFromText(
            String inputFile, String outputFile, 
            boolean stem, boolean removeStopwords) {

        //Initialize Stemmer
        TokenProcessor stemmer = new Porter2();

        //Process the text into a list of tokens
        System.out.println("Importing text");

        String text = IO.readFileAsString(inputFile);
        ArrayList<Token> list = Tokenizer.tokenize(
                    Filter.nonpermittedCharacters(
                            SentenceSplitter.splitSentences(text)));

        //Import stopwords
        HashSet<Token> stopwords = new HashSet<>();
        String stops = IO.readFileAsString("stopwords_combined.txt");
        stops = Filter.nonpermittedCharacters(stops);
        stopwords.addAll(Tokenizer.tokenizeLine(stops));
        
        if(removeStopwords) {
            list.removeAll(stopwords);
        }
        
        //Filter tokens with only one letter
//        ArrayList<Token> filteredList = new ArrayList<>();
//        for(Token token : list) {
//            if (token.signature.length() > 1) {
//                filteredList.add(token);
//            }
//        }
//        list = filteredList;
//        System.out.println("Tokens with less than two characters removed");

        if(stem) {
            Article article = new Article();
            article.processedText = list;
            article.stemUsing(stemmer);
            list = article.processedText;
        }
        
        
        
        
        int sizeWithoutAnnotators = 0;

        for (Token token : list) {
            if (token.type == TokenType.Semantic) {
                sizeWithoutAnnotators++;
            }
        }

        System.out.println("List size: " + sizeWithoutAnnotators + "/" + list.size() + " tokens");

        HashSet<Token> tokenSet = new HashSet<>();
        tokenSet.addAll(list);

        System.out.println("Unique tokens in list: " + tokenSet.size() + " tokens");

        WeightedDirectedNetwork weightedEdgelist = new WeightedDirectedNetwork(list);

        weightedEdgelist.writeEdgelist(outputFile);
    }
    
    
    //Simple version based on markov chain
    public static void generateAndExportMarkovChainAndSynonymityNetwork(
            String edgeListFileName, String markovFileName, 
            String outputFileName, int tokenThreshold, int edgeThreshold) {

        //Import edge set
        HashMap<OrderedSemanticPair, Double> edges = IO.importWeightedDirectedEdgelist(IO.readFileAsLines(edgeListFileName));
        WeightedDirectedNetwork edgeset = new WeightedDirectedNetwork(edges);

        //Filter edge set
        edgeset.removeTokensBelow(tokenThreshold);
        edgeset.removeEdgesBelow(edgeThreshold);
        

        //Create and write markov chain
        System.out.println("Starting Markov Chain Generation");
        MarkovChain markovChain = edgeset.toMarkovChain();
        WeightedDirectedNetwork markovOut = markovChain.outEdgesToWeightedDirectedNetwork();
        markovOut.writeEdgelist("markov");

        //DEBUG
//        for(OrderedSemanticPair edge : markovOut.edges.keySet()) {
//            if(markovOut.edges.get(edge) > 1.01) {
//                System.out.println("WARNING: " + edge.getA().signature + "  " + edge.getB().signature + "  " + edges.get(edge));
//            } 
//        }
        //Create and write synonymity network
        
        System.out.println("Starting Synonymity Network Generation");
        Network synNet = markovChain.toSynonymityNetwork();
        synNet.filterEdgesBelow(0.15);
        synNet.writeEdgelist(outputFileName);
        
                
        System.out.println("Script Complete");
    }

}
