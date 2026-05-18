# Chapter 3 — Design of the Overall Application (Task C)

The Urban Infrastructure Inspection System integrates the Task A
sorting workflow and the Task B shortest-path workflow into a single
Java application. This chapter walks through three layers of the
design: (1) the data structures that represent the input data and
intermediate results; (2) the classes and their responsibilities; and
(3) the OOP principles applied throughout.

## 3.1 Data Structures

### Candidate datasets — `ArrayList<Candidate>`

Each of the three input CSVs is read into an `ArrayList<Candidate>`.
`ArrayList` is the natural choice for sortable input data:

- **Random access in O(1)** is exploited by the in-place sorters (Quick
  Sort and Bubble Sort) when swapping by index.
- **Sequential iteration is cache-friendly** because the underlying
  storage is a contiguous array.
- **Iteration order is deterministic**, which matters for the top-K
  selection in `Main`: after sorting, the first 10 elements are the
  top 10 priorities.

`Candidate` itself is an immutable value class
(`final` fields, no setters), and the project-level ranking rule lives
on `Candidate` as a static `Comparator`:

```java
public static final Comparator<Candidate> RANKING =
    Comparator.comparingInt(Candidate::getPriorityScore).reversed()
              .thenComparing(Candidate::getLocationId);
```

This places the sort order *with the data* rather than scattered
through the sorters, so swapping the ranking rule (say, ascending
priority instead of descending) requires changing one place.

### Weighted graph — adjacency list `HashMap<String, List<Arc>>`

`graph.Graph` represents the undirected weighted network using an
adjacency list:

```java
private final Map<String, List<Arc>> adjacency = new HashMap<>();
```

The trade-off versus an adjacency matrix is decisive on this dataset
(see §2.1 of Chapter 2). With V = 1000 and E = 2600 the graph is sparse
(≈ 0.5 % density); the matrix would waste 99.5 % of its cells, while
the list uses O(V + E) cells. The list also gives O(deg(v)) neighbour
iteration, which is exactly what Dijkstra's relaxation loop needs.

`HashMap` is preferred over `TreeMap` here because we never need keys
in sorted order — we look up by exact location id, an O(1)-average
operation on `HashMap`. `ArrayList<Arc>` for each node's neighbour list
keeps neighbour iteration contiguous in memory.

The `Arc` inner class (the directed `(neighbour, weight)` pair stored
per node) is deliberately separate from the `Edge` class (the
undirected `(from, to, weight)` triple read from CSV). `Edge` reflects
the file format; `Arc` reflects the in-memory layout. Each `Edge` is
inserted as two `Arc`s (one in each direction) in `Graph.addEdge`,
which is the conventional encoding of an undirected graph as a
directed adjacency list.

### Priority queue — binary heap via `java.util.PriorityQueue`

Dijkstra's open set is stored in a `java.util.PriorityQueue<Entry>`,
where `Entry` is the `(node, current best distance)` pair. The standard
library's `PriorityQueue` is a binary heap, giving O(log n) push and
pop. As argued in §2.3, switching from a linear-scan open set to a
binary heap is the single change that drops Dijkstra's complexity from
O(V²) to O((V + E) log V) on this graph.

We use the *lazy-deletion* pattern: when a node's distance improves we
push a new entry rather than locating and updating the old entry
(`PriorityQueue` does not support O(log n) decrease-key). On pop we
skip any entry whose node has already been settled. This is simpler
than implementing an indexed heap and asymptotically equivalent.

### Result types — immutable records

Both algorithm pipelines return immutable result objects:

- `SortBenchmark.Result` carries the algorithm name, the average
  runtime in milliseconds, and the sorted list from the last run.
- `graph.PathResult` carries the start and end node ids, the ordered
  list of nodes along the path, the total cost, and a reachability
  flag.

Returning structured results (rather than printing inside the
algorithms) lets `Main` format the output once, and lets a future test
harness or alternative front-end consume the same data.

## 3.2 Classes and Responsibilities

The class diagram in `image/detail-class.png` shows the full set of
classes; `image/overall-arch.png` shows the high-level partition into
the sorting pipeline and the shortest-path pipeline.
`image/class-inheritence.png` shows where the standard-library types
we depend on (`ArrayList`, `HashMap`, `PriorityQueue`) sit in Java's
Collection / Map hierarchy.

