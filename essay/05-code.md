# Chapter 5 — Program Code

This chapter contains every Java source file as plain text, copied
from the working tree at the moment of submission. The session slides
are emphatic that Chapter 5 must contain text only, not screenshots —
this convention makes the source machine-readable for the marker and
preserves indentation.

The package layout follows the discussion in Chapter 3:

- `model.*` — immutable value classes (`Candidate`, `Edge`)
- `io.*` — file I/O (`CsvReader`)
- `sort.*` — sorting interface and three implementations, plus the
  benchmark harness
- `graph.*` — graph type, shortest-path interface, Dijkstra
  implementation, and result type
- `app.*` — the end-to-end driver (`Main`)

## src/main/java/app/Main.java

```java
package app;

import graph.DijkstraShortestPath;
import graph.Graph;
import graph.PathResult;
import graph.ShortestPathFinder;
import io.CsvReader;
import model.Candidate;
import model.Edge;
import sort.BubbleSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SortBenchmark;
import sort.Sorter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end driver for the Urban Infrastructure Inspection System.
 *
 * <p>Workflow:
 * <ol>
 *   <li>Read the three candidate CSVs.</li>
 *   <li>Benchmark Bubble / Quick / Merge sort on each; select top 10.</li>
 *   <li>Read {@code paths.csv} and build the weighted graph.</li>
 *   <li>Run the four shortest-path cases required by Task B.</li>
 * </ol>
 */
public final class Main {

    private static final int TOP_K = 10;
    private static final int BENCH_RUNS = 3;

    public static void main(String[] args) throws Exception {
        Path datasets = Paths.get(args.length > 0 ? args[0] : "datasets");

        List<Candidate> a = CsvReader.readCandidates(datasets.resolve("candidates_A.csv"));
        List<Candidate> b = CsvReader.readCandidates(datasets.resolve("candidates_B.csv"));
        List<Candidate> c = CsvReader.readCandidates(datasets.resolve("candidates_C.csv"));

        System.out.println("=== Task A: sorting benchmark ===");
        List<Candidate> topA = benchmarkAndSelect("Dataset A", a);
        List<Candidate> topB = benchmarkAndSelect("Dataset B", b);
        List<Candidate> topC = benchmarkAndSelect("Dataset C", c);

        List<Edge> edges = CsvReader.readEdges(datasets.resolve("paths.csv"));
        Graph graph = Graph.fromEdges(edges);
        System.out.printf("%nLoaded graph: %d nodes, %d undirected edges%n",
                graph.nodeCount(), edges.size());

        ShortestPathFinder finder = new DijkstraShortestPath();

        System.out.println();
        System.out.println("=== Task B: shortest-path cases ===");
        // Case 1: A[1] -> A[1]
        report("Case 1", finder.find(graph, topA.get(0).getLocationId(), topA.get(0).getLocationId()));
        // Case 2: A[1] -> A[10]
        report("Case 2", finder.find(graph, topA.get(0).getLocationId(), topA.get(9).getLocationId()));
        // Case 3: A[1] -> B[1] via B[5]
        report("Case 3", finder.findVia(
                graph,
                topA.get(0).getLocationId(),
                List.of(topB.get(4).getLocationId()),
                topB.get(0).getLocationId()));
        // Case 4: A[1] -> C[1] via B[5] then C[5]
        report("Case 4", finder.findVia(
                graph,
                topA.get(0).getLocationId(),
                List.of(topB.get(4).getLocationId(), topC.get(4).getLocationId()),
                topC.get(0).getLocationId()));
    }

    private static List<Candidate> benchmarkAndSelect(String label, List<Candidate> data) {
        SortBenchmark bench = new SortBenchmark(BENCH_RUNS);
        List<Sorter> sorters = List.of(new BubbleSort(), new QuickSort(), new MergeSort());

        System.out.printf("%n-- %s (n=%d) --%n", label, data.size());
        List<Candidate> authoritative = null;
        for (Sorter s : sorters) {
            SortBenchmark.Result r = bench.run(s, data, Candidate.RANKING);
            System.out.printf("  %-11s avg %.3f ms%n",
                    r.algorithm(), r.averageMillis());
            // all three algorithms must produce the same ranking; use the first
            if (authoritative == null) {
                authoritative = r.sorted();
            }
        }
        List<Candidate> top = new ArrayList<>(authoritative.subList(0, Math.min(TOP_K, authoritative.size())));
        System.out.printf("  Top %d: ", TOP_K);
        for (int i = 0; i < top.size(); i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(top.get(i).getLocationId());
        }
        System.out.println();
        return top;
    }

    private static void report(String label, PathResult result) {
        System.out.printf("%n[%s]%n", label);
        System.out.printf("  start: %s%n", result.start());
        System.out.printf("  end:   %s%n", result.end());
        if (result.isReachable()) {
            System.out.printf("  path:  %s%n", result.formatPath());
            System.out.printf("  cost:  %s%n", formatCost(result.totalCost()));
        } else {
            System.out.println("  path:  <unreachable>");
        }
    }

    private static String formatCost(double cost) {
        if (Double.isInfinite(cost)) return "inf";
        if (cost == Math.floor(cost)) return Long.toString((long) cost);
        return String.format("%.3f", cost);
    }

    private Main() {}
}
```

