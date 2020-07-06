package cz.vabalcar;

class ByteParser extends NumericTypeParserImpl<Byte> {
    public ByteParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Byte parseNumber(String word) throws NumberFormatException {
        return Byte.parseByte(word);
    }
}
