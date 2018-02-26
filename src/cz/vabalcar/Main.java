package cz.vabalcar;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

interface Parser<TOut> {
    MultiParser<?> getParentParser();
    default MultiParser<?> getRootParser() {
        MultiParser<?> parser = getParentParser();
        while ((parser.getParentParser()) != null) {
            parser = parser.getParentParser();
        }
        return parser;
    }
    TOut getParsedValue();
    boolean tryParse(WordReader reader) throws IOException;
    default TOut parse(WordReader reader) throws IOException {
        return tryParse(reader)? getParsedValue() : null;
    }
}

interface ParserFromWord<TOut> extends Parser<TOut> {
    boolean tryParse(String word);
    default TOut parse(String word) {
        return tryParse(word)? getParsedValue() : null;
    }
}

interface TreeExtractor<K, TOut> {
    TOut extract(TreeNode<K> treeRoot);
}

interface GraphAlgorithm<TGraph extends Graph, R> extends Function<TGraph, R> {
}

class WordReader implements AutoCloseable {
    private final BufferedReader reader;
    private int lastReadValue = -2; //State when lastReadValue equals -2 occurs only when nothing has been read yet.
    private String lastReadWord;
    private String currentPeek;
    private boolean longWordEnabled = false;
    private char longWordStart;
    private char longWordEnd;

    private boolean lastWordProcessed = false;

    public WordReader(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }
    public WordReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }
    public WordReader(InputStream inputStream, char longWordStart, char longWordEnd) {
        this(inputStream);
        enableLongWords(longWordStart, longWordEnd);
    }
    public WordReader(Reader reader, char longWordStart, char longWordEnd) {
        this(reader);
        enableLongWords(longWordStart, longWordEnd);
    }
    private void enableLongWords(char longWordStart, char longWordEnd) {
        this.longWordStart = longWordStart;
        this.longWordEnd = longWordEnd;
        longWordEnabled = true;
    }
    public boolean isEndReached() {
        return lastReadValue == -1;
    }
    private boolean readWhile(Predicate<Integer> continuePredicate, boolean saveReadChars) throws IOException {
        if (isEndReached() || (lastReadValue >= 0 && !continuePredicate.test(lastReadValue))) return false;
        boolean charRead = false;
        StringBuilder sb = saveReadChars? new StringBuilder() : null;
        if (saveReadChars && lastReadValue >= 0) {
            sb.append((char)lastReadValue);
            charRead = true;
        }
        while ((lastReadValue = reader.read()) != -1 && continuePredicate.test(lastReadValue)) {
            if (!charRead) charRead = true;
            if (saveReadChars) sb.append((char)lastReadValue);
        }
        if (lastReadValue == -1) {
            System.out.println();
        }
        if (charRead && currentPeek != null) currentPeek = null;
        if (saveReadChars) lastReadWord = sb.toString();
        return charRead;
    }
    public void skipWhiteSpaces() throws IOException {
        readWhile(Character::isWhitespace, false);
    }

    public String readWord() throws IOException {
        try {
            if (!lastWordProcessed && lastReadWord != null) {
                return lastReadWord;
            }
            if (!readWhile(c -> !Character.isWhitespace(c), true)) return null;
            if (longWordEnabled && lastReadWord.length() > 0 && lastReadWord.charAt(0) == longWordStart) {
                StringBuilder wordBuilder = new StringBuilder();
                if (lastReadWord.length() > 1) wordBuilder.append(lastReadWord.substring(1));
                if (readWhile(new EndOfLongWordDetector(), true)) {
                    wordBuilder.append(lastReadWord).deleteCharAt(wordBuilder.length() - 1);
                }
                lastReadWord = wordBuilder.toString();
            }
            return lastReadWord;
        } finally {
            lastWordProcessed = true;
        }
    }

    public String[] readNextWords(int count) throws IOException {
        String[] words = new String[count];
        for (int i = 0; i < count; i++) {
            skipWhiteSpaces();
            words[i] = readWord();
            if (words[i] == null) return null;
        }
        return words;
    }

    public String readNextWord() throws IOException {
        String[] words = readNextWords(1);
        return words == null || words.length == 0? null : words[0];
    }

    public String peek(int chars) throws IOException {
        if (currentPeek != null && currentPeek.length() == chars) return currentPeek;
        reader.mark(chars);
        StringBuilder peekBuilder = new StringBuilder();
        int readValue;
        while ((readValue = reader.read()) != -1 && peekBuilder.length() < chars) {
            peekBuilder.append((char)readValue);
        }
        reader.reset();
        currentPeek = peekBuilder.toString();
        return currentPeek;
    }

    public void skipPeek() throws IOException {
        reader.skip(currentPeek.length());
        currentPeek = null;
    }

    public String getLastReadWord() {
        return lastReadWord;
    }

    public boolean setLastWordProcessed(boolean lastWordProcessed) {
        return (this.lastWordProcessed = lastWordProcessed);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private class EndOfLongWordDetector implements Predicate<Integer> {
        private boolean prevCharWasEndOfLongWord = false;
        @Override
        public boolean test(Integer readVal) {
            if (prevCharWasEndOfLongWord) return false;
            char readChar = (char)readVal.intValue();
            if (readChar == longWordEnd) prevCharWasEndOfLongWord = true;
            return true;
        }
    }
}

