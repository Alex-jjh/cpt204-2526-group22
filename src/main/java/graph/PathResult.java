package graph;

import java.util.Collections;
import java.util.List;

/**
 * Result of a shortest-path query.
 *
 * <p>{@link #nodes()} is ordered from start to end; {@link #totalCost()} is
 * the sum of edge weights along that path. For an unreachable query,
 * {@link #isReachable()} is {@code false} and {@link #nodes()} is empty.
 *
 * <p>Self-loop queries (Case 1 in Task B) return a path of length 1 with
 * cost 0.
 */
public final class PathResult {

    private final String start;
    private final String end;
    private final List<String> nodes;
    private final double totalCost;
    private final boolean reachable;

    public PathResult(String start, String end, List<String> nodes,
                      double totalCost, boolean reachable) {
        this.start = start;
        this.end = end;
        this.nodes = List.copyOf(nodes);
        this.totalCost = totalCost;
        this.reachable = reachable;
    }

    public static PathResult unreachable(String start, String end) {
        return new PathResult(start, end, Collections.emptyList(), Double.POSITIVE_INFINITY, false);
    }

    public String start() { return start; }
    public String end() { return end; }
    public List<String> nodes() { return nodes; }
    public double totalCost() { return totalCost; }
    public boolean isReachable() { return reachable; }

    /** Human-readable path line, e.g. {@code "L0001 -> L0002 -> L0010"}. */
    public String formatPath() {
        return String.join(" -> ", nodes);
    }
}
