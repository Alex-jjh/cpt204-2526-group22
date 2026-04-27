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

## Self-Study Checklist — Liang Textbook Reading Map

Below is the **exact** mapping from Liang's *Introduction to Java Programming
and Data Structures* chapters to each project task. Read them in the order
listed; earlier chapters are prerequisites for later ones.

### Foundation (read before you write any code)

These chapters cover the Java basics you'll use everywhere in the project.
If you're already comfortable with them, skim for review.

| Chapter | Title | Why you need it |
|---|---|---|
| **Ch 1** | Introduction to Computers, Programs, and Java | Toolchain: compiling, running, JDK vs JRE. |
| **Ch 2** | Elementary Programming | Variables, types, operators — used in every file. |
| **Ch 3** | Selections | `if`/`else`, boolean expressions — used in sort comparisons and graph relaxation. |
| **Ch 4** §4.3–4.4 | Characters and Strings | `String` operations — `location_id` is a String; CSV parsing uses `split`. |
| **Ch 5** | Loops | `for`/`while` — the backbone of every algorithm. |
| **Ch 6** | Methods | Defining, calling, overloading — every class has methods. |
| **Ch 7** §7.1–7.6, 7.11 | Single-Dimensional Arrays | Array basics + sorting arrays intro. We use `ArrayList` but the concepts are the same. |

### Task A — Sorting (Phase 1)

| Chapter | Sections | What to focus on | Maps to file |
|---|---|---|---|
| **Ch 9** | 9.2–9.5, 9.8–9.9, 9.12 | Defining classes, constructors, visibility modifiers, **encapsulation**, **immutable objects** | `model/Candidate.java` |
| **Ch 12** | 12.1–12.4, 12.10–12.11 | Exception handling, `try-with-resources`, `File` class, **file I/O** (`Scanner`/`BufferedReader`) | `io/CsvReader.java` |
| **Ch 13** | 13.2, 13.5–13.8 | **Abstract classes vs interfaces**, `Comparable`, when to use which | `sort/Sorter.java` |
| **Ch 19** | 19.1–19.4, 19.7 | **Generics**: type parameters `<T>`, bounded wildcards `<? super T>` (PECS) | `sort/Sorter.java` |
| **Ch 20** | 20.5–20.6 | `List`, **`Comparator` interface** — `comparing`, `reversed`, `thenComparing` | `model/Candidate.RANKING` |
| **Ch 23** | **23.3** Bubble Sort | Algorithm, early-exit optimization, O(n²) worst / O(n) best, **stability** | `sort/BubbleSort.java` |
| **Ch 23** | **23.4** Merge Sort | Divide-and-conquer, O(n) auxiliary space, O(n log n) all cases, **stability** | `sort/MergeSort.java` |
| **Ch 23** | **23.5** Quick Sort | Lomuto/Hoare partition, pivot strategies, O(n²) worst case, **not stable** | `sort/QuickSort.java` |
| **Ch 22** | 22.2–22.4 | **Big-O notation** — how to analyze and compare algorithm complexity | Report Ch 1 analysis |
| **Ch 18** | 18.1–18.5, 18.9 | **Recursion** — merge sort and quick sort are recursive; understand base case, call stack, recursion vs iteration | `sort/MergeSort.java`, `sort/QuickSort.java` |

**Key study questions for Task A (answer these before writing the report):**
- Why does Bubble Sort run in O(n) on Dataset A? → §23.3 + early-exit flag
- Why does Quick Sort risk O(n²) on already-sorted input? → §23.5 pivot choice
- Why is Merge Sort the most consistent? → §23.4 always O(n log n)
- What's the memory trade-off? → §23.4 (O(n) extra) vs §23.3/23.5 (in-place)

### Task B — Graph / Shortest Path (Phase 2)

| Chapter | Sections | What to focus on | Maps to file |
|---|---|---|---|
| **Ch 9** | 9.12 | Immutable objects (same pattern as Candidate) | `model/Edge.java` |
| **Ch 21** | 21.5–21.6 | **`HashMap`** — used for the adjacency list `Map<String, List<Arc>>` | `graph/Graph.java` |
| **Ch 20** | 20.10 | **Priority queues** — binary heap, `PriorityQueue` class | `graph/DijkstraShortestPath.java` |
| **Ch 28** | 28.1–28.4, 28.6–28.9 | **Graph basics**: terminology, adjacency list vs matrix, **DFS**, **BFS** | `graph/Graph.java`, Report Ch 2 analysis |
| **Ch 29** | **29.1–29.3**, **29.5** | **Weighted graphs**, `WeightedGraph` class, **Dijkstra's shortest path** — relaxation, non-negative weights, complexity O((V+E) log V) | `graph/DijkstraShortestPath.java` |
| **Ch 13** | 13.5 (interfaces), 13.8 (interfaces vs abstract classes) | `ShortestPathFinder` interface + default method `findVia` | `graph/ShortestPathFinder.java` |

**Key study questions for Task B:**
- Why does Dijkstra require non-negative weights? → §29.5 (it commits distances on pop; a later negative edge could invalidate that)
- What's the complexity? → O((V+E) log V) with binary heap
- If the graph were unweighted, what would you use instead? → BFS (§28.9), O(V+E)
- Does segment-wise optimal = globally optimal? → No. Build a 4-node counter-example on paper.
- What if the graph were much larger / had coordinates? → A* (mention as extension; not in textbook but worth a sentence in the report)

