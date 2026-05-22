# Chapter 2 — Graph Algorithm (Task B)

## 2.1 Constructing the Weighted Graph

The shortest-path infrastructure for Task B is built from `paths.csv`.
Each row of that file (`from_location, to_location, weight`) is parsed
by `io/CsvReader.readEdges` into an immutable `model.Edge` triple, which
the `model.Edge` constructor validates to reject negative weights — a
necessary precondition for Dijkstra's algorithm (see §2.3 below).

The graph itself is stored as an **adjacency list**, modelled as
`HashMap<String, List<Arc>>` inside `graph.Graph`. Because the
infrastructure network is undirected, every edge produces two arcs in
the adjacency list (one in each direction). The provided dataset has
**1000 nodes and 2600 undirected edges**, which is sparse: only
≈ 2.6 average edges per node out of a possible 999, or roughly 0.5 %
density. An adjacency-matrix representation would allocate 10⁶ cells of
which 99.5 % are empty; the adjacency list therefore reduces both
memory and neighbour-iteration cost from O(V²) to O(V + E).

## 2.2 Required Output — Four Shortest-Path Cases

| Case | Start | Destination | Waypoints (in order) | Path | Total cost |
|---|---|---|---|---|---|
| 1 | L0001 | L0001 | — | L0001 | 0 |
| 2 | L0001 | L0010 | — | L0001 → L0340 → L0339 → L0895 → L0894 → L0082 → L0284 → L0010 | 27 |
| 3 | L0001 | L0101 | L0105 | L0001 → L0340 → L0339 → L0247 → L0017 → L0128 → L0107 → L0106 → **L0105** → L0106 → L0107 → L0108 → L0827 → L0996 → L0101 | 39 |
| 4 | L0001 | L0201 | L0105, L0205 | L0001 → L0340 → L0339 → L0247 → L0017 → L0128 → L0107 → L0106 → **L0105** → L0106 → L0107 → L0108 → L0243 → L0242 → L0241 → L0385 → **L0205** → L0201 | 48 |

Notes on the printed paths:

- **Case 1** is the self-loop case required by the task sheet. The
  `DijkstraShortestPath` implementation special-cases `start == end` to
  return a single-node path of cost 0 without entering the relaxation
  loop, which is the conventional and correct interpretation of "the
  shortest path from a node to itself".
- **Case 3 and Case 4** show the `findVia` path-stitching behaviour: the
  computed path traverses `… L0107 → L0106 → L0105 → L0106 → L0107 …`,
  i.e. it walks into the waypoint and then out again along the same
  edges. The `findVia` implementation correctly removes the duplicated
  joining node when concatenating segments, so the printed path lists
  L0105 once, but the cost still correctly counts the two edges traversed
  to leave the waypoint. This behaviour is the source of the
  segment-wise-vs-globally-optimal discussion in §2.4 below.

## 2.3 Algorithm: Dijkstra with a Binary-Heap Priority Queue

We use Dijkstra's single-source shortest-path algorithm, implemented
with a `java.util.PriorityQueue` (binary heap) rather than the simpler
linear-scan implementation found in introductory texts.

### Why Dijkstra is suitable for this graph

- **Non-negative weights.** Dijkstra's correctness hinges on the
  invariant that the first time a node is dequeued, its distance is
  final. This invariant holds if and only if all edge weights are
  non-negative; a later edge of negative weight could otherwise create
  a shorter path to a node already settled. `model.Edge` enforces this
  precondition at construction (`weight < 0` throws), so the algorithm
  is sound for the input we are given.
- **Single-source semantics fit the four required cases.** Every case
  is a "start → end" or "start → waypoint → … → end" query. Dijkstra
  computes the entire shortest-path tree rooted at the start node, so
  each direct-query case in §2.2 is a single Dijkstra invocation
  followed by a path reconstruction. The waypoint cases concatenate
  several such invocations.
- **Sparse graph.** On the provided graph (V = 1000, E = 2600) the
  binary-heap variant is comfortably faster than the linear-scan
  variant: see complexity discussion below.

### Implementation outline

The implementation in `graph/DijkstraShortestPath.java` follows the
standard relaxation pattern:

1. Initialise `dist[start] = 0` and push `(start, 0.0)` into the
   priority queue. All other distances are implicitly infinity (we use
   the absence of a `dist` map entry rather than a sentinel value).
