package cz.vabalcar;

class LongParser extends NumericTypeParserImpl<Long> {
    public LongParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Long parseNumber(String word) throws NumberFormatException {
        return Long.parseLong(word);
    }
}