### Layer 1 — immutable data carriers

These classes hold data and have no algorithmic behaviour of their own.

| Class | Responsibility |
|---|---|
| `model.Candidate` | A single candidate location: `(locationId, priorityScore)`. Hosts the static `RANKING` comparator. |
| `model.Edge` | A single edge as read from `paths.csv`: `(from, to, weight)`. Validates non-negative weight at construction. |
| `graph.PathResult` | The result of one shortest-path query: start, end, ordered node list, total cost, reachability flag. |
| `sort.SortBenchmark.Result` | The result of one benchmark run: algorithm name, average milliseconds, sorted list. |

All four are `final` classes with `final` private fields, exposed only
via getters, and constructed once. They cannot be mutated after
creation.

### Layer 2 — service / utility

| Class | Responsibility |
|---|---|
| `io.CsvReader` | Reads `candidates_*.csv` and `paths.csv` into `List<Candidate>` and `List<Edge>` respectively. Utility class — private constructor, only static methods. |
| `graph.Graph` | The adjacency-list representation of the undirected weighted graph. Hosts `addEdge`, `addNode`, `neighbours`, and the `fromEdges` factory. The inner class `Graph.Arc` represents one outgoing arc. |
| `sort.SortBenchmark` | Runs a `Sorter` for one warm-up run and *k* timed runs on a fresh copy of the input each time, returns the average runtime in `Result`. |

### Layer 3 — algorithm abstractions and implementations

| Type | Responsibility |
|---|---|
| `sort.Sorter` | Interface. Single method `sort(List<T>, Comparator<? super T>)` plus a `name()` method for reporting. |
| `sort.BubbleSort`, `sort.QuickSort`, `sort.MergeSort` | Three implementations of `Sorter`, each in its own file. |
| `graph.ShortestPathFinder` | Interface. Single abstract method `find(graph, start, end)` plus a `default` method `findVia(graph, start, waypoints, end)` that concatenates point-to-point segments. |
| `graph.DijkstraShortestPath` | Implementation of `ShortestPathFinder` using a binary-heap priority queue. The inner class `DijkstraShortestPath.Entry` is the heap element. |

### Layer 4 — driver

| Class | Responsibility |
|---|---|
| `app.Main` | Wires all of the above together. Reads the three candidate CSVs, runs the benchmark for each algorithm on each dataset, prints the timing table and top-10s, builds the graph, runs each of the four required Task B cases, prints the result. |

`Main` is *only* an orchestrator: every algorithm is reached through
its interface, never through the concrete class. This matters for the
polymorphism discussion in §3.3.

### How the classes collaborate

The end-to-end flow is:

1. `Main` calls `CsvReader.readCandidates(...)` three times, once per
   dataset, producing three `List<Candidate>`.
2. For each list, `Main` constructs a `SortBenchmark` and a list of
   three `Sorter` instances. It iterates over the list of sorters
   polymorphically, asking the benchmark to time each one. The first
   `Result` produced is the source of truth for the top-10 selection
   (all three sorters agree on the order because `Candidate.RANKING`
   is total).
3. `Main` calls `CsvReader.readEdges(...)` once and then
   `Graph.fromEdges(...)` to build the graph.
4. `Main` constructs a single `DijkstraShortestPath` and stores it
   under the `ShortestPathFinder` interface type. For each of the four
   required cases it calls either `find` (Cases 1 and 2) or `findVia`
   (Cases 3 and 4) and prints the resulting `PathResult`.

`Main` therefore depends on the interfaces, not on concrete
implementations. Adding a new sorter (say, Heap Sort) or a new
shortest-path algorithm (say, A\*) is a one-line change in `Main`'s
factory list — no other code changes.

## 3.3 Object-Oriented Principles

The four core OOP principles — encapsulation, inheritance, abstraction,
polymorphism — all appear in the design, each with concrete code
evidence.

### Encapsulation

Every value class uses `private final` fields exposed only through
getters. The strongest example is `model.Candidate`:

```java
public final class Candidate {
    private final String locationId;
    private final int priorityScore;
    public Candidate(String locationId, int priorityScore) { ... }
    public String getLocationId() { return locationId; }
    public int getPriorityScore() { return priorityScore; }
}
```

