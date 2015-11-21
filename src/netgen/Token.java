package netgen;



//Wrapper class for the String signatures of annotations and semantic tokens

public abstract class Token

{
    
    public String signature = "";
    public TokenType type;
    
    //CONSTRUCTORS
    public Token(String signature) {
        this.setSignature(signature);
    }
    
    //ACCESSORS AND MUTATORS
    
    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

  
    @Override
    public boolean equals(Object other) {
        if(other.getClass() != this.getClass()) {
            return false;
        }
        
        Token o = (Token)other;
        
        if (this.signature.equals(o.getSignature())) {
            return true;
        } else {
            return false;
        }
    }
   
    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    public void print() {
        System.out.print(this.signature);
    }


    public enum TokenType {
        Annotation,
        Semantic
    }
    
    @Override
    public String toString(){
        return this.getSignature();
    }
    
    
}
