package cz.vabalcar;

class NetworkVertex extends Vertex {
    private int height = 0;
    private double excess;

    public NetworkVertex(int id) {
        super(id);
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public double getExcess() {
        return excess;
    }
    public void setExcess(double excess) {
        this.excess = excess;
    }

    @Override
    public String toString() {
        return "(" + super.toString() + "; h:" + height + "; e:" + excess + ")";
    }
}