### Task C — OOP Design (Phase 4)

| Chapter | Sections | What to focus on | Report topic |
|---|---|---|---|
| **Ch 9** | 9.8–9.9, 9.12 | **Encapsulation**: private fields, getters, immutable classes | `Candidate`, `Edge`, `PathResult` are all immutable |
| **Ch 10** | 10.2–10.4 | **Class abstraction**, thinking in objects, class relationships (association, aggregation, dependency) | UML class diagram, how `Main` depends on `Sorter` and `ShortestPathFinder` |
| **Ch 11** | 11.2–11.8, 11.10 | **Inheritance and polymorphism**: superclass/subclass, method overriding, **dynamic binding** | Three `Sorter` implementations dispatched polymorphically in `Main` |
| **Ch 13** | 13.2, 13.5, 13.8 | **Abstract classes and interfaces**: when to use each, design trade-offs | `Sorter` interface, `ShortestPathFinder` interface |
| **Ch 20** | 20.2, 20.5 | **Collections framework**: `List`, `ArrayList` — why we chose them for candidate data | Data structure justification |
| **Ch 21** | 21.5 | **Maps**: `HashMap` for adjacency list | Data structure justification |

**What to write in the report:**
- For each OOP principle, point to **a specific line** in your code. Don't just say "we used encapsulation" — say "Candidate's fields are `private final`, exposed only through getters (§9.9), making the object immutable (§9.12)."
- Draw one UML class diagram showing all classes and their relationships.

### Task D — Reflection (Phase 6, your own writing)

No specific Liang chapters — this is about your experience. But for the
EDI section, think about:

| Topic | Possible angle |
|---|---|
| Accessibility | Text-to-speech for visually impaired users (mention Java's `javax.accessibility`) |
| Internationalization | Location IDs could be multilingual; `Locale`-aware formatting |
| Colour-blind safety | If you add any visualization, use a colour-blind-safe palette |

### Quick-Reference: Chapter → Task Matrix

| Liang Chapter | Task A | Task B | Task C | Notes |
|---|---|---|---|---|
| Ch 1–7 (Java basics) | ✓ | ✓ | ✓ | Foundation for everything |
| Ch 9 (Objects & Classes) | ✓ | ✓ | ✓ | `Candidate`, `Edge`, immutability |
| Ch 10 (OO Thinking) | | | ✓ | Class abstraction, relationships |
| Ch 11 (Inheritance & Polymorphism) | ✓ | | ✓ | `Sorter` polymorphism, dynamic binding |
| Ch 12 (Exceptions & Text I/O) | ✓ | ✓ | | CSV reading, error handling |
| Ch 13 (Abstract Classes & Interfaces) | ✓ | ✓ | ✓ | `Sorter`, `ShortestPathFinder` |
| Ch 18 (Recursion) | ✓ | | | Merge sort, quick sort recursion |
| Ch 19 (Generics) | ✓ | | ✓ | `Sorter<T>`, bounded wildcards |
| Ch 20 (Lists, Queues, PQ) | ✓ | ✓ | ✓ | `ArrayList`, `Comparator`, `PriorityQueue` |
| Ch 21 (Sets & Maps) | | ✓ | ✓ | `HashMap` for adjacency list |
| Ch 22 (Efficient Algorithms) | ✓ | | | Big-O analysis |
| Ch 23 (Sorting) | ✓ | | | Bubble §23.3, Merge §23.4, Quick §23.5 |
| Ch 28 (Graphs) | | ✓ | | Graph basics, BFS, DFS |
| Ch 29 (Weighted Graphs) | | ✓ | | Dijkstra §29.5 |

### Suggested Reading Order (most efficient path)

If you're short on time, read in this order — each step unlocks the next:

1. **Ch 9** (Objects & Classes) — unlocks writing `Candidate`, `Edge`
2. **Ch 12 §12.10–12.11** (File I/O) — unlocks `CsvReader`
3. **Ch 13 §13.5–13.8** (Interfaces) — unlocks `Sorter`, `ShortestPathFinder`
4. **Ch 19 §19.1–19.4** (Generics) — unlocks the `<T>` in `Sorter.sort()`
5. **Ch 20 §20.5–20.6** (Lists & Comparator) — unlocks `Candidate.RANKING`
6. **Ch 18 §18.1–18.5** (Recursion) — unlocks merge sort / quick sort
7. **Ch 23 §23.3–23.5** (Sorting algorithms) — **core of Task A**
8. **Ch 22 §22.2–22.4** (Big-O) — unlocks the analysis section
9. **Ch 21 §21.5** (Maps) — unlocks `Graph` adjacency list
10. **Ch 28 §28.1–28.4, 28.6–28.9** (Graphs, BFS, DFS) — graph foundations
11. **Ch 29 §29.1–29.3, 29.5** (Weighted graphs, Dijkstra) — **core of Task B**
12. **Ch 10, Ch 11** (OO Thinking, Inheritance) — unlocks Task C writeup

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
