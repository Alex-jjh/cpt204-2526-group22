---
inclusion: always
---

# CPT204 Group Project — Urban Infrastructure Inspection System

Reference: see `task-sheet.md` at the repo root for the full task description.

## Scope

An object-oriented **Java** application that:

1. Reads three candidate-location datasets (`datasets/candidates_A.csv`, `candidates_B.csv`, `candidates_C.csv`), each with 1000 rows of `location_id, priority_score`.
2. Sorts each dataset with **Bubble Sort**, **Quick Sort**, and **Merge Sort**; measures runtime; selects the top 10 per dataset (30 total inspection targets).
3. Builds an undirected weighted graph from `datasets/paths.csv` (`from_location, to_location, weight`). The graph is the **complete** infrastructure network; the 30 selected targets are important nodes inside it, not the only nodes.
4. Runs four shortest-path query cases over the graph (see below).

## Ranking Rule (used by every sorting algorithm)

- `priority_score` **descending**
- tie-break: `location_id` **ascending**

## Shortest-Path Cases

Let `A[i]` / `B[i]` / `C[i]` be the i-th selected location (1-indexed) from dataset A / B / C.

- **Case 1:** `A[1] → A[1]` (self).
- **Case 2:** `A[1] → A[10]`.
- **Case 3:** `A[1] → B[1]`, via `B[5]`.
- **Case 4:** `A[1] → C[1]`, must first pass through `B[5]`, **then** through `C[5]`, in this order.

For each case output: start node, destination node, path (e.g. `L0001 -> L0002 -> ...`), total cost.

## Hard Constraints

- **Language:** Java only.
- **Libraries:** only those covered in CPT204 (class content and/or Liang's textbook). **No third-party libraries** outside that scope — this includes no external CSV parser, no Guava/Apache Commons/etc. Standard JDK (`java.util`, `java.io`, `java.nio`, etc.) is fine.
- **Sorting:** must implement Bubble / Quick / Merge from scratch; do **not** call `Arrays.sort` / `Collections.sort` for Task A's comparison.
- **Timing:** use `System.nanoTime()` or `System.currentTimeMillis()`; average over multiple runs (e.g. 3) to reduce noise.
- **OOP:** design must be coherently object-oriented (not a bag of static utilities). Apply encapsulation, inheritance, polymorphism, and abstraction meaningfully (e.g. a common `Sorter` abstraction for the three algorithms).
- **Reproducibility:** results (top-10 lists, timings, path outputs) must be reproducible by running the program against the provided CSVs.

## Deliverables

1. ZIP of all Java source files.
2. Word report (`.docx`) — structure fixed by the task sheet:
   - Cover page
   - Ch 1 Task A (sorting)
   - Ch 2 Task B (graph)
   - Ch 3 Task C (design)
   - Ch 4 Task D (reflection)
   - Ch 5 Program Code — **full source as text, no screenshots**
   - Ch 6 Appendix
   - Ch 7 Contribution Form
3. MP4 video ≤ 8 minutes, both members on camera, own voice, roughly equal speaking time.
4. PPT used in the video.

Report body (Ch 1–4) is capped at **20 pages**, Calibri 12, line spacing 1.5, normal margins.

## Key Dates (UTC+8)

- Release: Mon 27 Apr 2026, 09:00
- Submission: Sun 24 May 2026, 23:59
- Cut-off: Fri 29 May 2026, 23:59 (5% late penalty per working day, max 5 days)

## Marking Weights

| Component | Weight |
|---|---|
| Ch 1 Task A | 20 |
| Ch 2 Task B | 20 |
| Ch 3 Task C | 20 |
| Ch 4 Task D | 20 |
| Ch 5 Code    | 10 |
| PPT + Video  | 10 |

## Coding Style Defaults

- Java 17+ syntax is fine unless the textbook pins an earlier version.
- Package layout (suggested): `src/main/java/` with packages like `io`, `sort`, `graph`, `app`.
- Keep each class focused; prefer interfaces (`Sorter`, `ShortestPathFinder`) so OOP discussion in Task C is backed by real code.
- Deterministic CSV reading — no locale-dependent parsing. `location_id` is a string (e.g. `L0001`); keep it as a string, do not parse to int.
- Weights in `paths.csv` — read as `double` to be safe; verify the actual data before committing to `int`.
- Always build and run against the real CSVs under `datasets/` before claiming a task is done.

## Out of Scope for Now

Task D's "AI-assisted planning" reflection and the academic-integrity AI citation will be handled manually by the student (unedited versions kept separately). Do **not** auto-generate that content.
