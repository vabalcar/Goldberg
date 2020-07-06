package cz.vabalcar;

import java.util.ArrayList;
import java.util.List;

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
