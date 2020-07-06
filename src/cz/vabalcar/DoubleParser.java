package cz.vabalcar;

class DoubleParser extends NumericTypeParserImpl<Double> {
    public DoubleParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Double parseNumber(String word) throws NumberFormatException {
        return Double.parseDouble(word);
    }
}
