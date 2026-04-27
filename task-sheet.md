# CPT204 AY2526 — Group Project Task Sheet

## Introduction

This group project is the final coursework component of the course this semester, accounting for **40%** of the final mark. Teams of two apply object-oriented principles and advanced data structures to solve the tasks below.

The submission must demonstrate understanding of:

1. Object-oriented concepts and software design
2. Problem-solving techniques using sorting and graph algorithms
3. AI-assisted team cooperation and project management
4. Equality, diversity, and inclusion (EDI) principles
5. Planning, reflection, and self-learning in object-oriented programming

### Deliverables

- Java code in a ZIP file
- A Word report
- An MP4 video
- A PowerPoint (PPT) presentation used in the video

### Timeline

| Stage | Date |
|---|---|
| Task sheet release | Week 9, Monday, 27 April, 9:00 (UTC+8) |
| Submission | Week 12, Sunday, 24 May, 23:59 (UTC+8) |
| Submission cut-off | Week 13, Friday, 29 May, 23:59 (UTC+8) |

Late penalty: 5% per working day, up to 5 working days (Mon–Fri, Week 13). No submissions after 23:59 (UTC+8), 29 May.

---

## Project Brief

### Scenario

A city plans to carry out a large-scale infrastructure inspection task. Three different information sources each provide a candidate list of locations that may require inspection. The datasets come from different systems with different properties (e.g., ordered or unordered). The program must process these datasets, identify important inspection targets, and compute routes for visiting them efficiently.

### Overview

Design and implement an object-oriented Java application for an **Urban Infrastructure Inspection System**, integrating data structures, sorting algorithms, graph algorithms, and OOP principles.

Five tasks:

- **Task A** — analyze behaviour and performance of three sorting algorithms on the three candidate datasets; select the top 10 highest-priority locations from each dataset.
- **Task B** — use graph algorithms to compute routes/paths for visiting the selected inspection targets on a weighted graph.
- **Task C** — explain and justify the design of the overall application (data structures, class design, OOP principles).
- **Task D** — reflect on the project process (AI-assisted planning, teamwork, EDI).
- **Task E** — present the project via PPT and video.

### Provided Data

Three candidate datasets, each with 1000 candidate locations:

- `candidates_A.csv`
- `candidates_B.csv`
- `candidates_C.csv`

Each row contains:

- `location_id` — unique id for locations
- `priority_score` — the priority for inspection (higher score = higher priority)

One undirected weighted graph edge file:

- `paths.csv`

Each row represents a weighted edge:

- `from_location` — starting node
- `to_location` — ending node
- `weight` — distance

---

## Task A — Algorithm Evaluation: Sorting

Critically evaluate the performance of different sorting algorithms for candidate-location selection.

Evaluate these algorithms (as taught in class):

- Bubble Sort
- Quick Sort
- Merge Sort

### Ranking Rule (all algorithms must use the same rule)

- Sort by `priority_score` in **descending** order.
- If two rows have the same `priority_score`, sort by `location_id` in **ascending** order.

### For each dataset, the program must

1. Read the dataset from the CSV file.
2. Sort the data using the required ranking rule.
3. Measure and compare the running time of the three sorting algorithms.
4. Identify the top 10 highest-priority locations.

After all three datasets are processed:

- 10 selected locations from Dataset A
- 10 selected locations from Dataset B
- 10 selected locations from Dataset C

Total: **30 selected inspection targets**.

### Required Output

Provide a table summarizing sorting results and timing performance. Use `System.nanoTime()` or `System.currentTimeMillis()`. To reduce noise, run each algorithm multiple times (e.g., 3 times) and report the average.

Suggested format:

| Dataset | Bubble (ns/ms) | Quick (ns/ms) | Merge (ns/ms) | Top 10 Selected Locations |
|---|---|---|---|---|
| Dataset A | | | | |
| Dataset B | | | | |
| Dataset C | | | | |

### Required Analysis

- How does the initial order of the input data affect the performance of Bubble Sort, Quick Sort, and Merge Sort on the three datasets?
- Which of the three sorting algorithms performs best on each dataset? Explain with reference to data characteristics.
- Which sorting algorithm behaves most consistently across the three datasets? Why?
- If only one sorting algorithm were allowed in the final system, would you choose best average runtime, most stable behaviour, or simplest implementation? Why?
- If the number of candidate locations became significantly larger, which sorting algorithm would be the most suitable? Why?
- If both runtime efficiency and memory usage are considered, how would the algorithm choice be affected?

