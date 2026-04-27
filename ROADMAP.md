# Roadmap — CPT204 Group Project

Submission: **Sun 24 May 2026, 23:59 (UTC+8)** · Cut-off: **Fri 29 May 2026, 23:59**

This document is the plan for *you* (the developer who is typing the code
by hand) and your teammate. The agent-generated skeleton under `src/` is a
**reference**, not the submission — treat every file as something you must
re-type and understand line by line.

---

## Principles

1. **You type everything.** The skeleton is a reading exercise. Open one
   skeleton file, read it top to bottom, then re-type it into a fresh file
   of your own. Do not copy-paste.
2. **Understand before you type.** If a line does not make sense, stop and
   learn that concept first (list below).
3. **Keep unedited versions.** Before you ask any AI tool for proofreading
   of your report, save a snapshot of your own draft. Module policy
   requires those snapshots.
4. **Build after every file.** A 10-second `javac` catches 90% of mistakes
   early. Do not accumulate 5 files of errors.

---

## Phase 0 — Warm-up (today, ~1 hour)

**Goal:** confirm the toolchain works end-to-end on your machine.

- [ ] `java -version` shows Corretto 26.
- [ ] Run the existing skeleton:
      ```bash
      mkdir -p build/classes
      find src/main/java -name '*.java' > build/sources.txt
      javac -d build/classes @build/sources.txt
      java -cp build/classes app.Main
      ```
- [ ] Skim every file under `src/main/java/`. Don't worry about details;
      just map the packages to the five tasks.
- [ ] Decide split with teammate — suggestion below under "Team split".

---

## Phase 1 — Task A (Sorting) — typing order

Type files in this exact order; each builds on the previous.

### 1.1 `model/Candidate.java`
The simplest file. Immutable value class.

**Concepts to know first:**
- `final` fields vs `final` class
- Why pass fields through the constructor instead of setters (encapsulation)
- `java.util.Comparator` — especially `comparing`, `comparingInt`, `reversed`, `thenComparing`
- `Objects.requireNonNull`
- Overriding `equals` / `hashCode` / `toString`

**Deliverable check:** a tiny `main` method that builds two `Candidate`s
and prints `Candidate.RANKING.compare(a, b)` — should return negative /
zero / positive as expected.

### 1.2 `io/CsvReader.java` (candidates half only for now)
Read `candidates_A.csv` into `List<Candidate>`.

**Concepts:**
- `try-with-resources` (auto-close)
- `java.nio.file.Path`, `java.nio.file.Files.newBufferedReader`
- Character encoding (`StandardCharsets.UTF_8`) — why it matters
- `String.split(",", -1)` — what the `-1` limit does
- Checked vs unchecked exceptions; when to wrap vs rethrow
- Don't parse `location_id` as an integer; it's a string like `L0001`

**Deliverable check:** add a throwaway `main` that reads `candidates_A.csv`
and prints the first 5 rows.

### 1.3 `sort/Sorter.java`
A one-method interface. Trivial to type, but type out the Javadoc so you
understand *why* it is generic.

**Concepts:**
- Interfaces vs abstract classes — when to use which
- Generics: `<T>` method type parameter
- Bounded wildcards: `Comparator<? super T>` (PECS — producer-extends,
  consumer-super). Know why it's `? super T`, not `T`.

### 1.4 `sort/BubbleSort.java`
Simplest to get right. **Do not copy the skeleton blindly** — draw the
algorithm on paper first (swap pairs, after pass k the last k are fixed).

**Concepts:**
- The early-exit flag — why it makes sorted input O(n), not O(n²)
- In-place vs out-of-place
- Stability (bubble sort is stable — understand why)

**Sanity test:** sort a 5-element hand-crafted list, print it.

### 1.5 `sort/MergeSort.java`
Harder: recursion + an auxiliary buffer.

**Concepts:**
- Divide and conquer; recursion base case
- Why merge sort needs O(n) extra space
- Why it is stable if you merge with `<=` (not `<`) on the left half
- Top-down vs bottom-up merge sort (we use top-down)

**Concept pitfall to watch:** off-by-one in the merge indices is the
classic bug. Walk through `merge(0, 1, 3)` on paper.

### 1.6 `sort/QuickSort.java`
Trickiest of the three.

**Concepts:**
- Lomuto vs Hoare partition (we use Lomuto)
- Pivot choice — first / last / random / **median-of-three**
- Worst case O(n²) on already-sorted input with naive pivot → why we use
  median-of-three
- Recursion depth and stack overflow risk on pathological input

**Concept pitfall:** after partitioning, the pivot index is at `i + 1`;
get that wrong and the recursion diverges.

