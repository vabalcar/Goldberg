package cz.vabalcar;

class StringObjectMapParser extends MapParser<String> {
    public StringObjectMapParser(MultiParser<?> parentParser, String stoppingWord) {
        super(parentParser, stoppingWord);
    }
}
