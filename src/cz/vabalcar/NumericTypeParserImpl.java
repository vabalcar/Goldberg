package cz.vabalcar;

abstract class NumericTypeParserImpl<T extends Number> extends ParserFromWordImpl<T> {
    public NumericTypeParserImpl(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    public boolean tryParse(String word) {
        T value;
        try {
            value = parseNumber(word);
        } catch (NumberFormatException e) {
            return false;
        }
        parsedValue = value;
        return true;
    }
    protected abstract T parseNumber(String word) throws NumberFormatException;
}
