package cz.vabalcar;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        try (WordReader reader = new WordReader(args.length == 1? new FileReader(args[0]) : new InputStreamReader(System.in), '"', '"')) {
            GMLParser parser = new GMLParser(null);
            Map<String, Object> graphDescription = parser.parse(reader);
            TreeNode<String> graphNode = (TreeNode<String>) graphDescription.get("graph");

            SimpleGraphExtractor graphExtractor = new SimpleGraphExtractor();
            SimpleGraph graph = graphExtractor.extract(graphNode);
            System.out.println(graph);
            NetworkExtractor networkExtractor = new NetworkExtractor();
            Network network = networkExtractor.extract(graphNode);
            System.out.println(network);
            Goldberg goldberg = new Goldberg();
            System.out.println(goldberg.apply(network).getMaxFlow());
        } catch (FileNotFoundException e) {
            System.err.println("File wasn't found!");
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
    }
}
