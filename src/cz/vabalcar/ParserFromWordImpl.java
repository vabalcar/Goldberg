package cz.vabalcar;

import java.io.IOException;

abstract class ParserFromWordImpl<TOut> extends ParserImpl<TOut> implements ParserFromWord<TOut> {
    protected TOut parsedValue;

    public ParserFromWordImpl(MultiParser<?> parentParser) {
        super(parentParser);
    }
    public TOut getParsedValue() {
        return parsedValue;
    }
    public boolean tryParse(WordReader reader) throws IOException {
        String word = reader.readNextWord();
        return word != null && tryParse(word);
    }
    public abstract boolean tryParse(String word);
}
