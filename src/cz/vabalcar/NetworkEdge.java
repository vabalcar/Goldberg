package cz.vabalcar;

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