This delivers two properties:

1. **Immutability.** Once a `Candidate` is constructed, its fields can
   never change. Algorithms that pass `Candidate` references around
   cannot accidentally mutate the original input.
2. **Invariant enforcement at construction.** `model.Edge` rejects
   negative weights in its constructor; once an `Edge` exists, the
   rest of the program can rely on the non-negative-weight invariant
   without re-checking. This is precisely the precondition Dijkstra
   needs (§2.3).

`graph.Graph` reinforces this further: the `neighbours(node)` method
returns `Collections.unmodifiableList(...)` over the internal arc
list, so callers cannot mutate the adjacency list by side-effect.

### Inheritance

Three relationships use inheritance to share contract without sharing
state:

- `BubbleSort`, `QuickSort`, `MergeSort` each `implement Sorter`.
- `DijkstraShortestPath` `implement`s `ShortestPathFinder`.
- A handful of classes implicitly inherit standard-library contracts
  (e.g. our `Entry` class implements `Comparable<Entry>` so the binary
  heap can order it).

We deliberately use **interface inheritance** rather than abstract-base-
class inheritance. The three sorters share no state and almost no code
(each algorithm has a fundamentally different shape: nested loops for
Bubble, divide-and-conquer recursion for Merge, in-place partitioning
for Quick). An abstract `AbstractSorter` would not have anything
non-trivial to put in a shared base. Java interfaces also support
**default methods**, which we use on `ShortestPathFinder.findVia` to
provide a free implementation of multi-waypoint stitching for any
class that implements `find`. This is the same pattern that lets
`Iterator` define `forEachRemaining` once and have every iterator
inherit it.

### Abstraction

Two abstractions sit at the heart of the design:

- `Sorter` abstracts "an algorithm that sorts a list given a
  comparator". The benchmark and `Main` know nothing of how sorting
  is actually performed.
- `ShortestPathFinder` abstracts "an algorithm that finds the shortest
  path on a weighted graph". The driver knows nothing of whether the
  algorithm is Dijkstra, A\*, BFS, or Bellman–Ford.

The benefit is concrete and testable: §2.5 considers BFS and A\* as
alternatives for hypothetical inputs. If we ever needed to plug in
either, the only change required is implementing
`ShortestPathFinder.find` on a new class and instantiating it in
`Main`. The graph itself, the path-result type, the benchmark, and the
driver workflow are all untouched.

### Polymorphism

Polymorphism is what makes the abstractions pay off at runtime. The
clearest example is the benchmark loop in `app.Main`:

```java
List<Sorter> sorters = List.of(new BubbleSort(), new QuickSort(), new MergeSort());
for (Sorter s : sorters) {
    SortBenchmark.Result r = bench.run(s, data, Candidate.RANKING);
    ...
}
```

`Main` iterates over a `List<Sorter>` and calls `s.sort(...)` and
`s.name()` on each element. At compile time the Java compiler knows
only that `s` is a `Sorter`; at runtime the JVM dispatches to the
concrete `BubbleSort.sort`, `QuickSort.sort`, or `MergeSort.sort` based
on the actual object type. This is *dynamic dispatch*, the textbook
mechanism that lets one piece of client code drive an unbounded family
of algorithm implementations.

The same pattern recurs in the shortest-path side of the system, where
`Main` holds the algorithm in a variable of the interface type:

```java
ShortestPathFinder finder = new DijkstraShortestPath();
PathResult r = finder.find(graph, start, end);
```

If a future requirement called for A\*, only the right-hand side of the
assignment would change.

### Why these principles matter to this project

Three concrete pay-offs accrue from following these principles
throughout:

1. **Adding an algorithm is a one-line change.** The Sorter and
   ShortestPathFinder interfaces decouple the *what* from the *how*.
2. **Defending design choices is easier in the report.** Each principle
   maps to a specific code site, not to a vague "OO style".
3. **The test surface is small.** Because data carriers are immutable
   and algorithms communicate through interfaces, unit tests can
   substitute a fake `Sorter` or a fake `Graph` without infrastructure.
   We have not invested in JUnit (which would push us outside the
   CPT204 library allowlist), but the door is open.
