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

<!-- BLOCKER: this chapter is auto-assembled. Run the script
     scripts/assemble-chapter5.sh (to be created) immediately before
     converting essay/ to the final docx. The script reads every
     src/main/java/**/*.java file in package order and emits a section
     of the form:

         ## src/main/java/<package>/<File>.java
         ```java
         <file contents>
         ```

     For now, leave this section as a placeholder. -->

<!-- BLOCKER: paste contents of all 12 .java files here in this order:
     1. src/main/java/app/Main.java
     2. src/main/java/io/CsvReader.java
     3. src/main/java/model/Candidate.java
     4. src/main/java/model/Edge.java
     5. src/main/java/sort/Sorter.java
     6. src/main/java/sort/BubbleSort.java
     7. src/main/java/sort/MergeSort.java
     8. src/main/java/sort/QuickSort.java
     9. src/main/java/sort/SortBenchmark.java
    10. src/main/java/graph/Graph.java
    11. src/main/java/graph/PathResult.java
    12. src/main/java/graph/ShortestPathFinder.java
    13. src/main/java/graph/DijkstraShortestPath.java -->