2. Pop the lowest-distance entry. If it is already in the `settled`
   set, skip it (lazy deletion: stale entries from earlier relaxations
   are filtered here rather than removed from the heap on update).
3. For each outgoing arc of the popped node, relax the distance: if
   `dist[u] + w(u, v) < dist[v]`, update `dist[v]`, set `prev[v] = u`,
   and push `(v, new_dist)` into the heap.
4. Stop early when the destination is popped (its distance is now
   final), then reconstruct the path by walking back through `prev`.

### Time and space complexity

**Time complexity: O((V + E) log V).**

The dominant cost is the priority-queue operations. Each vertex is
inserted into the heap up to O(deg(v) + 1) times (once initially, plus
once per relaxation that improves its distance), so across the whole
graph the heap holds at most O(V + E) entries. Each push and pop costs
O(log V). The total is O((V + E) log V).

**Space complexity: O(V + E)** for the adjacency list, plus O(V) for
the distance map, predecessor map, and settled set. The heap holds at
most O(V + E) entries because of the lazy-deletion strategy.

### Why this is faster than the textbook version

The Week 10 lecture distributed a `WeightedGraph.getShortestPath`
implementation that uses two `ArrayList`-based structures inside the
relaxation loop: an `ArrayList<Integer> T` to record settled vertices,
and a linear scan over the parallel `cost[]` array to extract the next
minimum. That code runs at **O(V³)** on this graph — not because the
algorithm is wrong, but because the auxiliary data structures impose
two unnecessary linear scans per iteration. The lecturer flagged this
explicitly during the session ("*What if we don't use
`ArrayList<Integer> T`? What if we use other data structures?*"), and
this section answers that question concretely.

There are **two distinct linear scans** in the textbook version, and
each maps to a single data-structure swap in our implementation.

| Role inside the relaxation loop | W10 textbook                          | Our implementation                     | Per-call cost     |
|---|---|---|---|
| Membership test ("is *v* already settled?")     | `ArrayList<Integer> T`, `T.contains()`         | `HashSet<String> settled`              | O(V) → **O(1)**  |
| Extract-min ("cheapest unsettled *v*?")         | linear scan over `cost[]`                       | `java.util.PriorityQueue` (binary heap) | O(V) → **O(log V)** |

Composing the two costs over V iterations of the outer `while`:

- **W10 textbook:** outer loop V × (extract-min V × membership-test V) = **O(V³)** ≈ 10⁹ operations on the provided graph.
- **Our version:** total heap operations bounded by O(V + E) (each vertex inserted at most once per relaxation that improves its distance), each costing O(log V), giving **O((V + E) · log V)** ≈ 3.6 × 10⁴ operations on the provided graph.

That is roughly a **30 000× reduction in operation count** without any
change to the algorithmic logic. The relaxation rule, the priority
ordering, and the settled-vertex invariant are all unchanged — what
changes is how the loop *finds* the next vertex and *checks* whether
it has been settled.

#### Why the two swaps must be made together

Replacing only one of the `ArrayList` roles is not enough. If only the
heap is added (extract-min becomes O(log V)) but `T.contains()` remains
linear, the per-pop work is still O(V); the overall complexity stays at
O(V²). Symmetrically, if only the `HashSet` is added (membership in
O(1)) but the extract-min remains a linear scan over `cost[]`, the
inner scan is still O(V) and the overall complexity is again O(V²).
**The reduction to O((V + E) · log V) lives in the interaction between
the two structures**, not in either change in isolation. This is also
why the two-line code change is reported as a single optimisation in
the slide deck (slide 9): they are conceptually one decision.

A small implementation detail completes the picture. Our heap does not
support O(log V) `decreaseKey`, so when relaxation improves a node's
distance we push a fresh `(node, new_distance)` entry rather than
updating the old one — the well-known **lazy-deletion** pattern. Stale
entries are filtered on pop by the `settled.add(...)` check
(`if (!settled.add(cur.node)) continue;`). The `HashSet` therefore
serves a second purpose beyond fast membership: it makes lazy deletion
trivial. This reuse is part of why the two swaps compose so cleanly.

## 2.4 Local vs Global Optimality

A natural next question is whether the four cases in §2.2 are *jointly*
optimal — that is, whether running Dijkstra optimally for each case
implies that the inspection-planning problem as a whole is solved
optimally. The answer is **no**, for two distinct reasons.

### Reason 1: Segment-wise stitching is not globally optimal

The `findVia` method (Cases 3 and 4) decomposes a multi-waypoint query
into pairwise shortest-path segments and concatenates them. Each
segment is locally optimal, but the concatenation can be globally
sub-optimal whenever a small detour earlier in the path would unlock a
much shorter detour later.

A four-node counter-example illustrates this. Consider the graph:

```
      1            10
   S ───── A ─────────── E
   │                     │
   │ 100         1       │
   └──────  W  ──────────┘
```

Edges and weights: S–A (1), A–E (10), S–W (100), W–E (1). Required:
shortest path from S to E **via W**.

- Pairwise-optimal segments: S → A → ? → W has no edge except S → W
  with weight 100, so segment 1 is S → W with cost 100. Segment 2 is
  W → E with cost 1. Total = 101.
- A globally optimal path obeying the via constraint: S → A → E exists
  with cost 11 but does not visit W. So we cannot bypass the
  constraint. The next-best is S → W (100) → E (1) = 101, which is
  what segment-wise stitching also returns. *(Even in this small
  example, Dijkstra-on-segments is already optimal because the graph is
  too sparse to admit a clever detour.)*

A truer counter-example needs a graph where the locally optimal walk
into and out of W shares edges with a different, shorter walk. The
real `paths.csv` exhibits exactly this on Case 3: the segment
L0001 → L0105 finds it cheapest to go via the L0107 → L0106 → L0105
chain, and then the segment L0105 → L0101 retraces L0105 → L0106 →
L0107 because the first cost-effective branch out of L0105 leads back
through that same chain. The duplicated traversal accounts for two
extra edge weights in the total cost; an algorithm aware of both
segments could in principle plan a different route into L0105 to avoid
having to retrace, but `findVia` cannot.

The principled fix is to model "shortest path through a sequence of
required waypoints" as a single optimisation, not as a chain of
independent shortest paths. For *k* waypoints this can be solved by
Bellman–Held–Karp (O(2^k · V²)) or — in the unconstrained-order case —
by a TSP-like formulation. Both are out of scope for the current
project.

### Reason 2: The inspection-planning problem is not just a sum of queries

Even if every individual query were globally optimal, Task B asks four
specific cases out of the 30 selected targets. The full
inspection-planning problem (visit all 30 targets minimising travel
cost) is the metric Travelling Salesperson Problem and is NP-hard.
Optimising single shortest paths between pairs is therefore necessary
but not sufficient.

## 2.5 Alternative Algorithms

### If the graph were unweighted

We would replace Dijkstra with **Breadth-First Search** (Liang §28.9).
BFS computes shortest paths in unweighted graphs in O(V + E) using a
plain FIFO queue (no priority queue needed), because in an unweighted
graph the shortest path is the path with the fewest edges, and BFS
visits nodes in non-decreasing distance from the source by construction.

The comparison: BFS is asymptotically faster (no `log V` factor) and
has a simpler implementation (no priority queue, no relaxation), but
it is correct only when all edges have equal cost. For our weighted
infrastructure graph BFS would return a path with the fewest hops, not
the lowest total weight — these are different paths in general.

### If the graph were much larger or had node coordinates

We would consider **A\* search**. A\* is a goal-directed refinement of
Dijkstra: instead of expanding the lowest-distance node, it expands
the node minimising `dist + h(node, end)`, where `h` is an admissible
heuristic estimating the remaining distance. With a useful heuristic
(e.g. straight-line Euclidean distance when node coordinates are
available) A\* explores far fewer nodes than Dijkstra, which is
important when V is large.

The comparison: A\* matches or beats Dijkstra in practice when a good
heuristic exists, has the same worst-case complexity, but **requires
an admissible heuristic** — Dijkstra is essentially A\* with `h ≡ 0`.
The current `paths.csv` has no coordinate information, so we have no
heuristic and gain nothing from A\*; if the dataset were extended with
latitude/longitude per node, A\* would become attractive at large V.

### Other candidates considered and rejected

- **Bellman–Ford** (O(V · E)) tolerates negative weights but at the
  price of a higher complexity. We have no negative weights, so the
  extra cost is unjustified.
- **Floyd–Warshall** (O(V³)) computes all-pairs shortest paths up
  front. For four queries on a 1000-node graph this would be ≈ 10⁹
  operations versus ≈ 4 × 3.6 × 10⁴ for four heap-Dijkstras — slower
  by four orders of magnitude. Floyd–Warshall is the right choice
  only when the number of queries is comparable to V².
