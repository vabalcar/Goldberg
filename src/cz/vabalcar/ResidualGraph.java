package cz.vabalcar;

import java.util.Arrays;

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