abstract class ParserImpl<TOut> implements Parser<TOut> {
    protected MultiParser<?> parentParser;
    public ParserImpl(MultiParser<?> parentParser) {
        this.parentParser = parentParser;
    }
    public MultiParser<?> getParentParser() {
        return parentParser;
    }
}

abstract class ParserFromWordImpl<TOut> extends ParserImpl<TOut> implements ParserFromWord<TOut> {
    protected TOut parsedValue;

    public ParserFromWordImpl(MultiParser<?> parentParser) {
        super(parentParser);
    }
    public TOut getParsedValue() {
        return parsedValue;
    }
    public boolean tryParse(WordReader reader) throws IOException {
        String word = reader.readNextWord();
        return word != null && tryParse(word);
    }
    public abstract boolean tryParse(String word);
}

abstract class RecursiveParserImpl<TOut> extends ParserImpl<TOut> {
    protected final Stack<TOut> parsedValues = new Stack<>();

    public RecursiveParserImpl(MultiParser<?> parentParser) {
        super(parentParser);
    }
    public TOut getParsedValue() {
        return parsedValues.pop();
    }
}

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

class ByteParser extends NumericTypeParserImpl<Byte> {
    public ByteParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Byte parseNumber(String word) throws NumberFormatException {
        return Byte.parseByte(word);
    }
}

class ShortParser extends NumericTypeParserImpl<Short> {
    public ShortParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Short parseNumber(String word) throws NumberFormatException {
        return Short.parseShort(word);
    }
}

class IntParser extends NumericTypeParserImpl<Integer> {
    public IntParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Integer parseNumber(String word) throws NumberFormatException {
        return Integer.parseInt(word);
    }
}

class LongParser extends NumericTypeParserImpl<Long> {
    public LongParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Long parseNumber(String word) throws NumberFormatException {
        return Long.parseLong(word);
    }
}

class FloatParser extends NumericTypeParserImpl<Float> {
    public FloatParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Float parseNumber(String word) throws NumberFormatException {
        return Float.parseFloat(word);
    }
}

class DoubleParser extends NumericTypeParserImpl<Double> {
    public DoubleParser(MultiParser<?> parentParser) {
        super(parentParser);
    }
    @Override
    protected Double parseNumber(String word) throws NumberFormatException {
        return Double.parseDouble(word);
    }
}

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

