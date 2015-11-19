package netgen;

import netgen.Token;




public class AnnotationToken extends Token {
    
    
    public AnnotationToken (String signature) {
        super(signature);
        this.type = TokenType.Annotation;
    }
    
}
