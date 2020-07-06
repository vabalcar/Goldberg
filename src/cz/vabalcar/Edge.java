package cz.vabalcar;

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
