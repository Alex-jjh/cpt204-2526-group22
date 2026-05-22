# Task B — Shortest-Path Results

> Run on **2026-05-22**, V = 1 000, E = 2 600 (undirected).
> Top-10 selections from Task A:
> - **Dataset A** (sorted, no ties): `L0001 … L0010`
> - **Dataset B** (shuffled): `L0101 … L0110`
> - **Dataset C** (heavy ties, stable order): `L0201 … L0210`

---

## 1. Summary table — the four required cases

| # | Case | Start | Waypoints (ordered) | End | Hops | Total weight | Notes |
|---|------|-------|---------------------|-----|------|--------------|-------|
| i | Self-loop | `L0001` | — | `L0001` | 1 | **0** | `start == end` short-circuit, no relaxation. |
| ii | A → A | `L0001` | — | `L0010` | 8 | **27** | Single Dijkstra invocation. Both endpoints from Dataset A's top-10. |
| iii | A → B (via B) | `L0001` | `L0105` | `L0101` | 15 | **39** | **Two stitched Dijkstras.** Path retraces `L0107 › L0106 › L0105 › L0106 › L0107` — local-vs-global gap. |
| iv | A → B → C (via B & C) | `L0001` | `L0105`, `L0205` | `L0201` | 18 | **48** | **Three stitched Dijkstras.** Crosses all three datasets; same retrace pattern as Case iii on the first leg. |

---

## 2. Full computed paths

### Case i — self-loop (cost 0, 1 hop)

```
L0001
```

The `DijkstraShortestPath` implementation special-cases `start == end` and
returns a single-node path of cost 0 without entering the relaxation loop.

### Case ii — `L0001 → L0010` (cost 27, 8 hops)

```
L0001 → L0340 → L0339 → L0895 → L0894 → L0082 → L0284 → L0010
```

Direct shortest path within Dataset A's top-10. No waypoint stitching, no
backtracking — clean Dijkstra output.

### Case iii — `L0001 → via L0105 → L0101` (cost 39, 15 hops)

```
L0001 → L0340 → L0339 → L0247 → L0017 → L0128 → L0107 → L0106 → **L0105**
      → L0106 → L0107 → L0108 → L0827 → L0996 → L0101
```

> **⚠ Local-optimality artefact.** The path enters the waypoint via
> `L0107 → L0106 → L0105` and leaves it along the *same* edges
> `L0105 → L0106 → L0107`. `findVia` runs the second Dijkstra
> independently of the first, so the cheapest egress from `L0105`
> happens to retrace the cheapest ingress. Each leg is locally optimal;
> the concatenation is not globally optimal.

### Case iv — `L0001 → via L0105, L0205 → L0201` (cost 48, 18 hops)

```
L0001 → L0340 → L0339 → L0247 → L0017 → L0128 → L0107 → L0106 → **L0105**
      → L0106 → L0107 → L0108 → L0243 → L0242 → L0241 → L0385 → **L0205** → L0201
```

> **⚠ Same retrace pattern at `L0105`** as Case iii (first two legs are
> structurally identical). The third leg `L0205 → L0201` is clean.

---

## 3. Local-vs-global optimality — what the artefact actually is

`findVia` decomposes a *k*-waypoint query into *k+1* independent
shortest-path segments and concatenates them. Concretely for Case iii:

1. `Dijkstra(L0001, L0105)` returns the cheapest ingress chain
   `… L0107 → L0106 → L0105`.
2. `Dijkstra(L0105, L0101)` is run **fresh**, with no memory of step 1's
   geometry. The cheapest first edge out of `L0105` is `L0105 → L0106`,
   so the returned egress chain is `L0105 → L0106 → L0107 → L0108 → …`.
3. The two segments are concatenated. Both `L0106` and `L0107` are
   walked twice; the cost correctly counts both traversals.

**Why this is locally but not globally optimal:** an algorithm aware of
both segments could, in principle, plan a different *ingress* into
`L0105` so that the natural egress doesn't have to retrace. Closing this
gap is the *k*-waypoint shortest-path problem and admits exact solutions
via Bellman–Held–Karp (O(2^k · V²)). For k ∈ {1, 2}, this matters in
practice; for general k it is NP-hard (it reduces to TSP-with-order).

The current implementation is **correct** under the task brief
("compose Dijkstra calls at each waypoint"). The artefact is honestly
reported in §2.4 of the report and is the basis of the *future-work*
note on slide 14 (closing slide).

---

## 4. Sorting benchmark — supporting Task A timings (same run)

| Dataset | n | Bubble Sort | Quick Sort | Merge Sort |
|---------|---|------------:|-----------:|-----------:|
| **A** (sorted, no ties) | 1 000 | **0.096 ms** | 0.122 ms | 0.254 ms |
| **B** (shuffled) | 1 000 | 3.227 ms | 0.068 ms | **0.055 ms** |
| **C** (heavy ties) | 1 000 | 0.259 ms | **0.049 ms** | 0.061 ms |

> Methodology: averaged over 3 runs after one untimed warm-up,
> `System.nanoTime()`, fresh data copy per run, JVM 21, MacBook Pro
> M-series. **Bold** marks the fastest algorithm per dataset.

---

## 5. Reproduction

```bash
cd /Users/alexjiang/workspace/cpt204-2526-group22
javac -d build/classes -sourcepath src/main/java src/main/java/app/Main.java
java -cp build/classes app.Main datasets
```

The output above was produced verbatim by this command on 2026-05-22.
