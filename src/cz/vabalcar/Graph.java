package cz.vabalcar;

import java.util.Arrays;
import java.util.List;

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
