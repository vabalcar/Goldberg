package cz.vabalcar;

class FloatParser extends NumericTypeParserImpl<Float> {
    public FloatParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Float parseNumber(String word) throws NumberFormatException {
        return Float.parseFloat(word);
    }
}