## src/main/java/io/CsvReader.java

```java
package io;

import model.Candidate;
import model.Edge;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV reader for this project.
 *
 * <p>Deliberately narrow in scope: the provided datasets have a header row,
 * plain comma separators, no quoted fields, and no escaping. A full RFC 4180
 * parser would be overkill and would also pull us outside the CPT204 library
 * allowlist.
 */
public final class CsvReader {

    private CsvReader() {
        // utility
    }

    /**
     * Reads a candidates file of the form {@code location_id,priority_score}.
     * The first line is treated as a header and skipped.
     */
    public static List<Candidate> readCandidates(Path file) throws IOException {
        List<Candidate> result = new ArrayList<>(1024);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("empty file: " + file);
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 2) {
                    throw new IOException("malformed candidate row at "
                            + file + ":" + lineNo + " -> " + line);
                }
                String id = parts[0].trim();
                int score;
                try {
                    score = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException nfe) {
                    throw new IOException("bad priority_score at "
                            + file + ":" + lineNo + " -> " + parts[1], nfe);
                }
                result.add(new Candidate(id, score));
            }
        }
        return result;
    }

    /**
     * Reads a paths file of the form {@code from_location,to_location,weight}.
     * The first line is treated as a header and skipped.
     */
    public static List<Edge> readEdges(Path file) throws IOException {
        List<Edge> result = new ArrayList<>(4096);
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("empty file: " + file);
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 3) {
                    throw new IOException("malformed edge row at "
                            + file + ":" + lineNo + " -> " + line);
                }
                String from = parts[0].trim();
                String to = parts[1].trim();
                double weight;
                try {
                    weight = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException nfe) {
                    throw new IOException("bad weight at "
                            + file + ":" + lineNo + " -> " + parts[2], nfe);
                }
                result.add(new Edge(from, to, weight));
            }
        }
        return result;
    }
}
```

## src/main/java/model/Candidate.java

```java
package model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Immutable record of a candidate inspection location.
 *
 * <p>Fields map directly to the columns of {@code candidates_*.csv}:
 * {@code location_id} and {@code priority_score}.
 */
public final class Candidate {

    private final String locationId;
    private final int priorityScore;

    public Candidate(String locationId, int priorityScore) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.priorityScore = priorityScore;
    }

    public String getLocationId() {
        return locationId;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    /**
     * Ranking rule required by Task A:
     * priorityScore descending, then locationId ascending.
     */
    public static final Comparator<Candidate> RANKING =
            Comparator.comparingInt(Candidate::getPriorityScore).reversed()
                    .thenComparing(Candidate::getLocationId);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate other)) return false;
        return priorityScore == other.priorityScore
                && locationId.equals(other.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, priorityScore);
    }

    @Override
    public String toString() {
        return locationId + "(" + priorityScore + ")";
    }
}
```

## src/main/java/model/Edge.java

```java
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
```

## src/main/java/sort/Sorter.java

