package graph;

import model.Edge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Undirected weighted graph stored as an adjacency list.
 *
 * <p>Chosen over an adjacency matrix because the infrastructure graph is
 * sparse (≈ 2.6 edges per 1000 nodes in the provided {@code paths.csv}).
 * Adjacency lists give O(V + E) space and O(deg(v)) neighbour iteration,
 * both of which matter for Dijkstra on sparse graphs.
 */
public final class Graph {

    /** adjacency: nodeId -> list of outgoing (neighbour, weight) arcs. */
    private final Map<String, List<Arc>> adjacency = new HashMap<>();

    /** Adds a node with no edges if it is not already present. */
    public void addNode(String id) {
        adjacency.computeIfAbsent(id, k -> new ArrayList<>());
    }

    /**
     * Adds both directions of an undirected edge. Safe to call even if the
     * endpoints have not been added via {@link #addNode} first.
     */
    public void addEdge(Edge edge) {
        adjacency.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>())
                .add(new Arc(edge.getTo(), edge.getWeight()));
        adjacency.computeIfAbsent(edge.getTo(), k -> new ArrayList<>())
                .add(new Arc(edge.getFrom(), edge.getWeight()));
    }

    public boolean hasNode(String id) {
        return adjacency.containsKey(id);
    }

    public Collection<String> nodes() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    /** Neighbours of {@code node}; empty list if node is unknown. */
    public List<Arc> neighbours(String node) {
        List<Arc> arcs = adjacency.get(node);
        return arcs == null ? List.of() : Collections.unmodifiableList(arcs);
    }

    public int nodeCount() {
        return adjacency.size();
    }

    /** Factory: build a graph from a list of edges. */
    public static Graph fromEdges(List<Edge> edges) {
        Graph g = new Graph();
        for (Edge e : edges) {
            g.addEdge(e);
        }
        return g;
    }

    /** A directed arc inside the adjacency list. */
    public static final class Arc {
        private final String to;
        private final double weight;

        public Arc(String to, double weight) {
            this.to = to;
            this.weight = weight;
        }

        public String to() { return to; }
        public double weight() { return weight; }
    }
}
