package netgen;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author Neal
 */
public class IO {

//    public static ArrayList<Article> importEntireSourcesFolder() throws Exception {
//        ArrayList<Article> fullSourceFolderCorpii = new ArrayList<>();
//
//        String filepath = new File("").getAbsolutePath();
//        filepath = filepath.concat("/src/netgen/DataSources/Sources");
//        File dir = new File(filepath);
//        File[] directoryListing = dir.listFiles();
//        if (directoryListing != null) {
//            for (File child : directoryListing) {
//                String mode = child.getName().split("\\.")[0];
//                System.out.println("mode = " + mode);
//                filepath = "src/netgen/DataSources/Sources/" + child.getName();
//                System.out.println("filepath = " + filepath);
//                fullSourceFolderCorpii.addAll(importCSV(read(filepath), mode));
//            }
//        } else {
//            throw new Exception("Could not find the Data Sources folder!");
//        }
//        return fullSourceFolderCorpii;
//    }
    
    //Reads a file & returns each line in the file as a string
    public static String readFileAsString(String fileName) {
        return concatenateAll(readFileAsLines(fileName));
    }
    
    //Reads a file and returns its lines in an arraylist
    public static ArrayList<String> readFileAsLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        Scanner inFile = null;

        try {
            System.out.println(new File("").getAbsolutePath());
            System.out.println(fileName);
            inFile = new Scanner(new FileReader(fileName));
        } catch (Exception e) {
            System.out.println("Failed to open input file. Exiting.");
            System.exit(-1);
        }

        while (inFile.hasNextLine()) {
            lines.add(inFile.nextLine());
        }
        return lines;
    }
    
    //Combines the lines of the lines of the arraylist into a single String, separated by newline characters
    //TODO: throw exception if too many chars in strings?
    //Must have a total of less than about 2^30 characters
    public static String concatenateAll(ArrayList<String> lines) {
        String condensed = "";
        for (String line : lines) {
            condensed += ("\n" + line);
        }
        return condensed;
    }

    //Takes the lines of a .dl file and returns a weighted edgelist
    public static HashMap<OrderedSemanticPair, Double> importWeightedDirectedEdgelist(ArrayList<String> input) {
        
        HashMap<OrderedSemanticPair, Double> edges = new HashMap<>();
        
        for(int i = 4; i < input.size(); i++) {
            String[] line = input.get(i).split("\\s");
            edges.put(new OrderedSemanticPair(line[0], line[1]), Double.parseDouble(line[2]));
        }
        return edges;
    }
    
    //Produces an arraylist of KTUU, ADN or Homer Tribune corpora 
    //from unprocessed lines of text
    public static ArrayList<Article> importArticles(String input, String mode) {

        ArrayList<Article> corpora = new ArrayList<>();

        ArrayList<String> rows = new ArrayList<>();
        ArrayList<String> fields = new ArrayList<>();

        rows.addAll(Arrays.asList(input.split("<ROW>")));

        for (String row : rows) {
            fields.addAll(Arrays.asList(row.split("<COL>")));
        }

        //debug
        //        System.out.println("Fields: " + fields.size());
        //
        //        int count = 1;
        //        for (String field : fields) {
        //            System.out.println("Field " + count + ": " + field);
        //            count++;
        //        }
        if (mode.equalsIgnoreCase("adn")) {
            //in DB dump: Article, Link, Title, Date
            for (int i = 3; i < fields.size(); i += 4) {
                String inRaw = fields.get(i - 3);
                String inSource = "adn";
                String inDate = fields.get(i);
                String inTitle = fields.get(i - 1);
                String inSummary = "";
                String inLink = fields.get(i - 2);
                Article article = new Article(inRaw, inSource,
                        inDate, inTitle, inSummary, inLink);
                corpora.add(article);
            }

        } else if (mode.equalsIgnoreCase("ktuu")) {

            //in DB dump: Link, Summary, Article, Title, Date
            for (int i = 4; i < fields.size(); i += 5) {
                String inRaw = fields.get(i - 2);
                String inSource = "ktuu";
                String inDate = fields.get(i);
                String inTitle = fields.get(i - 1);
                String inSummary = fields.get(i - 3);
                String inLink = fields.get(i - 4);
                Article article = new Article(inRaw, inSource,
                        inDate, inTitle, inSummary, inLink);
                corpora.add(article);
            }

        } else if (mode.equalsIgnoreCase("tribune")) {

            //in DB dump: Link, Article, Title, Date
            for (int i = 3; i < fields.size(); i += 4) {
                String inRaw = fields.get(i - 2);
                String inSource = "tribune";
                String inDate = fields.get(i);
                String inTitle = fields.get(i - 1);
                String inSummary = "";
                String inLink = fields.get(i - 3);
                Article article = new Article(inRaw, inSource,
                        inDate, inTitle, inSummary, inLink);
                corpora.add(article);
                
//                System.out.println(inDate);
                
            }

        } else {
            System.out.println("Unrecognized type of input file");
            System.exit(-1);
        }

        return corpora;
    }

}
