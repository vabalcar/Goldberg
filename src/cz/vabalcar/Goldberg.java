package cz.vabalcar;

class Goldberg implements GraphAlgorithm<Network, ResidualGraph> {
    @Override
    public ResidualGraph apply(Network network) {
        ResidualGraph residualGraph = new ResidualGraph(network);
        residualGraph.runGoldberg();
        return residualGraph;
    }
}
