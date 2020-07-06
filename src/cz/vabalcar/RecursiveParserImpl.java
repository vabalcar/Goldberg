package cz.vabalcar;

import java.util.Stack;

abstract class RecursiveParserImpl<TOut> extends ParserImpl<TOut> {
    protected final Stack<TOut> parsedValues = new Stack<>();

    public RecursiveParserImpl(MultiParser<?> parentParser) {
        super(parentParser);
    }
    public TOut getParsedValue() {
        return parsedValues.pop();
    }
}
