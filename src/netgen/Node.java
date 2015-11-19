
package netgen;

import java.util.HashMap;

/**
 *
 * @author Neal
 */
public class Node {
    
    public SemanticToken token;
    public HashMap<Token, Double> outProbability = new HashMap<>();
    public HashMap<Token, Double> inProbability = new HashMap<>();
    
    
    public Node(String string) {
        this.token = new SemanticToken(string);
    }
    
    public Node(SemanticToken token) {
        this.token = token;
    }
    
}
