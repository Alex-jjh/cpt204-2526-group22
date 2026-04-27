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
            System.out.printf("  %-11s avg %d ms%n",
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
        if (cost == Math.floor(cost) && !Double.isInfinite(cost)) {
            return Long.toString((long) cost);
        }
        return Double.toString(cost);
    }

    private Main() {}
}
