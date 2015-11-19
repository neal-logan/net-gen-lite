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
        if (this.signature.equalsIgnoreCase(((Token) other).getSignature())) {
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
    
    
}
