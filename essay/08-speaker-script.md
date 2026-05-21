# Presentation Script — Group 22 (CPT 204, Spring 2026)

Companion script for `presentation.html`. Target total: **≤ 8 minutes** with
a ~30 s buffer. Both speakers must appear on camera (Task E rule).

## Timing layout

| Speaker  | Slides | Target | Topic |
|---|---|---|---|
| **Alex**     | 1 → 2   | ~1:30 | Intro + the three datasets |
| **Teammate** | 3 → 6   | ~2:00 | Task A — Sorting |
| **Alex**     | 7 → 11  | ~3:00 | Task B (Graph) + Task C (OOP) |
| **Teammate** | 12 → 13 | ~1:30 | Task D (EDI / reflection) + closing |

Total ≈ 8:00. Cushion ~30 s for natural pauses.

---

## ALEX — Part 1 · Intro & Datasets (slides 1–2, ~1:30)

**[Slide 1 — Cover · 0:00–0:10]**

> Hi everyone. I'm Alex, and this is my teammate, [Name]. We are Group 22,
> and this is our final coursework for CPT 204 — the *Urban Infrastructure
> Inspection System*.

**[Slide 2 — Project intro · 0:10–1:30]**

> The scenario is straightforward. A city is planning a large-scale
> infrastructure inspection. There are a thousand candidate locations that
> may need to be visited, and the inspection team's day is finite. So our
> program has to answer two questions: **whom to inspect first, and by
> what route.**
>
> The data comes in two parts. First, **three independent information
> sources** — each one gave us a list of the same one thousand locations,
> but with very different shapes. Let me walk through them.
>
> *[gesture to source card A]*
> **Dataset A** is already sorted by priority score, descending, with no
> ties. This is our *best case* — and as we'll see, it's exactly the
> regime where Bubble Sort's early-exit optimisation pays off.
>
> *[gesture to B]*
> **Dataset B** is the same locations, but the order is shuffled and the
> scores are randomly distributed. This is the *average case* — the
> territory where Quick Sort and Merge Sort earn their *n log n*.
>
> *[gesture to C]*
> **Dataset C** is the pathological one. Many locations share the same
> score — about twenty-five ties per score level. This stresses Quick
> Sort's pivots and also reveals our second sort key: when scores tie,
> we break the tie by `location_id` ascending. Without that secondary
> rule, the top-ten output from Dataset C wouldn't even be deterministic.
>
> The second part of the data is the road network — a thousand nodes
> connected by twenty-six hundred undirected weighted edges. That's where
> Task B will come in.
>
> So the overall workflow is: sort each dataset, take the top ten, end up
> with **thirty high-priority inspection targets**, then plan routes
> between them on the road network.
>
> I'll hand over to [Name] for Task A.

---

## TEAMMATE — Part 1 · Task A (slides 3–6, ~2:00)

**[Slide 3 — Task A intro · ~0:20]**

> Thanks Alex. Task A is about sorting. We implemented three algorithms
> from class — Bubble, Quick, and Merge — and ran each of them against
> all three datasets. They all sit behind a common `Sorter` interface, so
> the benchmark calls them polymorphically. We'll come back to that
> interface in Task C.

**[Slide 4 — Bubble animation · ~0:25]**

> Bubble Sort compares adjacent pairs and swaps when out of order. We
> added one optimisation from Liang's textbook: if a full pass goes by
> with no swap, the list is already sorted and we exit early. **That
> early-exit is the entire reason Bubble is fast on Dataset A** — already
> sorted means one pass and it's done.

**[Slide 5 — Quick animation · ~0:25]**

> Quick Sort uses *median-of-three* pivots — we look at the first,
> middle, and last element and pick the median as the pivot. This
> protects us from the worst case on already-sorted inputs, where naive
> Quick Sort would degrade to O(n²).

**[Slide 6 — Results · ~0:50]**

