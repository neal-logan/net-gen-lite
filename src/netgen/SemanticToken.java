package netgen;

import netgen.Token;




public class SemanticToken extends Token {
    
    public SemanticToken (String signature) {
        super(signature);
        this.type = TokenType.Semantic;
    }
    
}