---

## Task B — Algorithm Evaluation: Graph

Use the 30 selected targets from Task A to identify several shortest paths on the weighted graph in `paths.csv`, and evaluate the graph algorithm used.

`paths.csv` contains edge information of an undirected weighted graph; each row represents a weighted connection between two locations, travellable in both directions.

> **Note:** The weighted graph in `paths.csv` defines the **complete** infrastructure network. The 30 selected targets from Task A should be treated as important nodes within this graph, not as the only nodes.

### Required Output — four shortest-path query cases

- **Case 1:** from the 1st selected location in Dataset A to itself.
- **Case 2:** from the 1st selected location in Dataset A to the 10th selected location in Dataset A.
- **Case 3:** from the 1st selected location in Dataset A to the 1st selected location in Dataset B, via the 5th selected location in Dataset B.
- **Case 4:** shortest path from the 1st selected location in Dataset A to the 1st selected location in Dataset C, such that the path must first pass through the 5th selected location in Dataset B, then through the 5th selected location in Dataset C, in this order.

For each case, provide:

- starting node (e.g., `L0001`)
- destination node (e.g., `L0010`)
- shortest path found (e.g., `L0001 -> L0002 -> ...`)
- total path cost (distance)

Outputs may be given in a table, or via console-output screenshots.

### Required Analysis

- What graph algorithm did you use, and why is it suitable for this weighted graph?
- How is the algorithm implemented, and what are its time and space complexities (Big-O)?
- If each shortest-path query (cases 1–4) is solved optimally, does that mean the whole inspection-planning problem is solved optimally? Why or why not?
- What alternative graph algorithms could be considered if the graph were unweighted? How would they compare?
- What alternative algorithms / route-planning approaches might become more suitable if the graph were much larger, or if node coordinates were available? Discuss advantages and limitations.

---

## Task C — Design of the Overall Application

Explain and justify the design of the Urban Infrastructure Inspection System as a complete object-oriented Java application.

### Data Structures

- What data structure(s) represent the three candidate datasets?
- What data structure(s) represent the weighted graph in `paths.csv`?
- Why are these suitable for sorting, graph traversal, and shortest-path computation?
- How do they support efficiency and clarity of implementation?

### Classes and Functions

- What classes are used?
- What are their responsibilities?
- What important public or private functions do they have?
- How do they collaborate to complete the inspection workflow?

*(UML diagrams or code screenshots may be used.)*

### Object-Oriented Design

- Which OOP principles (encapsulation, inheritance, polymorphism, abstraction) are applied?
- How are they applied specifically (e.g., polymorphism for different behaviours via a common interface)?
- Why are these principles important to the program?

*(UML diagrams or code screenshots may be used.)*

---

## Task D — Project Reflection

Reflect on planning, development, and evaluation as a team. Show what was learned, how tools/resources were used, and broader software-development considerations.

### AI-assisted Planning and Collaboration

- How were AI tools (e.g., JIRA, Trello, as introduced in class) used for planning and collaboration (e.g., task allocation)?
- Advantages and disadvantages of AI-empowered project management tools?

### Equality, Diversity, and Inclusion

- Understanding of EDI in the context of this project; why are these principles important in software design?
- Based on these principles, in what areas can the project be optimized in future? (e.g., equality → text-to-speech for visually impaired users)
- What challenges might arise when applying such improvements, and how can they be addressed?

### Life-long Learning and Future Improvement

- What did this project teach about life-long learning in software development?
- How did you contribute to advanced software components as a developer in a team?
- What would be the next step if this application were developed further?

---

## Task E — Project Presentation

Create a video explanation with PPT slides illustrating the project succinctly.

Requirements:

- Briefly introduce the project (purpose, key objectives, role of the three candidate datasets and the weighted graph).
- Explain how sorting algorithms are applied in Task A, including how the top 10 locations are selected.
- Explain how the graph algorithm is applied in Task B, including how shortest-path queries are defined and how results are obtained.
- Explain how OOP principles are reflected in the overall design in Task C.
- Conclude with reflection on planning, collaboration, EDI principles, and future development.

---

## Report Requirements

### Structure

