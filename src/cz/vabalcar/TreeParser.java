package cz.vabalcar;

import java.io.IOException;
import java.util.Map;

abstract class TreeParser<K> extends RecursiveParserImpl<TreeNode<K>> {
    private final String treeBegin;
    private final String treeEnd;

    public TreeParser(MultiParser<?> parentParser, String treeBegin, String treeEnd) {
        super(parentParser);
        this.treeBegin = treeBegin;
        this.treeEnd = treeEnd;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean tryParse(WordReader reader) throws IOException {
        if (reader.isEndReached()) return false;
        String word = reader.readNextWord();
        if (word != null && word.equals(treeBegin)) {
            parsedValues.push(new TreeNode<>());
            Object parsedMap = parseAttributes(reader);
            if (parsedMap == null) return false;
            Map<K, Object> map;
            try {
                map = (Map<K, Object>) parsedMap;
            } catch (ClassCastException e) {
                return false;
            }
            parsedValues.peek().addAttributes(map);
            word = reader.getLastReadWord();
            return word.equals(treeEnd);
        } else {
            return false;
        }
    }
    protected abstract Map<K,Object> parseAttributes(WordReader reader) throws IOException;
}
