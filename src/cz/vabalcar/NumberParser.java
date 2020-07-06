package cz.vabalcar;

class NumberParser extends MultiParser<Number> {
    public NumberParser(MultiParser<?> parentParser) {
        super(parentParser);
        registerParser(Byte.TYPE, new ByteParser(this), 0);
        registerParser(Short.TYPE, new ShortParser(this), 1);
        registerParser(Integer.TYPE, new IntParser(this), 2);
        registerParser(Long.TYPE, new LongParser(this), 3);
        registerParser(Float.TYPE, new FloatParser(this), 4);
        registerParser(Double.TYPE, new DoubleParser(this), 5);
        freeze();
    }
}
