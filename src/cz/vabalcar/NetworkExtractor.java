package cz.vabalcar;

import java.util.List;

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
