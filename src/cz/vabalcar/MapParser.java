package cz.vabalcar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MapParser<K> extends RecursiveParserImpl<Map<K, Object>> {
    private String stoppingWord;

    public MapParser(MultiParser<?> parentParser, String stoppingWord) {
        super(parentParser);
        this.stoppingWord = stoppingWord;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean tryParse(WordReader reader) throws IOException {
        parsedValues.push(new HashMap<>());
        if (reader.isEndReached()) {
            return false;
        }
        String atrKey;
        Object atrVal;
        while (!reader.isEndReached()) {
            atrKey = reader.readNextWord();
            if (atrKey == null || atrKey.equals(stoppingWord)) return true;
            atrVal = getRootParser().parse(reader);
            if (atrVal == null) {
                return true;
            }
            insert((K)getRootParser().parse(atrKey), atrVal);
        }
        return true;
    }
    @SuppressWarnings("unchecked")
    private void insert(K atrKey, Object atrVal) {
        if (parsedValues.peek().containsKey(atrKey)) {
            Object originalValue = parsedValues.peek().get(atrKey);
            if (originalValue instanceof List<?>) {
                List<Object> list = (List<Object>) parsedValues.peek().get(atrKey);
                list.add(atrVal);
            } else {
                List<Object> list = new ArrayList<>();
                list.add(parsedValues.peek().put(atrKey, list));
                list.add(atrVal);
            }
        } else {
            parsedValues.peek().put(atrKey, atrVal);
        }
    }
}
