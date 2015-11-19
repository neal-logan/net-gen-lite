package netgen;





public class Tag {
    
    private TagType type;
        
    public Tag(TagType type) {
        this.type = type;
    }
    
    public enum TagType {
        SEMANTIC,
        ANNOTATION,
        NAMED_ENTITY,
        ADJ,
        ADV,
        NOUN,
        VERB,
        STOP_WORD,
        MULTI_WORD,
    }
    
}
