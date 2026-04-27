package model;

import java.util.Objects;

/**
 * An undirected weighted edge between two locations.
 *
 * <p>Maps to a row of {@code paths.csv}: {@code from_location, to_location, weight}.
 * Although the graph is undirected, each edge is stored once with a fixed
 * {@code from} / {@code to} ordering as read from the file; the {@code Graph}
 * class is responsible for inserting both directions in the adjacency list.
 */
public final class Edge {

    private final String from;
    private final String to;
    private final double weight;

    public Edge(String from, String to, double weight) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
        if (weight < 0) {
            throw new IllegalArgumentException("weight must be non-negative: " + weight);
        }
        this.weight = weight;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return from + " -" + weight + "- " + to;
    }
}