- Coursework submission cover page *(compulsory; template on CPT204 LMO homepage)*
- **Chapter 1** — Sorting Algorithm (Task A), sub-chapters allowed
- **Chapter 2** — Graph Algorithm (Task B), sub-chapters allowed
- **Chapter 3** — Design of the Overall Application (Task C), sub-chapters allowed
- **Chapter 4** — Project Reflection (Task D), sub-chapters allowed
- **Chapter 5** — Program Code
  - Include all code as **text** (no screenshots). Copy and paste each source file.
- **Chapter 6** — Appendix *(console outputs, extra sorting results, shortest-path outputs, UML diagrams not in main text)*
- **Chapter 7** — Contribution Form

| Student ID | Contribution |
|---|---|
| | |
| | |

*Total must equal 100%. Equal contribution = 50% / 50%.*

### Formatting

- Font: **Calibri**
- Font size: **12**
- Line spacing: **1.5**
- Margins: **Normal**

Additional:

- Page limit: **max 20 pages**, excluding cover page and Chapters 5–7.
- Images, tables, UML diagrams, and output screenshots encouraged.

### Coding

- Use only libraries covered in CPT204 (class content and/or Liang's textbook).
- Third-party libraries not covered in CPT204 may incur penalties per module policy.

---

## Presentation Requirements (Task E)

- Clearly demonstrate understanding of algorithm implementation, application design, analysis, and reflection — succinctly.
- Any PPT template allowed (XJTLU standard not required).
- Video **must not exceed 8 minutes**.
- Video must be recorded **jointly by both students**, each presenting roughly equal time.
- **Own voice** for narration. **Faces on camera** required (both students; either one-at-a-time or simultaneously).

> Exceeding time limit, using English audio translation software for narration, or failing to show faces → **score of 0 for Task E**.

Submit to LearningMall:

- MP4 video file
- PPT file used in the video

---

## Submission Requirement

Four separate submission portals on LMO. Submit:

- ZIP file containing **all** code files
- Word file for the report
- MP4 file for the video recording
- PPT file used in the video recording

---

## Marking Metrics

### Report (Tasks A–D): 80 / 100

| Task | Weight | Criteria |
|---|---|---|
| **Chapter 1 — Task A** | 20 | Correct implementation of sorting algorithms; correct target selection (top 10 per dataset); analysis of dataset properties (ordered/unordered) influencing sorting behaviour; justification of algorithm choice under different considerations (runtime stability, scalability, memory). |
| **Chapter 2 — Task B** | 20 | Correct construction of the weighted graph from `paths.csv`; correct application of the graph algorithm (direct queries and constrained queries with waypoint(s)); correct case handling and output (start, destination, waypoint(s), shortest path, total cost); algorithm analysis (justification, complexity, correctness, local vs global optimality, alternatives). |
| **Chapter 3 — Task C** | 20 | Clarity and correctness of overall application design (Tasks A and B integrated into one coherent Java application); selection and justification of data structures for datasets and graph; classes and functions with collaboration; meaningful discussion of OOP principles in the implementation. |
| **Chapter 4 — Task D** | 20 | AI-assisted planning and collaboration — clear explanation and critical evaluation; EDI awareness — understanding and critical discussion of future improvement; life-long learning and team role; depth of reflection (thoughtful, specific, grounded in actual experience). |

### Code and Presentation: 20 / 100

| Task | Weight | Criteria |
|---|---|---|
| **Chapter 5 — Code** | 10 | Code clarity, organization, and readability (variable naming, comments, documentation, structure); correctly supports sorting workflow, graph query workflow, and overall application logic; design quality (coherent OO design, not a collection of unrelated functions). |
| **PPT + Video** | 10 | Logical flow; coverage of key tasks (sorting, graph algorithms, overall design, reflection); clarity and fluency; effectiveness of communication (good visual support). |

---

## Academic Integrity

1. Only **non-substantive** AI use permitted — minor tasks such as grammatical corrections, formatting adjustments, or generic suggestions ("improve clarity"). Keep unedited versions of work prior to AI processing; may be required for verification. If unsure, consult the module leader. Appendix must include a brief citation of tool name, version, and use, e.g.:

   > [1] DeepSeek V3.1, available at <https://www.deepseek.com>. Used for grammatical proofreading.

2. Individual students may be invited to explain parts of their code in person. Failure to demonstrate understanding → no credit for that part.
3. In severe cases, suspected violations are reported to the Exam Officer; if confirmed, permanently recorded on the offender's official academic transcript.
4. Reference: <https://academicpolicy.xjtlu.edu.cn/article.php?id=138>
