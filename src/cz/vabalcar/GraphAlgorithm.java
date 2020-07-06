package cz.vabalcar;

import java.util.function.Function;

interface GraphAlgorithm<TGraph extends Graph, R> extends Function<TGraph, R> {
}
