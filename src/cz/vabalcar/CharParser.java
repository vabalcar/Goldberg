package cz.vabalcar;

class CharParser extends ParserFromWordImpl<Character> {
    public CharParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    public boolean tryParse(String word) {
        if (word.length() != 1) return false;
        parsedValue = word.charAt(0);
        return true;
    }
}
