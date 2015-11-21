package netgen;

import netgen.SemanticToken;

public class WeightedEdge {
    private SemanticToken a;
    private SemanticToken b;
    private double weight;

    /**
     * @return the a
     */
    public String getA() {
        return a.signature;
    }
    
    /**
     * @return the b
     */
    public String getB() {
        return b.signature;
    }
    
    public WeightedEdge(SemanticToken a, SemanticToken b, double weight) {
        if(a.getSignature().compareTo(b.getSignature()) > 0) {
            this.a = new SemanticToken(a.getSignature());
            this.b = new SemanticToken(b.getSignature());
        } else {
            this.a = new SemanticToken(b.getSignature());
            this.b = new SemanticToken(a.getSignature());
        }
        this.weight = weight;
    }
    
    public WeightedEdge(String a, String b, double weight) {
        if(a.compareTo(b) > 0) {
            this.a = new SemanticToken(a);
            this.b = new SemanticToken(b);
        } else {
            this.a = new SemanticToken(b);
            this.b = new SemanticToken(a);
        }    
        this.weight = weight;
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

    @Override
    public int hashCode() {
        return a.hashCode()/2 + b.hashCode()/2;
    }
    
    /**
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    
    
}