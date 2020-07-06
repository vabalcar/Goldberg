package cz.vabalcar;

interface ParserFromWord<TOut> extends Parser<TOut> {
    boolean tryParse(String word);
    default TOut parse(String word) {
        return tryParse(word)? getParsedValue() : null;
    }
}
