# Chapter 4 — Project Reflection (Task D)

## 4.1 AI-Assisted Planning and Collaboration

We used **Trello** as the AI-assisted task-management tool throughout
the six-day final sprint. Trello belongs to the Atlassian product
family that the module's session slides explicitly named (Jira / Trello)
and ships several AI-empowered features (automatic activity logging,
smart reminders, and the Butler automation engine) that satisfy the
"AI-assisted Software Management tool" requirement.

### How we used it

The board was structured as a five-column Kanban (`Backlog`,
`This Week`, `In Progress`, `Review`, `Done`) with one card per
deliverable. Every card carried four pieces of metadata:

1. A **colour-coded label** identifying which task (A–E) the card
   contributed to.
2. A **member assignment** so each card had a single owner.
3. A **due date** aligned to the daily milestones in our roadmap
   (translation of Ch 1, Trello AI-tool screenshot, video recording,
   etc.).
4. A **checklist** of subtasks that turned each card into a small,
   testable unit.

The board overview is shown in `image/task-overall.png` and a single
expanded card (Module 1: Bubble Sort) is shown in
`image/task-detail-card.png`. The expanded view illustrates the level
of detail we kept on each card: description, acceptance criteria, due
date, checklist progress, member, label, and a chronological comment
trail.

### Why we planned the project this way

Three considerations drove the planning structure:

- **The 80/20 mark split.** Tasks A–D in the report are worth 80
  marks; the code and presentation are worth 20. We deliberately
  weighted the first two days of the sprint towards understanding and
  reading (task sheet, session slides, teammate's draft) rather than
  coding, because the highest-leverage outcome was a report whose
  prose accurately describes a working system, not a working system
  with no prose.
- **Parallelism with one-way dependencies.** Task A's analysis depends
  on running benchmarks; Task B's analysis depends on running the
  four cases. Both pipelines, once functional, free the writers from
  re-running code while drafting prose. We therefore put "stabilise
  benchmark methodology" and "verify Task B output" as the earliest
  blocking cards, ahead of any writing card.
- **Visibility under low-bandwidth coordination.** Two team members
  working in different time zones cannot rely on synchronous
  meetings. The Kanban board gives both of us asynchronous visibility:
  whoever wakes up first sees what is blocked, what is in progress, and
  what was just finished, without requiring the other person to be
  online.

### Advantages of AI-assisted task management

- **Activity log captures coordination automatically.** Every move
  between columns, every checklist tick, and every comment is timestamped
  and attributed. We never had to write a separate progress report;
  the board *is* the progress report.
- **Due-date reminders surface slipping work without nagging.** Trello
  emails the assignee 24 hours before a card's due date. This shifts
  accountability from "did your teammate remember to remind you?" to
  "did the system remind you?".
- **Templates and Butler automation reduce overhead.** A single
  template (description sections + checklist) was applied to every
  card; the small fixed cost of card creation pays back many times
  over the project's lifetime.

### Disadvantages we observed

- **AI suggestions can over-decompose simple tasks.** When we let
  Trello's automation suggest sub-tasks for a card that was already
  small (e.g. "format final report"), it produced four trivial
  sub-checks that added bookkeeping without adding clarity. We
  reverted to manual checklists for any card where the work was
  obvious.
- **Free-tier limits force trade-offs.** Time tracking and advanced
  analytics live behind a paywall. For a one-week project this was
  acceptable, but a longer engagement would require Power-Ups or a
  paid plan to keep the AI features useful.
- **Risk of over-fitting the workflow to the tool.** It is easy to
  spend more time grooming the board than doing the work. We capped
  card maintenance at ~10 minutes per day to keep the focus on
  deliverables.

A separate but related caveat applies to our use of AI **for content**
(grammar polishing, code-readability suggestions). AI tools
occasionally produce confidently-wrong claims; for example, an early
suggestion to pull in a third-party shortest-path library would have
violated the CPT204 library allowlist. This is the well-known
"hallucination" failure mode, and the right mitigation is the same one
academic integrity demands: verify every AI-produced claim against the
authoritative source (Liang's textbook, the task sheet) before
accepting it.

## 4.2 Equality, Diversity, and Inclusion

### Why EDI matters in software design

Software is rarely consumed by the same demographic that produces it.
The Urban Infrastructure Inspection System will, in any realistic
deployment, be operated by inspection staff with widely varying
backgrounds, abilities, and working conditions: some users may be
visually impaired, some may have limited reading proficiency in
English, some may be operating on low-bandwidth networks in field
conditions. Designing for an "average" user implicitly excludes
everyone outside that average. EDI is therefore not an after-the-fact
accessibility audit; it is a core design constraint from the first
sketch.

### Where the current system would exclude users

Three concrete exclusions are visible in the current implementation:

1. **Console-only output.** All routes and timings are printed as
   text to standard output. A visually impaired inspector with a
   screen reader can in principle consume this, but the formatting
   ("L0001 -> L0340 -> ...") is not optimised for speech: a
   text-to-speech engine reads each `L00xx` as four phonemes, slowing
   comprehension to a crawl on long paths.
2. **English-only identifiers and field names.** Location ids,
   priority labels, and any future user-facing strings assume an
   English-reading user. There is no mechanism for localisation.
3. **No alternative representations.** A path is presented only as a
   linear sequence of node ids. An inspector who is more comfortable
   with maps than with id sequences cannot consume the output without
   translating it mentally.

### Concrete improvements (with implementation sketches)

- **Text-to-speech path summary** for visually impaired users. Java's
  `javax.sound.sampled` API combined with an offline TTS engine
  (e.g. FreeTTS, MaryTTS) can synthesise a sentence-level summary of
  a `PathResult`: "Travel from location 1 to location 10 via 6 stops,
  total distance 27 units." This requires extending `PathResult` with
  a `formatSpeech()` method alongside `formatPath()`.
- **Internationalisation (i18n)** for diverse linguistic backgrounds.
  Java's `java.util.ResourceBundle` plus locale-aware
  `String.format` lets us swap user-facing strings via a single
  configuration change. The project's data layer (location ids, edge
  weights) is already locale-neutral, so the change is contained to
  the formatting layer.
- **Colour-blind-safe visualisation** if a graphical front-end is ever
  added. The default green-vs-red highlight common in routing tools
  is unreadable for the ~5 % of users with red-green colour
  blindness. Switching to a colour-blind-safe palette (e.g. blue and
  orange, or using both colour and shape) costs nothing and benefits
  every user.

### Implementation challenges and mitigations

- **Performance.** TTS synthesis introduces latency on the critical
  path of "compute route → present route". Mitigation: synthesise
  asynchronously on a background thread, so the textual output is
  available immediately and the audio arrives a moment later.
- **Memory and complexity overhead.** A localisation layer adds
  resource bundles for each supported language. Mitigation: ship only
  the bundle matching the user's locale at install time; lazy-load
  others.
- **Testing burden.** EDI features multiply the test surface (every
  feature × every locale × every assistive technology). Mitigation:
  integrate accessibility checks into automated tests using a tool
  like Java Accessibility Bridge; caught regressions are cheaper
  than missed ones.

The deeper point is that EDI improvements are not a separate, optional
"accessibility module": they are continuous design choices that, made
early, cost very little, and made late, cost a lot. The architectural
isolation between data (`Candidate`, `Edge`, `PathResult`) and
presentation (`Main`) in our current code means a future GUI/TTS
layer can be bolted on without disturbing the algorithms, exactly the
abstraction property argued for in §3.3.

## 4.3 Life-long Learning and Future Improvement

### What this project taught us about life-long learning

Three lessons stand out from the six-day sprint and apply well beyond
this project:

1. **A specification is a hypothesis to verify, not a contract to
   execute blindly.** We re-read the task sheet four separate times
   during the project; each re-reading caught a requirement we had
   misunderstood the previous round (the page-limit footnote applies
   only to Ch 1–4; the warm-up requirement is implicit in the session
   slides, not in the task sheet itself; the AI-tool screenshot
   requirement is *also* in the session slides only). The textbook
   skill of reading specifications carefully is not optional; it is
   the difference between a passing report and a top one.
2. **Implementation choices have downstream consequences for the
   report.** Switching the benchmark from `currentTimeMillis` to
   `nanoTime` did not change any algorithm, but it transformed the
   Task A timing table from a row of zeros to data with three
   decimal places of meaningful contrast. The lesson (that measurement
   methodology is itself a deliverable) applies to any software role.
3. **Documentation is not after-the-fact; it *is* the work.** The
   Experience Sharing PDF distributed to the cohort makes this point
   explicitly, and our sprint validates it: every screenshot, every
   commit message, and every Trello card description we produced *during*
   the work was an artefact we needed *for* the report. Teams that
   defer documentation pay it back at a higher interest rate.

### Contribution as a developer in a team

Within the team, our contributions divided along the natural axes of
the project:

- One member led the **algorithm pipelines**: the three sorters, the
  benchmark harness, and the Dijkstra implementation. This included
  the precision-related upgrades (warm-up phase, nanoTime,
  three-decimal output) that made Task A's analysis possible.
- The other member led the **report and presentation drafting**:
  the first-pass Chinese draft of all four chapters, the slide deck,
  and the slide-by-slide narrative for the video.
- Both members co-owned **integration and proofreading**: translation
  passes, cross-checking the report against the latest code output,
  recording the joint video, and final formatting.

Concrete contributions are itemised in the Contribution Form in
Chapter 7. The split is roughly equal in effort if not identical in
type of work.

### Next steps if the system were developed further

If the system were to evolve beyond this coursework, three directions
would unlock the most value:

1. **Time-dependent edge weights.** A real city road network has
   weights that vary by hour: rush-hour congestion makes the same
   geographic edge cost very differently at 8 AM vs midnight. A
   straightforward extension is to make `Edge.getWeight()` accept a
   timestamp and to make Dijkstra's relaxation aware of the time at
   which an edge would be traversed. This puts the system in the
   regime of *dynamic shortest paths* and connects directly to live
   traffic feeds.
2. **A\* with geographic coordinates.** If `paths.csv` were extended
   with latitude/longitude per node, A\* using straight-line
   Euclidean distance as the heuristic would explore far fewer nodes
   than Dijkstra on each query. Combined with the
   `ShortestPathFinder` interface this is a drop-in replacement: only
   the implementation class changes.
3. **A multi-waypoint planner that is globally optimal.** §2.4
   identified that `findVia`'s segment-wise stitching is not
   guaranteed to be globally optimal under sequential waypoint
   constraints. A Bellman–Held–Karp dynamic-programming planner over
   the small set of required waypoints (k ≤ 30 in our setting) would
   close that gap, and the cost is feasible at our scale (2^30 ·
   1000² is ≈ 10¹⁵, but on the practical 4-stop queries of Task B
   the relevant exponent is 2⁴, a non-issue).

Beyond these three, the system has obvious opportunities for a
graphical front-end (JavaFX) and multilingual support (§4.2), but the
algorithmic upgrades above offer the highest value-per-effort ratio.

The unifying lesson, and the one we will carry forward beyond CPT204,
is that the gap between a working program and a useful, evolving
system is bridged not by writing more code but by understanding the
problem more deeply, documenting decisions in the moment, and leaving
the architecture honest enough to admit later change.