class TreeNode<K> {
    private Map<K, Object> attributes = new HashMap<>();
    public Map<K, Object> getAttributes() {
        return attributes;
    }
    public <T> void addAttribute(K attributeKey, T attributeValue) {
        attributes.put(attributeKey, attributeValue);
    }
    public void addAttributes(Map<? extends K, ?> attributes) {
        this.attributes.putAll(attributes);
    }
    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(Class<T> attributeValueClass, K attributeKey) {
        Object value = attributes.get(attributeKey);
        return (T)value;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}

class StringTreeNode extends TreeNode<String> {
}

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

class StringTreeParser extends TreeParser<String> {
    public StringTreeParser(MultiParser<?> parentParser, String treeBegin, String treeEnd) {
        super(parentParser, treeBegin, treeEnd);
    }
    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseAttributes(WordReader reader) throws IOException {
        try {
            return ((MultiParser<Object>)getRootParser()).parse(StringObjectHashMap.class, reader);
        } catch (ClassCastException e) {
            return null;
        }
    }
}

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

class StringObjectHashMap extends HashMap<String, Object> {
}

class StringObjectMapParser extends MapParser<String> {
    public StringObjectMapParser(MultiParser<?> parentParser, String stoppingWord) {
        super(parentParser, stoppingWord);
    }
}

class GMLParser extends ParserImpl<Map<String, Object>> {
    private Map<String, Object> parsedValue;
    private MultiParser<Object> parser = new MultiParser<>(null);
    public GMLParser(MultiParser<?> parentParser) {
        super(parentParser);
        parser.registerParser(StringTreeNode.class, new StringTreeParser(parser, "[", "]"), 0);
        parser.registerParser(Number.class, new NumberParser(parser), 1);
        parser.registerParser(Character.class, new CharParser(parser), 2);
        parser.registerParser(String.class, new StringParser(parser,'"'), 3);
        parser.registerParser(StringObjectHashMap.class, new StringObjectMapParser(parser,"]"));
        parser.freeze();
    }
    @Override
    public Map<String, Object> getParsedValue() {
        return parsedValue;
    }
    @Override
    public boolean tryParse(WordReader reader) throws IOException {
        return (parsedValue = parser.parse(StringObjectHashMap.class, reader)) != null;
    }
}

abstract class GraphExtractor<TVertex extends Vertex, TEdge extends Edge, G extends Graph<TVertex, TEdge>> implements TreeExtractor<String, G> {
    public G extract(TreeNode<String> graphNode) {
        List<TreeNode<String>> unextractedVertices = extractList(graphNode, "node");
        List<TreeNode<String>> unextractedEdges = extractList(graphNode, "edge");
        List<TVertex> vertices = new ArrayList<>();
        TVertex v;
        for (TreeNode<String> vertexTreeNode : unextractedVertices) {
            v = extractVertex(vertexTreeNode);
            if (v != null) vertices.add(v);
        }
        List<TEdge> edges = new ArrayList<>();
        TEdge e;
        for (TreeNode<String> edgeTreeNode : unextractedEdges) {
            e = extractEdge(edgeTreeNode);
            if (e != null) edges.add(e);
        }
        return createGraph(graphNode, vertices, edges);
    }
    protected List<TreeNode<String>> extractList(TreeNode<String> node, String atrKey) {
        Object uncastedValue = node.getAttributes().get(atrKey);
        List<TreeNode<String>> treeNodeList = null;
        if (uncastedValue != null) {
            if (uncastedValue instanceof TreeNode<?>) {
                treeNodeList = new ArrayList<>();
                treeNodeList.add((TreeNode<String>)uncastedValue);
            } else if (uncastedValue instanceof List<?>) {
                treeNodeList = (List<TreeNode<String>>) uncastedValue;
            }
        }
        return treeNodeList;
    }
    protected abstract G createGraph(TreeNode<String> graphNode, List<TVertex> vertices, List<TEdge> edges);
    protected abstract TVertex extractVertex(TreeNode<String> vertexNode);
    protected abstract TEdge extractEdge(TreeNode<String> edgeNode);
}

class SimpleGraphExtractor extends GraphExtractor<Vertex, Edge, SimpleGraph> {
    @Override
    protected SimpleGraph createGraph(TreeNode<String> graphNode, List<Vertex> vertices, List<Edge> edges) {
        return new SimpleGraph(vertices, edges);
    }
    @Override
    public Vertex extractVertex(TreeNode<String> vertexNode) {
        Number id = vertexNode.getAttributeValue(Number.class, "id");
        return new Vertex(id.intValue());
    }
    @Override
    public Edge extractEdge(TreeNode<String> edgeNode) {
        Number from = edgeNode.getAttributeValue(Number.class, "source");
        Number to = edgeNode.getAttributeValue(Number.class, "target");
        return new Edge(from.intValue(), to.intValue());
    }
}

class NetworkExtractor extends GraphExtractor<NetworkVertex, NetworkEdge, Network> {
    @Override
    protected Network createGraph(TreeNode<String> graphNode, List<NetworkVertex> networkVertices, List<NetworkEdge> networkEdges) {
        Number source = graphNode.getAttributeValue(Number.class, "source");
        Number sink = graphNode.getAttributeValue(Number.class, "sink");
        return new Network(networkVertices, networkEdges, source.intValue(), sink.intValue());
    }
    @Override
    protected NetworkVertex extractVertex(TreeNode<String> vertexNode) {
        Number id = vertexNode.getAttributeValue(Number.class, "id");
        return new NetworkVertex(id.intValue());
    }
    @Override
    protected NetworkEdge extractEdge(TreeNode<String> edgeNode) {
        Number from = edgeNode.getAttributeValue(Number.class, "source");
        Number to = edgeNode.getAttributeValue(Number.class, "target");
        Number capacity = edgeNode.getAttributeValue(Number.class, "capacity");
        return new NetworkEdge(from.intValue(), to.intValue(), capacity.doubleValue());
    }
}

class Vertex {
    protected int id;
    private List<Integer> neighbours = new ArrayList<>();

