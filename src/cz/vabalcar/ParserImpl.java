package cz.vabalcar;

abstract class ParserImpl<TOut> implements Parser<TOut> {
    protected MultiParser<?> parentParser;
    public ParserImpl(MultiParser<?> parentParser) {
        this.parentParser = parentParser;
    }
    public MultiParser<?> getParentParser() {
        return parentParser;
    }
}