```java
package sort;

import java.util.Comparator;
import java.util.List;

/**
 * Common abstraction for the three sorting algorithms required by Task A.
 *
 * <p>Accepting an arbitrary {@link Comparator} keeps the algorithms generic;
 * the project-level ranking rule is supplied by the caller (e.g.
 * {@code model.Candidate.RANKING}). This is the polymorphism example cited in
 * the Task C design discussion.
 *
 * <p>Implementations sort the given list in place.
 */
public interface Sorter {

    /** Sort {@code list} in place using {@code comparator}. */
    <T> void sort(List<T> list, Comparator<? super T> comparator);

    /** Human-readable name used in reports (e.g. {@code "Bubble Sort"}). */
    String name();
}
```

## src/main/java/sort/BubbleSort.java

```java
package sort;

import java.util.Comparator;
import java.util.List;

public final class BubbleSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        // robust programming, available to any Object list that is comparable
        int n = list.size();

        for (int k = 1; k < n; k++) {
            // initialize the swapped tag (boolean varaible)
            boolean swapped = false;

            for (int i = 0; i < n - k; i++) {
                // get the current and next
                T current = list.get(i);
                T next = list.get(i + 1);
                // compare current and next
                if (comparator.compare(current, next) > 0) {
                    list.set(i, next);
                    list.set(i + 1, current);
                    swapped = true;
                }
            }
            // an imporvement by Liang, if there doesn't exist a single swap in a loop,
            // then it is already sorted
            if (!swapped) {
                return;
            }
        }
    }

    @Override
    public String name() {
        return "Bubble Sort";
    }
}
```

## src/main/java/sort/MergeSort.java

```java
package sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Top-down merge sort. Stable and O(n log n) in all cases, at the cost of
 * O(n) auxiliary memory — useful talking point for Task A's runtime-vs-memory
 * discussion.
 */
public final class MergeSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        int n = list.size();
        if (n < 2) return;
        // work on a mutable array-backed buffer to keep O(n) extra, not O(n log n)
        List<T> aux = new ArrayList<>(list);
        mergesort(list, aux, 0, n - 1, comparator);
    }

    private <T> void mergesort(List<T> src, List<T> aux, int lo, int hi, Comparator<? super T> cmp) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergesort(src, aux, lo, mid, cmp);
        mergesort(src, aux, mid + 1, hi, cmp);
        merge(src, aux, lo, mid, hi, cmp);
    }

    private <T> void merge(List<T> src, List<T> aux, int lo, int mid, int hi, Comparator<? super T> cmp) {
        for (int k = lo; k <= hi; k++) {
            aux.set(k, src.get(k));
        }
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                src.set(k, aux.get(j++));
            } else if (j > hi) {
                src.set(k, aux.get(i++));
            } else if (cmp.compare(aux.get(i), aux.get(j)) <= 0) {
                src.set(k, aux.get(i++));
            } else {
                src.set(k, aux.get(j++));
            }
        }
    }

    @Override
    public String name() {
        return "Merge Sort";
    }
}
```

## src/main/java/sort/QuickSort.java

```java
package sort;

import java.util.Comparator;
import java.util.List;

/**
 * In-place quick sort using Lomuto partition with a median-of-three pivot.
 * The median-of-three choice reduces the chance of hitting O(n^2) on
 * already-sorted or reverse-sorted input — a point worth discussing in
 * Task A's analysis.
 */
public final class QuickSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        if (list.size() < 2) return;
        quicksort(list, 0, list.size() - 1, comparator);
    }

    private <T> void quicksort(List<T> list, int lo, int hi, Comparator<? super T> cmp) {
        if (lo >= hi) return;
        int p = partition(list, lo, hi, cmp);
        quicksort(list, lo, p - 1, cmp);
        quicksort(list, p + 1, hi, cmp);
    }

    private <T> int partition(List<T> list, int lo, int hi, Comparator<? super T> cmp) {
        int mid = lo + (hi - lo) / 2;
        medianOfThree(list, lo, mid, hi, cmp);
        T pivot = list.get(hi);
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, hi);
        return i + 1;
    }

    /** Arrange list[lo], list[mid], list[hi] so the median ends up at hi (used as pivot). */
    private <T> void medianOfThree(List<T> list, int lo, int mid, int hi, Comparator<? super T> cmp) {
        if (cmp.compare(list.get(lo), list.get(mid)) > 0) swap(list, lo, mid);
        if (cmp.compare(list.get(lo), list.get(hi)) > 0) swap(list, lo, hi);
        if (cmp.compare(list.get(mid), list.get(hi)) > 0) swap(list, mid, hi);
        // now list[mid] is the median; move it to hi to use as pivot
        swap(list, mid, hi);
    }

    private <T> void swap(List<T> list, int i, int j) {
        if (i == j) return;
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    @Override
    public String name() {
        return "Quick Sort";
    }
}
```