    public Vertex(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public List<Integer> getNeighbours() {
        return neighbours;
    }
    public void addNeighbour(int neighbour) {
        neighbours.add(neighbour);
    }

    @Override
    public String toString() {
        return "v" + id;
    }
}

class Edge {
    protected int from;
    protected int to;

    public Edge(int from, int to) {
        this.from = from;
        this.to = to;
    }
    public int getFrom() {
        return from;
    }
    public void setFrom(int from) {
        this.from = from;
    }
    public int getTo() {
        return to;
    }
    public void setTo(int to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return "(v" + from + ", v" + to + ")";
    }
}

class NetworkVertex extends Vertex {
    private int height = 0;
    private double excess;

    public NetworkVertex(int id) {
        super(id);
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public double getExcess() {
        return excess;
    }
    public void setExcess(double excess) {
        this.excess = excess;
    }

    @Override
    public String toString() {
        return "(" + super.toString() + "; h:" + height + "; e:" + excess + ")";
    }
}

class NetworkEdge extends Edge {
    private double capacity;
    private double flow = 0;

    public NetworkEdge(int from, int to, double capacity) {
        super(from, to);
        this.capacity = capacity;
    }

    public double getCapacity() {
        return capacity;
    }
    public double getFlow() {
        return flow;
    }
    public void setFlow(double flow) {
        this.flow = flow;
    }

    public double getResidualCapacity() {
        return capacity - flow;
    }

    @Override
    public String toString() {
        return "(v" + from + ", v" + to + ", c:" + capacity + ")";
    }
}

abstract class Graph<TVertex extends Vertex, TEdge extends Edge> {
    protected TVertex[] vertices;
    protected TEdge[][] edges;

    public Graph(List<TVertex> vertices, List<TEdge> edges) {
        this.vertices = createVertexArray(vertices.size());
        vertices.toArray(this.vertices);
        this.edges = createEdgeArray(vertices.size(), vertices.size());
        for (TEdge e : edges) {
            this.edges[e.from][e.to] = e;
            this.vertices[e.from].addNeighbour(e.to);
        }
    }
    public Graph(TVertex[] vertices, TEdge[][] edges) {
        this.vertices = vertices;
        this.edges = edges;
    }

    protected abstract TVertex[] createVertexArray(int length);

    protected abstract TEdge[][] createEdgeArray(int height, int width);

    public TEdge[][] getEdges() {
        return edges;
    }
    public TVertex[] getVertices() {
        return vertices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vertices: ").append(Arrays.toString(vertices)).append(", Edges: [");
        boolean firstEdge = true;
        for(Edge[] eRow : edges) {
            for(Edge e : eRow) {
                if (e != null) {
                    if (firstEdge) firstEdge = false;
                    else sb.append(", ");
                    sb.append(e);
                }
            }
        }
        sb.append(']');
        return sb.toString();
    }
}

class SimpleGraph extends Graph<Vertex, Edge> {
    public SimpleGraph(List<Vertex> vertices, List<Edge> edges) {
        super(vertices, edges);
    }
    public SimpleGraph(Vertex[] vertices, Edge[][] edges) {
        super(vertices, edges);
    }
    @Override
    protected Vertex[] createVertexArray(int length) {
        return new Vertex[length];
    }
    @Override
    protected Edge[][] createEdgeArray(int height, int width) {
        return new Edge[height][width];
    }

    @Override
    public String toString() {
        return "Graph [" + super.toString() + "]";
    }
}

class Network extends Graph<NetworkVertex, NetworkEdge> {
    protected int source;
    protected int sink;

    public Network(List<NetworkVertex> vertices, List<NetworkEdge> edges, int source, int sink) {
        super(vertices, edges);
        init(source, sink);
    }
    public Network(NetworkVertex[] vertices, NetworkEdge[][] edges, int source, int sink) {
        super(vertices, edges);
        init(source, sink);
    }
    @Override
    protected NetworkVertex[] createVertexArray(int length) {
        return new NetworkVertex[length];
    }
    @Override
    protected NetworkEdge[][] createEdgeArray(int height, int width) {
        return new NetworkEdge[height][width];
    }
    private void init(int source, int sink) {
        this.source = source;
        this.sink = sink;
    }
    public NetworkVertex getSource() {
        return vertices[source];
    }

