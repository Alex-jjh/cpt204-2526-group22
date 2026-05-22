# Presentation Script — Group 22 (CPT 204, Spring 2026)

Companion script for `presentation.html`. Target total: **≤ 8 minutes** with
a ~30 s buffer. Both speakers must appear on camera (Task E rule).

## Timing layout

| Speaker  | Slides | Target | Topic |
|---|---|---|---|
| **Alex**     | 1 → 2   | ~1:30 | Intro + the three datasets |
| **Teammate** | 3 → 6   | ~2:00 | Task A — Sorting |
| **Alex**     | 7 → 13  | ~3:00 | Task B (Graph, incl. composition + W10 follow-up) + Task C (OOP) |
| **Teammate** | 14 → 15 | ~1:30 | Task D (EDI / reflection) + closing |

Total ≈ 8:00. Cushion ~30 s for natural pauses.

**Slide 8 — the thirty selected targets / case composition** bridges
Task A and Task B by showing all 30 top-ten picks (10 + 10 + 10) and how
the four required cases compose endpoints and waypoints from those
thirty. **Slide 10 — the W10 follow-up** is the bonus slide that
addresses the lecturer's open question from Week 10 (*"What if we don't
use `ArrayList<Integer> T`? What if we use other data structures?"*).
Both are deliberate value-adds — marking-friendly answers to questions
raised in class. Keep slide 10 tight (~0:35) so it doesn't push the run
over budget.

---

## ALEX — Part 1 · Intro & Datasets (slides 1–2, ~1:30)

**[Slide 1 — Cover · 0:00–0:08]**

> Hi everyone. I'm Alex, and this is my teammate Wenlu. We are Group 22,
> and this is our final coursework for CPT 204 — the *Urban Infrastructure
> Inspection System*.

**[Slide 2 — Project intro · 0:08–1:30]**

> The scenario: a city is planning a large-scale infrastructure
> inspection across **one thousand candidate locations**, **L0001
> through L1000**. These same locations are also the nodes of a road
> network, connected by **2,600 weighted, undirected edges** — each
> weight a travel cost between two locations.
>
> Our input arrives as **three independent priority files** — A, B,
> and C. All three list the same thousand locations, but they differ
> in two ways.
>
> **First, the scores themselves disagree** — each file comes from a
> different source, so the priority File A assigns to L0001 isn't what
> File B or C assigns.
>
> **Second, the row ordering inside each file is structurally
> different:**
>
> - **File A** — already sorted by priority, **descending, no ties** —
>   the *best case*.
> - **File B** — **shuffled** — the *average case*.
> - **File C** — heavy with **ties** — the *stress case* for sorting.
>
> These three shapes are exactly what stress sorting algorithms
> differently — and that sets up Task A.
>
> Our system has to answer two questions.
>
> **First — whom do we inspect first?** We sort each file independently
> and take the top ten, giving us **thirty high-priority targets** in
> total.
>
> **Second — by what route?** We run shortest-path queries between
> those targets on the road network. That's Task B.
>
> I'll hand over to Wenlu for Task A.

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

## ALEX — Part 2 · Task B + Task C (slides 7–13, ~3:00)

**[Slide 7 — Dijkstra animation · ~0:40]**

> Thanks. So once Task A has selected our thirty inspection targets,
> Task B asks us to plan actual routes between them on the road network.
>
> The algorithm we use is **Dijkstra's** — single-source shortest paths
> on a non-negative weighted graph. The intuition is simple: starting
> from a node, we keep expanding outward, **always picking the cheapest
> unsettled node next** — like ripples spreading on water. Once a node
> is settled, its distance is final — and that's guaranteed by the fact
> that all weights are non-negative.

**[Slide 8 — Thirty selected targets · case composition · ~0:35]**

> Before I show the routes, here's how the four cases get *built*. Task A
> produced **three top-tens** — thirty targets in total, shown on the
> left. The four required cases each pick endpoints, and waypoints, from
> those thirty.
>
> **Case 1 and 2** stay inside Dataset A — self-loop, then L0001 to L0010.
> **Case 3** crosses A to B, *via* L0105.
> **Case 4** crosses A to B to C, two ordered waypoints — L0105 then
> L0205, ending at L0201.
>
> For Cases 3 and 4 we split each query at every waypoint and concatenate
> the Dijkstra results. That's locally optimal, not globally optimal —
> the next slide shows the receipt.

**[Slide 9 — The four routes & the backtrack · ~0:25]**

> Here are the actual costs: zero, twenty-seven, thirty-nine, forty-eight.
>
> Look at Case 3. The computed path retraces itself —
> `L0107 → L0106 → L0105 → L0106 → L0107` — a small but telling backtrack.
> That's because the second leg is *blind* to the first leg's geometry.
> A globally-optimal k-waypoint solver would close the gap. We discuss
> that limitation in the report.

