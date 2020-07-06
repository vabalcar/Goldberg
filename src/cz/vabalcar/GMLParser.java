package cz.vabalcar;

import java.io.IOException;
import java.util.Map;

class GMLParser extends ParserImpl<Map<String, Object>> {
    private Map<String, Object> parsedValue;
    private MultiParser<Object> parser = new MultiParser<>(null);
    public GMLParser(MultiParser<?> parentParser) {
        super(parentParser);
        parser.registerParser(StringTreeNode.class, new StringTreeParser(parser, "[", "]"), 0);
        parser.registerParser(Number.class, new NumberParser(parser), 1);
        parser.registerParser(Character.class, new CharParser(parser), 2);
        parser.registerParser(String.class, new StringParser(parser,'"'), 3);
        parser.registerParser(StringObjectHashMap.class, new StringObjectMapParser(parser,"]"));
        parser.freeze();
    }
    @Override
    public Map<String, Object> getParsedValue() {
        return parsedValue;
    }
    @Override
    public boolean tryParse(WordReader reader) throws IOException {
        return (parsedValue = parser.parse(StringObjectHashMap.class, reader)) != null;
    }
}