### 1.7 `sort/SortBenchmark.java`
Simple harness. Key idea: give each run a *fresh copy*, else after run 1
the list is already sorted.

**Concepts:**
- `System.nanoTime()` vs `System.currentTimeMillis()` — nanoTime for
  durations, currentTimeMillis for wall-clock
- Why averaging multiple runs helps
- JIT warm-up — for a short report like this, 3 runs is enough; you can
  mention JIT as a caveat in Task A analysis

### 1.8 Hook it into `app/Main.java` (sort section only)
At this point Main can do Task A end-to-end. **Commit when Task A works.**

---

## Phase 2 — Task B (Graph) — typing order

### 2.1 `model/Edge.java`
Immutable triple (from, to, weight). Reject negative weights.

**Concepts:**
- Why negative weights would break Dijkstra
- Undirected graph representation choice (store once, insert both
  directions in the adjacency list)

### 2.2 `io/CsvReader.java` (add `readEdges`)
Just the second half.

### 2.3 `graph/Graph.java`
Adjacency list as `Map<String, List<Arc>>`.

**Concepts:**
- Adjacency list vs adjacency matrix — memory and lookup trade-offs
- Why `HashMap` / `ArrayList` are appropriate here
- `computeIfAbsent` — idiom for lazy bucket creation
- Defensive copies vs `Collections.unmodifiableList`

**Deliverable check:** a throwaway `main` that builds the graph from
`paths.csv` and prints `graph.nodeCount()` and `graph.neighbours("L0001")`.

### 2.4 `graph/PathResult.java`
Immutable record of a query result. Self-loop case returns a single node
with cost 0.

### 2.5 `graph/ShortestPathFinder.java`
Interface + default method `findVia`.

**Concepts:**
- Default methods on interfaces — why they exist
- Segment-wise optimality vs global optimality (this is **the** Task B
  analysis question). Build a small 4-node counter-example on paper: a
  path that must visit W can be globally worse than segment-optimal if W
  forces a detour.

### 2.6 `graph/DijkstraShortestPath.java`
The heavy file.

**Concepts:**
- Dijkstra's relaxation rule
- Why it needs non-negative weights (it commits distances on pop)
- Binary-heap priority queue — why `O((V+E) log V)`
- "Lazy deletion" pattern: on pop, skip nodes already settled
- Predecessor map → path reconstruction by walking back from `end`
- What a `settled` set protects against (re-processing the same node)

**Concept pitfall:** do not add `start` with distance `Integer.MAX_VALUE`
to the priority queue "just in case"; start is the only node that starts
at 0.

### 2.7 Wire into `app/Main.java` — all four cases
Confirm:
- Case 1 cost = 0, path = `[start]`.
- Case 2 cost > 0, path length ≥ 2.
- Case 3 and Case 4 may revisit nodes across segment boundaries — that is
  expected with segment-wise stitching. Note it in Task B analysis.

---

## Phase 3 — Tests (recommended, not required)

Not in the spec, but cheap and gives you evidence for the report.

- [ ] `src/test/java/sort/SorterTest.java` — same inputs, assert all three
      produce identical output. Verifies correctness of the hand-typed
      implementations.
- [ ] `src/test/java/graph/DijkstraShortestPathTest.java` — a tiny
      hand-drawn 5-node graph with a known shortest path; assert cost and
      node list.
- [ ] `src/test/java/graph/FindViaTest.java` — build a graph where the
      segment-wise path differs from the naive shortest path; use this as
      the "local vs global optimum" example in the report.

**Note:** if JUnit is outside your module's library list, write the tests
as plain `main` methods with `assert` statements and run with
`java -ea`. Keep them simple; the point is reproducibility evidence.

---

## Phase 4 — Task C (Design writeup)

After the code works, write it up. **Don't** write it before — the words
have to match the code.

Deliverables for the report (Chapter 3):

1. **Data structures** — one short paragraph each:
   - Candidate lists → `ArrayList<Candidate>` (random access for sorting).
   - Graph → adjacency list `HashMap<String, List<Arc>>` (sparse graph).
   - Priority queue → binary heap via `java.util.PriorityQueue`.
2. **Class diagram** — one UML picture. Tools: PlantUML, draw.io, or
   Mermaid in a markdown file. Include: `Candidate`, `Edge`, `Graph`,
   three `Sorter` implementations, `Dijkstra`, `PathResult`, `Main`.
3. **OOP principles — concrete examples in your code:**
   - Encapsulation: `Candidate` / `Edge` / `PathResult` are immutable.
   - Abstraction: `Sorter`, `ShortestPathFinder` interfaces.
   - Polymorphism: `Main` iterates over `List<Sorter>`; at runtime each
     call dispatches to a different concrete class.
   - Inheritance: all three sorters implement a common supertype. (If the
     marker expects a class hierarchy, extract a small
     `AbstractComparisonSorter` that handles the swap helper.)

