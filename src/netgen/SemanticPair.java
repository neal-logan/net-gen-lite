package netgen;

import netgen.SemanticToken;

public class SemanticPair {

    private SemanticToken a;
    private SemanticToken b;

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

    public SemanticPair(SemanticToken a, SemanticToken b) {
        if (a.getSignature().compareTo(b.getSignature()) > 0) {
            this.a = new SemanticToken(a.getSignature());
            this.b = new SemanticToken(b.getSignature());
        } else {
            this.a = new SemanticToken(b.getSignature());
            this.b = new SemanticToken(a.getSignature());
        }
    }

    public SemanticPair(String a, String b) {
        if (a.compareTo(b) > 0) {
            this.a = new SemanticToken(a);
            this.b = new SemanticToken(b);
        } else {
            this.a = new SemanticToken(b);
            this.b = new SemanticToken(a);
        }
    }

    @Override
    public boolean equals(Object other) {

        if (!other.getClass().equals(this.getClass())) {
            return false;
        } else if (!this.a.equals(((SemanticPair) other).getA())) {
            return false;
        } else if (!this.b.equals(((SemanticPair) other).getB())) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        return a.hashCode() / 2 + b.hashCode() / 2;
    }

}
