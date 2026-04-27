# CPT204 Group Project — Urban Infrastructure Inspection System

Group 22, AY 2025/26.

See [`task-sheet.md`](task-sheet.md) for the full task description.

## Project Structure

```
src/main/java/
├── app/    # Main — end-to-end driver
├── io/     # CsvReader — candidates + paths files
├── model/  # Candidate, Edge — immutable data types
├── sort/   # Sorter + BubbleSort / QuickSort / MergeSort + SortBenchmark
└── graph/  # Graph, ShortestPathFinder + DijkstraShortestPath, PathResult

datasets/   # candidates_A.csv, candidates_B.csv, candidates_C.csv, paths.csv
```

## Build & Run

Requires **JDK 17+** (project uses Amazon Corretto 26).

```bash
# compile
mkdir -p build/classes
find src/main/java -name '*.java' > build/sources.txt
javac -d build/classes @build/sources.txt

# run
java -cp build/classes app.Main
# or with a custom dataset folder:
java -cp build/classes app.Main path/to/datasets
```

## Tasks

- **Task A** — `sort.*`: Bubble / Quick / Merge, benchmarked on the three
  candidate CSVs, top-10 selection per dataset.
- **Task B** — `graph.*`: Dijkstra on the undirected weighted graph in
  `paths.csv`; four shortest-path cases (self, direct, single waypoint,
  ordered two waypoints).
- **Task C** — OOP design rationale (report).
- **Task D** — Project reflection (report).
- **Task E** — PPT + video.
