package netgen;

import netgen.Token;

public class OrderedSemanticPair {

    private Token a;
    private Token b;

    /**
     * @return the a
     */
    public Token getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public void setA(Token a) {
        this.a = a;
    }

    /**
     * @return the b
     */
    public Token getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public void setB(Token b) {
        this.b = b;
    }

    public OrderedSemanticPair(Token a, Token b) {
        this.a = new SemanticToken(a.getSignature());
        this.b = new SemanticToken(b.getSignature());
    }

    public OrderedSemanticPair(String a, String b) {
        this.a = new SemanticToken(a);
        this.b = new SemanticToken(b);
    }
    
    @Override
    public boolean equals(Object other) {
        
        if(!other.getClass().equals(this.getClass())) {
            return false;
        } else if (!this.a.equals(((OrderedSemanticPair)other).getA())) {
            return false;
        } else if (!this.b.equals(((OrderedSemanticPair)other).getB())) {
            return false;
        } else {
            return true;
        }
        
    }    
    
}
