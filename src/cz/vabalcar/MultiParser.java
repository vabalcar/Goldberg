package cz.vabalcar;

import java.io.IOException;
import java.util.*;

class MultiParser<T> extends ParserFromWordImpl<T> {
    private Map<Class<? extends T>, Parser<? extends T>> parsers = new HashMap<>();
    private Map<Parser<? extends T>, Integer> priority = new HashMap<>();
    private List<Parser<? extends T>> sortedParsers;
    private List<ParserFromWord<? extends T>> sortedParsersFromWord;

    private boolean frozen = false;

    public MultiParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    public T parse(WordReader reader) throws IOException {
        if (!frozen) throw new IllegalStateException();
        for (Parser<? extends T> parser : sortedParsers) {
            if (reader.setLastWordProcessed(parser.tryParse(reader))) {
                return parser.getParsedValue();
            }
        }
        return null;
    }

    @Override
    public boolean tryParse(String word) {
        if (!frozen) throw new IllegalStateException();
        for (ParserFromWord<?> parser : sortedParsersFromWord) {
            if (parser.tryParse(word)) {
                parsedValue = (T)parser.getParsedValue();
                return true;
            }
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public T parse(String word) {
        if (!frozen) throw new IllegalStateException();
        return super.parse(word);
    }

    @SuppressWarnings("unchecked")
    public <S extends T> S parse(Class<? extends S> c, String word) {
        if (!frozen) throw new IllegalStateException();
        ParserFromWord<S> parser = (ParserFromWord<S>) parsers.get(c);
        return parser.tryParse(word)? parser.getParsedValue() : null;
    }
    @SuppressWarnings("unchecked")
    public <S extends T> S parse(Class<? extends S> c, WordReader reader) throws IOException {
        if (!frozen) throw new IllegalStateException();
        Parser<S> parser = (Parser<S>) parsers.get(c);
        return reader.setLastWordProcessed(parser.tryParse(reader))? parser.getParsedValue() : null;
    }

    public boolean isFrozen() {
        return frozen;
    }
    @SuppressWarnings("unchecked")
    public void freeze() {
        sortedParsers = new ArrayList<>(priority.keySet());
        sortedParsers.sort(Comparator.comparing(p -> priority.get(p)));
        sortedParsersFromWord = new ArrayList<>();
        sortedParsers.forEach((Parser<? extends T> p) -> { if (p instanceof ParserFromWord<?>) sortedParsersFromWord.add((ParserFromWord<? extends T>) p); });
        priority = null;
        System.gc();
        this.frozen = true;
    }
    protected  <S extends T> void registerParser(Class<? extends S> c, Parser<? extends S> parser) {
        if (frozen) throw new IllegalStateException();
        parsers.put(c, parser);
    }
    protected  <S extends T> void registerParser(Class<? extends S> c, Parser<? extends S> parser, int priority) {
        if (frozen) throw new IllegalStateException();
        parsers.put(c, parser);
        if (this.priority.put(parser, priority) != null) throw new IllegalArgumentException();
    }
}