---

## Phase 5 — Report Chapters 1 & 2

Fill in after you have real numbers from `Main`. Your writing > mine; I
won't pre-draft these.

Suggested structure:

- **Ch 1 Task A**
  1. Brief description of each algorithm (2–3 sentences each).
  2. The required results table (three datasets × three algorithms,
     timings + top 10).
  3. Analysis answering **every** bulleted question in the task sheet.
     Skipping one = lost marks.
- **Ch 2 Task B**
  1. Graph construction summary.
  2. Dijkstra implementation summary + complexity.
  3. Four cases in a table.
  4. Analysis questions (again — all of them).

---

## Phase 6 — Task D (Reflection) — YOUR text

The steering file and I leave this alone on purpose. Handwrite:

- AI-assisted planning (JIRA / Trello) — what you actually used.
- EDI reflection — at least one concrete application-level improvement
  (text-to-speech? colour-blind-safe palette? multilingual location IDs?).
- Life-long learning — 1 paragraph, honest, specific.

**Save unedited drafts** under a separate folder like `report-drafts/`.
Module policy may ask for these.

---

## Phase 7 — Task E (PPT + Video)

- Outline slides (10–12 slides max for an 8-min video).
- Split speaking time roughly 50/50.
- Record, export MP4. Double-check both faces visible.
- Stop well before the 8-minute cut-off; cutoff = 0 marks.

---

## Phase 8 — Package & Submit

- `zip -r code.zip src/ datasets/ README.md` (or equivalent).
- Four separate LMO portals: code zip, docx, mp4, ppt.
- Submit by **24 May 23:59** to stay penalty-free.

---

## Team Split (suggestion — adjust with your partner)

| You (developer A) | Partner (developer B) |
|---|---|
| `model/*`, `io/CsvReader`, `sort/*`, `SortBenchmark` | `graph/*`, Dijkstra, `findVia` |
| Ch 1 writeup (Task A) | Ch 2 writeup (Task B) |
| UML class diagram | OOP-principles subsection of Ch 3 |
| Record video part 1 | Record video part 2 |
| Slides 1–6 | Slides 7–12 |
| Ch 4 half (AI + lifelong) | Ch 4 half (EDI + contribution) |

Both of you write `Main` together — it's the glue.

---

## Self-Study Checklist (what you should understand before writing code)

Tick these off *before* Phase 1; most are in Liang's book or week-by-week
CPT204 lectures.

**Java core**
- [ ] Interfaces, abstract classes, `final` class/method/field
- [ ] Generics, bounded type parameters, PECS
- [ ] `equals` / `hashCode` / `toString` contract
- [ ] Checked vs unchecked exceptions; `try-with-resources`
- [ ] `java.util.Comparator` combinators
- [ ] `java.nio.file` basics

**Algorithms**
- [ ] Bubble sort, best/avg/worst case, stability
- [ ] Merge sort, auxiliary memory, stability
- [ ] Quick sort, pivot strategies, worst-case triggers
- [ ] Dijkstra's algorithm, why non-negative weights, relaxation
- [ ] Priority queues / binary heaps (big picture only)
- [ ] Big-O for all of the above

**OOP concepts**
- [ ] Four principles (encapsulation, inheritance, polymorphism,
      abstraction) — be able to point at **a line in your own code** for
      each one.

**Project management**
- [ ] Basic git workflow: `status`, `add`, `commit`, `push`, `pull`
- [ ] Writing a conventional commit message
- [ ] JIRA or Trello basics (for Task D's AI-assisted planning writeup)

---

## Hand-typing tips

- Keep the skeleton open in one window, a blank file in the other. Type
  the file, glance at the skeleton only when you are stuck on syntax.
- After each file compiles, **commit** with a focused message:
  `feat(sort): add Bubble Sort`, `feat(graph): add adjacency list Graph`.
  Small commits = easy to roll back one file.
- If your version behaves differently from the skeleton: **your version
  wins**, as long as the algorithm is correct. The skeleton is one
  choice, not the only correct one.

---

## Red flags — stop and ask

- You can't explain why a line is there. → Learn the concept first.
- A test passes but you don't trust it. → Run it by hand on a tiny input.
- The report copies a phrase from the skeleton's Javadoc. → Rewrite in
  your own words; the markers can spot boilerplate.
- You're < 48 h from submission and Task D isn't written. → Stop coding,
  finish the writeup first. Task D is 20% of the grade.