## src/main/java/sort/SortBenchmark.java

```java
package sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs a {@link Sorter} against a fresh copy of the input list a configurable
 * number of times and reports the average wall-clock runtime in milliseconds
 * (as a {@code double} so sub-millisecond differences are visible on small
 * inputs).
 *
 * <p>Methodology: one untimed warm-up run to mitigate JVM class-loading /
 * JIT-compilation noise, then {@code runs} timed runs measured with
 * {@link System#nanoTime()} (monotonic, sub-millisecond precision). Each timed
 * run gets its own copy so in-place sorters do not get a pre-sorted input on
 * subsequent runs.
 */
public final class SortBenchmark {

    /** How many times to repeat each sort (for averaging). */
    private final int runs;

    /**
     * Constructor.
     *
     * @param runs number of repetitions (must be >= 1)
     */
    public SortBenchmark(int runs) {
        if (runs < 1) throw new IllegalArgumentException("runs must be >= 1");
        this.runs = runs;
    }

    /**
     * Benchmark a single sorter on the given data.
     *
     * @param sorter the sorting algorithm to benchmark
     * @param data   the original unsorted list (never modified)
     * @param cmp    the comparator defining the sort order
     * @return a {@link Result} containing the algorithm name, average time,
     *         and the sorted list from the last run
     */
    public <T> Result run(Sorter sorter, List<T> data, Comparator<? super T> cmp) {
        // Warm-up: one untimed run so the timed runs are not skewed by JVM
        // class-loading and JIT compilation of the sort method. The result is
        // discarded.
        sorter.sort(new ArrayList<>(data), cmp);

        long totalNanos = 0;
        List<T> lastSorted = null;

        for (int i = 0; i < runs; i++) {
            // 1. Copy the original data so each run starts from the same unsorted state
            List<T> copy = new ArrayList<>(data);

            // 2. Record start time (nanoTime: monotonic, sub-millisecond precision)
            long startNanos = System.nanoTime();

            // 3. Sort
            sorter.sort(copy, cmp);

            // 4. Record end time
            long endNanos = System.nanoTime();

            // 5. Accumulate elapsed time
            totalNanos += (endNanos - startNanos);

            // Keep the last sorted result for top-K selection
            lastSorted = copy;
        }

        double averageMillis = (totalNanos / (double) runs) / 1_000_000.0;
        return new Result(sorter.name(), averageMillis, lastSorted);
    }

    /**
     * Holds the benchmark result for one algorithm on one dataset.
     */
    public static final class Result {

        private final String algorithm;
        private final double averageMillis;
        private final List<?> sorted;

        public Result(String algorithm, double averageMillis, List<?> sorted) {
            this.algorithm = algorithm;
            this.averageMillis = averageMillis;
            this.sorted = sorted;
        }

        /** Name of the sorting algorithm (e.g. "Bubble Sort"). */
        public String algorithm() { return algorithm; }

        /** Average runtime in milliseconds across all runs (sub-ms precision). */
        public double averageMillis() { return averageMillis; }

        /** The sorted list from the last run (used for top-K selection). */
        @SuppressWarnings("unchecked")
        public <T> List<T> sorted() { return (List<T>) sorted; }
    }
}
```

## src/main/java/graph/Graph.java

```java
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
```

## src/main/java/graph/PathResult.java

```java
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
```

## src/main/java/graph/ShortestPathFinder.java

```java
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
```

## src/main/java/graph/DijkstraShortestPath.java

```java
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
```