**[Slide 10 — The Week 10 follow-up · ~0:35]**

> *Bonus slide — answers the open question Mr Liu raised in Week 10.*
>
> The Dijkstra implementation we were given in Week 10 runs at
> **O(V³)** — about a billion operations on our thousand-node graph.
> The lecturer asked at the time: what if we didn't use
> `ArrayList<Integer> T`? What if we picked a different data structure?
>
> The answer is **two swaps, working as a pair.**
>
> *First* — `ArrayList<Integer> T` becomes `HashSet<String> settled`.
> The "is this node already settled?" check drops from O(V) to **O(1)**.
>
> *Second* — the linear scan over the cost array becomes a
> **`PriorityQueue`**, a binary heap. Extract-min drops from O(V) to
> **O(log V)** per pop.
>
> Together, those two swaps take the complexity from **O(V³)** all the
> way to **O((V + E) log V)** — that's about **thirty-thousand times
> cheaper** on our graph.
>
> The key insight is that the two swaps **only work as a pair**. The
> heap finds the minimum in log time, but only if the membership check
> is also fast. Replace just one of them and you still have a quadratic
> loop. *The optimisation lives in the interaction, not in either
> structure on its own.*

**[Slide 11 — Complexity & alternatives · ~0:25]**

> A quick word on alternatives. **Floyd–Warshall** computes *all-pairs*
> shortest paths in O(V³) — a billion operations on a thousand-node
> graph. Four heap-Dijkstras come in around a hundred thousand. The
> point is, **algorithm choice is a function of the query pattern**, not
> absolute speed. If we had thousands of queries, Floyd's amortised cost
> would win. With four, Dijkstra wins by four orders of magnitude.

**[Slide 12 — Task C OOP · ~0:30]**

> That brings me to Task C — the design.
>
> Our application has **five packages**: `model` for immutable data,
> `io` for CSV reading, `sort` and `graph` for the algorithms, and `app`
> as the driver. The whole system pivots on **two interfaces** — `Sorter`
> and `ShortestPathFinder` — which separate the *what* from the *how*.
>
> Adding a fourth sort algorithm, say Heap Sort, is one new class —
> `Main` doesn't change. Adding BFS as an alternative to Dijkstra — same
> story.
>
> The four OOP principles each map to a concrete code site:
> **encapsulation** in our immutable `Candidate` class; **inheritance**
> through `implements Sorter`; **abstraction** through the two
> interfaces; and **polymorphism** through dynamic dispatch in the
> benchmark loop.

**[Slide 13 — Data structures · ~0:25]**

> A few words on data structures. `ArrayList` for the sortable input —
> random access, cache-friendly. `HashMap` for the adjacency list —
> sparse-graph efficient. `PriorityQueue` for the Dijkstra frontier, and
> `HashSet` for the settled set.
>
> One subtle choice worth noting: our graph is undirected, but each edge
> is stored *twice* — once as a directed `Arc` from each endpoint. We
> pay a bit of memory for that, but it means Dijkstra's inner loop never
> has to ask 'which end of this edge is me?' — it just reads `arc.to()`.
> **Space-for-time, where the time is the inner loop of every query.**
>
> I'll hand back to Wenlu for the reflection.

---

## TEAMMATE — Part 2 · Task D + Closing (slides 14–15, ~1:30)

**[Slide 14 — Task D · ~1:00]**

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

**[Slide 15 — Closing · ~0:30]**

> To summarise what we delivered:
> three sorting algorithms benchmarked with proper methodology;
> a heap-based Dijkstra at O((V + E) log V) — about thirty-thousand
> times cheaper than the Week 10 textbook implementation on our graph;
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

- [x] Teammate name (Wenlu) populated throughout — verify spelling
      matches her Trello handle / coursework registration.
- [ ] Confirm the four Task B costs (0 / 27 / 39 / 48) by re-running
      `Main` immediately before recording — these are the numbers we
      cite verbatim.
- [ ] Verify the three sorting timings by re-running `SortBenchmark`
      and let the teammate update the Slide 6 narration if needed.
- [ ] Time the full read-through twice. If over 8:00, drop either the
      Floyd-Warshall paragraph (Slide 11) or the Edge-vs-Arc paragraph
      (Slide 13) — both are bonus, not task-sheet-required. Keep
      Slide 8 (case composition) and Slide 10 (the W10 follow-up) —
      both are highest-value bonus content because they bridge
      Tasks A → B and answer a question Mr Liu raised in class.
- [ ] Both faces visible on camera throughout (Task E rule — failure
      is automatic 0).
- [ ] Native voice only; no English-translation TTS (Task E rule).
- [ ] MP4 ≤ 8:00 and ≤ 250 MB.
