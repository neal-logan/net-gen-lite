package netgen.stemmers;

//SUPPORTS PORTER II / SNOWBALL

import java.lang.reflect.InvocationTargetException;

public abstract class SnowballStemmer extends SnowballProgram {
    public abstract boolean stem();
};
