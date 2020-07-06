package cz.vabalcar;

class IntParser extends NumericTypeParserImpl<Integer> {
    public IntParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Integer parseNumber(String word) throws NumberFormatException {
        return Integer.parseInt(word);
    }
}
