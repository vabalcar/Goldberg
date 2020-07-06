package cz.vabalcar;

import java.util.ArrayList;
import java.util.List;

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
