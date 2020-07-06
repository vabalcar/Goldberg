package cz.vabalcar;

import java.io.IOException;
import java.util.Map;

class StringTreeParser extends TreeParser<String> {
    public StringTreeParser(MultiParser<?> parentParser, String treeBegin, String treeEnd) {
        super(parentParser, treeBegin, treeEnd);
    }
    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseAttributes(WordReader reader) throws IOException {
        try {
            return ((MultiParser<Object>)getRootParser()).parse(StringObjectHashMap.class, reader);
        } catch (ClassCastException e) {
            return null;
        }
    }
}