    public void setSource(Vertex vertex) {
        this.source = vertex.getId();
    }

    public void setSource(int source) {
        this.source = source;
    }

    public NetworkVertex getSink() {
        return vertices[sink];
    }
    public void setSink(int sink) {
        this.sink = sink;
    }
    public void setTarget(Vertex target) {
        this.sink = target.getId();
    }

    @Override
    public String toString() {
        return "Network [ Source: " + getSource() + ", " + "Sink: " + getSink() + ", " + super.toString() + "]";
    }
}

class ResidualGraph extends Network {
    public ResidualGraph(Network network) {
        super(network.vertices, network.edges, network.getSource().getId(), network.getSink().getId());
        edges = Arrays.copyOf(network.edges, network.edges.length);
        vertices = Arrays.copyOf(network.vertices, network.vertices.length);
        vertices[source].setHeight(edges.length);
        for(int v : vertices[source].getNeighbours()) {
            edges[source][v].setFlow(edges[source][v].getCapacity());
            vertices[v].setExcess(edges[source][v].getCapacity());
            edges[v][source] = new NetworkEdge(v,source,0);
            edges[v][source].setFlow(-edges[source][v].getCapacity());
            vertices[v].addNeighbour(source);
        }
    }
    void push(int u, int v) {
        double change = Math.min(vertices[u].getExcess(), edges[u][v].getResidualCapacity());
        vertices[u].setExcess(vertices[u].getExcess() - change);
        vertices[v].setExcess(vertices[v].getExcess() + change);
        edges[u][v].setFlow(edges[u][v].getFlow() + change);
        if (edges[v][u] == null) {
            edges[v][u] = new NetworkEdge(v, u, 0);
            edges[v][u].setFlow(-edges[u][v].getCapacity());
            vertices[v].addNeighbour(u);
        }
        edges[v][u].setFlow(-edges[u][v].getFlow());
    }
    void relabel(int u) {
        int min = Integer.MAX_VALUE;
        for(int v : vertices[u].getNeighbours()) {
            if (edges[u][v].getResidualCapacity() > 0) min = Math.min(min, vertices[v].getHeight());
        }
        if (min < Integer.MAX_VALUE) vertices[u].setHeight(min + 1);
    }
    int getExcessedVertex() {
        for(NetworkVertex v : vertices) {
            if (v.getId() != source && v.getId() != sink && v.getExcess() > 0) return v.getId();
        }
        return -1;
    }
    void runGoldberg() {
        int excessedVertex;
        boolean pushed;
        while ((excessedVertex = getExcessedVertex()) != -1) {
            pushed = false;
            for (int v : vertices[excessedVertex].getNeighbours()) {
                if (vertices[excessedVertex].getHeight() > vertices[v].getHeight()
                        && edges[excessedVertex][v].getResidualCapacity() != 0) {
                    push(excessedVertex, v);
                    pushed = true;
                    break;
                }
            }
            if (!pushed) relabel(excessedVertex);
        }
    }
    public double getMaxFlow() {
        return vertices[sink].getExcess();
    }
}

class Goldberg implements GraphAlgorithm<Network, ResidualGraph> {
    @Override
    public ResidualGraph apply(Network network) {
        ResidualGraph residualGraph = new ResidualGraph(network);
        residualGraph.runGoldberg();
        return residualGraph;
    }
}

public class Main {
    public static void main(String[] args) {
        try (WordReader reader = new WordReader(args.length == 1? new FileReader(args[0]) : new InputStreamReader(System.in), '"', '"')) {
            GMLParser parser = new GMLParser(null);
            Map<String, Object> graphDescription = parser.parse(reader);
            TreeNode<String> graphNode = (TreeNode<String>) graphDescription.get("graph");

            SimpleGraphExtractor graphExtractor = new SimpleGraphExtractor();
            SimpleGraph graph = graphExtractor.extract(graphNode);
            System.out.println(graph);
            NetworkExtractor networkExtractor = new NetworkExtractor();
            Network network = networkExtractor.extract(graphNode);
            System.out.println(network);
            Goldberg goldberg = new Goldberg();
            System.out.println(goldberg.apply(network).getMaxFlow());
        } catch (FileNotFoundException e) {
            System.err.println("File wasn't found!");
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
    }
}