> Here are the timings, averaged over three runs after a warm-up pass.
>
> On **Dataset A**, Bubble's early-exit dominates — it finishes in well
> under a millisecond. Quick and Merge are slightly slower because they
> don't get to short-circuit on sorted input.
>
> On **Dataset B**, Quick Sort wins. Bubble is roughly an order of
> magnitude slower — the early-exit can't help when the data is genuinely
> shuffled.
>
> On **Dataset C** — the heavy-ties dataset — Merge Sort is the most
> consistent. Its O(n log n) holds regardless of input shape, while
> Quick's pivot choice can struggle with duplicate keys.
>
> Methodology-wise, we used `System.nanoTime`, one untimed warm-up to let
> the JIT compiler kick in, then three timed runs averaged on a fresh
> copy of the data each time. This is the closest we can get to a fair
> benchmark inside a student project.
>
> If we had to pick **one** algorithm for the final system, we'd pick
> Merge — not because it's the fastest on any single dataset, but because
> it's the most *consistent* across all three. Stability matters more
> than peak speed when the input shape isn't known in advance.
>
> Back to Alex for the graph.

---

## ALEX — Part 2 · Task B + Task C (slides 7–11, ~3:00)

**[Slide 7 — Dijkstra animation · ~0:50]**

> Thanks. So once Task A has selected our thirty inspection targets,
> Task B asks us to plan actual routes between them on the road network.
>
> The algorithm we use is **Dijkstra's** — single-source shortest paths
> on a non-negative weighted graph. The intuition is: starting from a
> node, we keep expanding outward, **always picking the cheapest
> unsettled node next** — like ripples spreading on water. Once a node is
> settled, its distance is final — and that's guaranteed by the fact
> that all weights are non-negative.
>
> The key implementation choice is the **priority queue**. We use Java's
> `PriorityQueue`, which is a binary heap — that gives us O(log V)
> extract-min. Without it, Dijkstra would be O(V²); with it, we're at
> O((V + E) log V). On our graph, that's roughly **twenty-eight times
> cheaper**.

**[Slide 8 — Four cases · ~0:50]**

> Here are the four required cases.
>
> **Case 1** is `L0001` to itself — a sanity check, total cost zero.
> **Case 2** is the first selected location of Dataset A to the tenth —
> cost twenty-seven.
> **Case 3** adds a waypoint constraint — the path must pass through the
> fifth selected location of Dataset B — cost thirty-nine.
> **Case 4** has *two ordered waypoints* — total cost forty-eight.
>
> For Cases 3 and 4, we split the query at each waypoint and concatenate
> the Dijkstra results.
>
> One thing I want to flag honestly: this is **locally optimal, not
> globally optimal**. If a waypoint happens to sit just off the natural
> shortest route, the algorithm forces a backtrack — and we observed
> exactly that pattern in Case 3. Solving this globally would require a
> *k-waypoint variant* of Dijkstra. We discuss that limitation in the
> report.

**[Slide 9 — Complexity & alternatives · ~0:25]**

> Why Dijkstra rather than Floyd-Warshall? Floyd computes *all-pairs*
> shortest paths in O(V³) — on a thousand-node graph that's a billion
> operations. Dijkstra called for our four cases is closer to a few
> hundred thousand operations. The point is, **algorithm choice is a
> function of the query pattern**, not absolute speed. If we had
> thousands of queries, Floyd's amortised cost would win.

**[Slide 10 — Task C OOP · ~0:35]**

> That brings me to Task C — the design.
>
> Our application has **five packages**: `model` for immutable data,
> `io` for CSV reading, `sort` and `graph` for the algorithms, and `app`
> as the driver. The whole system pivots on **two interfaces** — `Sorter`
> and `ShortestPathFinder` — which separate the *what* from the *how*.
>
> Adding a fourth sort algorithm, say Heap Sort, is one new class —
> `Main` doesn't change. Adding BFS as an alternative to Dijkstra — same
> story. That's the leverage interfaces give you.
>
> The four OOP principles each map to a concrete code site:
> **encapsulation** in our immutable `Candidate` class; **inheritance**
> through `implements Sorter`; **abstraction** through the two
> interfaces; and **polymorphism** through dynamic dispatch in the
> benchmark loop.

