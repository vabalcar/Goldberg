package cz.vabalcar;

class StringParser extends ParserFromWordImpl<String> {
    private boolean tryToExtractFirst;
    private char startBound;
    private char endBound;

    public StringParser(MultiParser<?> parentParser, char startBound, char endBound) {
        super(parentParser);
        this.startBound = startBound;
        this.endBound = endBound;
        tryToExtractFirst = true;
    }
    public StringParser(MultiParser<?> parentParser, char bound) {
        this(parentParser, bound, bound);
    }
    public StringParser(MultiParser<?> parentParser) {
        super(parentParser);
        tryToExtractFirst = false;
    }
    @Override
    public boolean tryParse(String word) {
        if (tryToExtractFirst && word.charAt(0) == startBound && word.charAt(word.length() - 1) == endBound) {
            parsedValue = word.substring(1, word.length() - 2);
        } else parsedValue = word;
        return true;
    }
}
