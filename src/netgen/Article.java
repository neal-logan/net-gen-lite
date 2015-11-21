package netgen;

import netgen.stemmers.TokenProcessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.HashMap;

/* 
 * This class represents an article which has already been through preliminary processing:
 * It is already tokenized and split on sentences
 * All other text processing should be done to articles in this format, including
 * but not limited to stemming/lemmatizing, NER, POS-tagging, and stop-word removal
 */
public class Article implements Comparable<Article> {

    public String title;
    public String link;
    public String summary;
    public String text;
    public String source;
    public ArrayList<Token> processedText;
    public Calendar calendar;

    public Article(String text, String source, String date, String title, String summary, String link) {
        this.text = text;
        this.source = source;
        this.title = title;
        this.summary = summary;
        this.link = link;
        calendar = new GregorianCalendar();
        this.setDate(date);
    }

    //METADATA GENERATION METHODS
    public HashMap<Token, Integer> getTokenFrequencyMap() {

        HashMap<Token, Integer> map = new HashMap<>();

        for (Token token : processedText) {

            if (map.containsKey(token)) {
                int value = map.get(token) + 1;
                map.put(token, value);
            } else {
                map.put(token, 1);
            }

        }
        return map;
    }

    //Returns a set of all unique tokens in the corpus
    public HashSet<Token> getUniqueTokenSet() {
        HashSet<Token> tokenSet = new HashSet<>();
        for (Token token : processedText) {
            tokenSet.add(token);
        }
        return tokenSet;
    }

    //ACCESSORS AND MUTATORS
    //Accepts only YYYY-MM-DD format
    //Warns & sets to "UNKOWN DATE" if provided non-matching string
    //TODO: Check
    public void setDate(String date) {

        if (date.trim().matches("[0-9]{4}(-[0-9]{2}){2}")) {

            this.calendar.set(Integer.parseInt(date.substring(0, 4)),
                    Integer.parseInt(date.substring(5, 7)),
                    Integer.parseInt(date.substring(8, 10)));
        } else {
            this.calendar.set(0, 0, 0);
            System.out.println("Date? " + date);
        }
    }

    //TODO: this is hacky. Make them comparable other ways as well
    @Override
    public int compareTo(Article other) {
        return this.calendar.compareTo(other.calendar);
    }

    //Processing methods
    public void stemUsing(TokenProcessor stemmer) {
        for (Token token : processedText) {
            if (token.type == Token.TokenType.Semantic) {
                token.signature = stemmer.stem(token.signature);
            }
        }
    }

    public void removeAll(HashSet<Token> stopwords) {

        ArrayList<Token> filteredText = new ArrayList<>();

        for (Token token : processedText) {
            if (token.type == Token.TokenType.Annotation) {
                filteredText.add(token);
            } else if (token.type == Token.TokenType.Semantic
                    && !stopwords.contains(token)) {
                filteredText.add(token);
            }
        }

        processedText.removeAll(stopwords);
    }

}
