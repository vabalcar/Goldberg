package cz.vabalcar;

import java.io.IOException;

interface Parser<TOut> {
    MultiParser<?> getParentParser();
    default MultiParser<?> getRootParser() {
        MultiParser<?> parser = getParentParser();
        while ((parser.getParentParser()) != null) {
            parser = parser.getParentParser();
        }
        return parser;
    }
    TOut getParsedValue();
    boolean tryParse(WordReader reader) throws IOException;
    default TOut parse(WordReader reader) throws IOException {
        return tryParse(reader)? getParsedValue() : null;
    }
}