**[Slide 11 — Data structures · ~0:30]**

> A few words on data structures. We use `ArrayList` for the sortable
> input — random access, cache-friendly. `HashMap` for the adjacency
> list — sparse-graph efficient. `PriorityQueue` for the Dijkstra
> frontier — that's the binary heap I mentioned. And `HashSet` for the
> settled set — O(1) lookups.
>
> One subtle design choice worth noting: our graph is undirected, but
> each edge is stored *twice* — once as a directed `Arc` from each
> endpoint. We pay a bit of memory for that, but it means Dijkstra's
> inner loop never has to ask 'which end of this edge is me?' — it just
> reads `arc.to()`. **Space-for-time, where the time is the inner loop
> of every query.**
>
> I'll hand back to [Name] for the reflection.

---

## TEAMMATE — Part 2 · Task D + Closing (slides 12–13, ~1:30)

**[Slide 12 — Task D · ~1:00]**

> Thanks Alex. Task D is about how we ran the project — tools, AI
> assistance, and EDI.
>
> For **project management** we used **Trello** — five columns: Backlog,
> This Week, In Progress, Review, Done. Each card carries description,
> acceptance criteria, due date, owner, and a checklist — the same
> fields a real engineering team would use.
>
> For **AI assistance** we used ChatGPT, version *gpt-4o*, **only for
> non-substantive tasks**: drafting Trello card descriptions, proofreading
> English, suggesting acceptance-criteria phrasing. We caught it
> hallucinating once — it invented a `Comparator.thenReversing` method
> that doesn't exist — which is exactly why we treat AI output as a
> draft to verify, not a substitute for our own thinking.
>
> On **EDI** — equality, diversity, and inclusion — three concrete
> improvements we'd ship next:
> *text-to-speech* for visually-impaired inspectors using the route
> output;
> *internationalisation*, because location IDs are language-neutral but
> our console messages aren't yet;
> and a *colour-blind palette* for the path visualisation, replacing
> red-green coding with shape and dash-pattern.
>
> For **life-long learning**, the most transferable thing we built isn't
> the algorithms — it's the **benchmark methodology**: warm-up, nanoTime,
> repeated runs averaged. Any time someone asks "is this faster?", we
> now have a sober answer instead of a guess.

**[Slide 13 — Closing · ~0:30]**

> To summarise what we delivered:
> three sorting algorithms benchmarked with proper methodology;
> heap-based Dijkstra at O((V + E) log V), twenty-eight times cheaper
> than the textbook O(V²);
> five packages, four OOP principles, no library imports outside the
> CPT 204 allowlist;
> Trello board, three UML diagrams, contribution form, and this
> presentation.
>
> If we had another week, we'd add Heap Sort, a globally-optimal
> k-waypoint solver, JUnit tests for all four Task B cases, and the EDI
> improvements I mentioned.
>
> Thank you. We're happy to take any questions.

---

## Pre-recording checklist

- [ ] Replace `[Name]` placeholders with the teammate's actual name
      throughout.
- [ ] Confirm the four Task B costs (0 / 27 / 39 / 48) by re-running
      `Main` immediately before recording — these are the numbers we
      cite verbatim.
- [ ] Verify the three sorting timings by re-running `SortBenchmark`
      and let the teammate update the Slide 6 narration if needed.
- [ ] Time the full read-through twice. If over 8:00, drop either the
      Floyd-Warshall paragraph (Slide 9) or the Edge-vs-Arc paragraph
      (Slide 11) — both are bonus, not task-sheet-required.
- [ ] Both faces visible on camera throughout (Task E rule — failure
      is automatic 0).
- [ ] Native voice only; no English-translation TTS (Task E rule).
- [ ] MP4 ≤ 8:00 and ≤ 250 MB.
