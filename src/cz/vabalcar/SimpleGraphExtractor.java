package cz.vabalcar;

import java.util.List;

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
