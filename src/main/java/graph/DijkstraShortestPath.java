package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Dijkstra's algorithm with a binary-heap priority queue.
 *
 * <p>Time: O((V + E) log V). Space: O(V) for the distance / predecessor maps
 * plus O(V) for the priority queue. Safe because {@link model.Edge} enforces
 * non-negative weights on construction.
 */
public final class DijkstraShortestPath implements ShortestPathFinder {

    @Override
    public PathResult find(Graph graph, String start, String end) {
        if (!graph.hasNode(start) || !graph.hasNode(end)) {
            return PathResult.unreachable(start, end);
        }
        // self-loop: required by Task B Case 1
        if (start.equals(end)) {
            return new PathResult(start, end, List.of(start), 0.0, true);
        }

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Set<String> settled = new HashSet<>();

        PriorityQueue<Entry> pq = new PriorityQueue<>();
        dist.put(start, 0.0);
        pq.add(new Entry(start, 0.0));

        while (!pq.isEmpty()) {
            Entry cur = pq.poll();
            if (!settled.add(cur.node)) continue;
            if (cur.node.equals(end)) break;

            for (Graph.Arc arc : graph.neighbours(cur.node)) {
                if (settled.contains(arc.to())) continue;
                double nd = cur.dist + arc.weight();
                Double known = dist.get(arc.to());
                if (known == null || nd < known) {
                    dist.put(arc.to(), nd);
                    prev.put(arc.to(), cur.node);
                    pq.add(new Entry(arc.to(), nd));
                }
            }
        }

        if (!dist.containsKey(end)) {
            return PathResult.unreachable(start, end);
        }

        List<String> path = new ArrayList<>();
        for (String node = end; node != null; node = prev.get(node)) {
            path.add(node);
        }
        Collections.reverse(path);
        return new PathResult(start, end, path, dist.get(end), true);
    }

    /** Priority-queue record: (node, current best distance). */
    private static final class Entry implements Comparable<Entry> {
        final String node;
        final double dist;

        Entry(String node, double dist) {
            this.node = node;
            this.dist = dist;
        }

        @Override
        public int compareTo(Entry o) {
            return Double.compare(this.dist, o.dist);
        }
    }
}
