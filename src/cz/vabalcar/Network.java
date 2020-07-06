package cz.vabalcar;

import java.util.List;

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
