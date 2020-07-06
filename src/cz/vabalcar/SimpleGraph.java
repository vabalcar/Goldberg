package cz.vabalcar;

import java.util.List;

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
