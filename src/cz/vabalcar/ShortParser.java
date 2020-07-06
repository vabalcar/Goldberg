package cz.vabalcar;

class ShortParser extends NumericTypeParserImpl<Short> {
    public ShortParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Short parseNumber(String word) throws NumberFormatException {
        return Short.parseShort(word);
    }
}
