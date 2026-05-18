# Report Draft — CPT204 Group Project (Group 22)

This folder is the working draft for the Word report. Each chapter lives
in its own `.md` file. Once all chapters are stable, we will copy them
into a single `.docx` and apply the required formatting (Calibri 12, line
spacing 1.5, normal margins, ≤ 20 pages for Ch 1–4).

## Files

- `00-README.md` — this file.
- `01-task-a-sorting.md` — Chapter 1 (Task A): sorting algorithm
  evaluation.
- `02-task-b-graph.md` — Chapter 2 (Task B): shortest-path computation.
- `03-task-c-design.md` — Chapter 3 (Task C): OOP design rationale.
- `04-task-d-reflection.md` — Chapter 4 (Task D): planning, EDI,
  life-long learning.
- `05-code.md` — Chapter 5: source code as plain text (assembled at
  the very end so it always matches the latest source tree).
- `06-appendix.md` — Chapter 6: console outputs, AI citations, extra
  diagrams not embedded in main text.
- `07-contribution.md` — Chapter 7: contribution form (50/50).

## Blocker conventions

Anywhere a piece of information is still missing, the draft uses a
`<!-- BLOCKER: ... -->` comment so it is searchable and impossible to
miss. Examples:

- `<!-- BLOCKER: student ID for James -->`
- `<!-- BLOCKER: AI citation tool name & version -->`

After every change, run `grep -n "BLOCKER" essay/*.md` to see what is
still outstanding.

## Page-budget plan

The 20-page limit applies only to Chapters 1–4. Suggested split:

| Chapter | Target pages |
|---|---|
| Ch 1 — Task A | 5 |
| Ch 2 — Task B | 5 |
| Ch 3 — Task C | 5 |
| Ch 4 — Task D | 5 |

Trim the analysis prose if a chapter overflows; never trim the
required-output tables.

## Key inputs already finalised

- **Sorting numbers** (Task A): see baseline run `2026-05-18` (warm-up +
  nanoTime + 3 timed runs averaged on Corretto 21).
- **Top-10 selections**: A = L0001..L0010, B = L0101..L0110,
  C = L0201..L0210.
- **Graph stats**: 1000 nodes, 2600 undirected edges.
- **Four shortest-path cases**: costs 0, 27, 39, 48 respectively.
- **UML diagrams**: `image/overall-arch.png`, `image/detail-class.png`,
  `image/class-inheritence.png` (Task C).
- **Trello screenshots**: `image/task-overall.png`,
  `image/task-detail-card.png` (Task D AI-tool subsection).
