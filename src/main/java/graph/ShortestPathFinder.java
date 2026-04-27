package graph;

import java.util.List;

/**
 * Abstraction for single-source / point-to-point shortest-path algorithms.
 *
 * <p>Having this interface makes the "alternative algorithms" discussion in
 * Task B concrete: {@link DijkstraShortestPath} is the default, but a BFS
 * implementation (for the unweighted case) could plug in here without
 * touching callers.
 */
public interface ShortestPathFinder {

    /**
     * Returns the shortest path from {@code start} to {@code end}, or
     * {@link PathResult#unreachable(String, String)} if no path exists.
     */
    PathResult find(Graph graph, String start, String end);

    /**
     * Convenience: shortest path visiting the given ordered waypoints,
     * built by concatenating point-to-point segments. Used for Task B's
     * Case 3 and Case 4 (where the path must visit waypoints in a specific
     * order).
     *
     * <p>Note for the report: segment-wise optimality does NOT imply global
     * optimality under arbitrary waypoint constraints; this is the "local vs
     * global optimum" point required by Task B's analysis.
     */
    default PathResult findVia(Graph graph, String start, List<String> waypoints, String end) {
        List<String> stops = new java.util.ArrayList<>();
        stops.add(start);
        stops.addAll(waypoints);
        stops.add(end);

        List<String> fullPath = new java.util.ArrayList<>();
        double totalCost = 0.0;
        for (int i = 0; i < stops.size() - 1; i++) {
            PathResult leg = find(graph, stops.get(i), stops.get(i + 1));
            if (!leg.isReachable()) {
                return PathResult.unreachable(start, end);
            }
            totalCost += leg.totalCost();
            if (fullPath.isEmpty()) {
                fullPath.addAll(leg.nodes());
            } else {
                // avoid duplicating the junction node where two legs meet
                fullPath.addAll(leg.nodes().subList(1, leg.nodes().size()));
            }
        }
        return new PathResult(start, end, fullPath, totalCost, true);
    }
}
