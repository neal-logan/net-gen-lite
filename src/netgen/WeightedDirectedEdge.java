
package netgen;

/**
 *
 * @author Neal
 */
public class WeightedDirectedEdge implements Comparable {
    
    public OrderedSemanticPair pair;
    public double weight;
    
    
    public WeightedDirectedEdge(OrderedSemanticPair pair, double weight) {
        this.pair = pair;
        this.weight = weight;
    }
    
    public WeightedDirectedEdge(String a, String b, double weight) {
        pair = new OrderedSemanticPair(a, b);
        this.weight = weight;
    }
    
    //Compares by edge weight
    //Warning: Behavior is not consistent with equals()
    @Override
    public int compareTo(Object other) throws NullPointerException, ClassCastException {
        if (other == null) {
            throw new NullPointerException();
        } else if (!other.getClass().equals(this.getClass())) {
            throw new ClassCastException();
        }
                
        WeightedDirectedEdge otherEdge = (WeightedDirectedEdge) other;
        
        if (this.getWeight() > otherEdge.getWeight()) {
            return 1;
        } else if (this.getWeight() < otherEdge.getWeight()) {
            return -1;
        } else {
            return 0;
        }

    }
    
    //Warning: Edges are checked for equality and hashed by their incident vertices
    //without respect for their weight
    @Override
    public boolean equals(Object other) {
        if(other.getClass() != this.getClass()) {
            return false;
        } else if (this.pair.equals(((WeightedDirectedEdge)other).pair)) {
            return true;
        } else {
            return false;
        }
    }
    
    //Warning: Edges are checked for equality and hashed by their incident vertices
    //without respect for their weight
    @Override
    public int hashCode() {
        return (pair.getA().hashCode()/2 + pair.getA().hashCode()%2 - pair.getB().hashCode()%2 + pair.getB().hashCode()/2);
    }

    /**
     * @return the pair
     */
    public OrderedSemanticPair getIncidentTokens() {
        return pair;
    }

    /**
     * @param pair the pair to set
     */
    public void setTokens(OrderedSemanticPair pair) {
        this.pair = pair;
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
