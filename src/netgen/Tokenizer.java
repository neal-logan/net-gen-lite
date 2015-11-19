package netgen;

import java.util.ArrayList;


public class Tokenizer {

    //Takes a filtered sentence and returns its contents as a list of tokens
    public static ArrayList<Token> tokenize(ArrayList<String> input) {

        ArrayList<Token> tokenizedCorpus = new ArrayList<>();

        for (String line : input) {
            tokenizedCorpus.addAll(tokenizeLine(line));
            tokenizedCorpus.add(new AnnotationToken("<SentenceSplit>"));
        }
        return tokenizedCorpus;
    }

    public static ArrayList<Token> tokenizeLine(String input) {
        ArrayList<Token> sentence = new ArrayList<>();
        String[] split = input.split("\\s+");

        for (String word : split) {
            word = word.trim();
            if (word.length() > 0) {
                sentence.add(new SemanticToken(word));
            }
        }

        return sentence;

    }

}
