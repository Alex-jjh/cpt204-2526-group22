# Chapter 1 — Sorting Algorithm (Task A)

## 1.1 Implementation, Methodology, and Comparison

We implemented Bubble Sort, Quick Sort, and Merge Sort in Java in strict
accordance with the algorithms presented in Liang Ch 23 (§23.3, §23.4,
§23.5 respectively). All three sorters share a common `Sorter` interface
that takes a generic `List<T>` and a user-supplied
`Comparator<? super T>`, so the project's ranking rule
(`Candidate.RANKING`: priority score descending, then location id
ascending) is decoupled from the algorithms themselves.

Three deliberate engineering choices in the implementation are worth
flagging because they directly shape the empirical results that follow:

- **Bubble Sort** uses an early-exit `swapped` flag (Liang §23.3): if a
  full pass executes without a swap, the algorithm returns immediately.
  This turns the best case from O(n²) to O(n).
- **Quick Sort** uses the **median-of-three** pivot strategy. Choosing
  the median of `list[lo]`, `list[mid]`, `list[hi]` as pivot avoids the
  pathological O(n²) behaviour that naive first/last-pivot Quick Sort
  exhibits on already-sorted input.
- **Merge Sort** is top-down with a single auxiliary buffer of size *n*,
  so its space cost is O(n), not O(n log n).

### Measurement methodology

Sub-millisecond timing on a 1000-element list is sensitive to JVM
warm-up and class-loading effects, so we measured each algorithm with
the following protocol:

1. **One untimed warm-up run** on a fresh copy of the input list. This
   triggers JIT compilation of the sort method so subsequent timings
   reflect compiled, not interpreted, code.
2. **Three timed runs**, each on a fresh copy of the input (so that
   in-place sorters do not benefit from a pre-sorted second run). Each
   run is bracketed by `System.nanoTime()` calls; nanoTime is monotonic
   and offers sub-millisecond precision, unlike `currentTimeMillis()`.
3. **Average of the three timed runs**, reported in milliseconds rounded
   to three decimal places.

The measurement harness is implemented once in `sort/SortBenchmark.java`
and reused by all three algorithms; the timing code is identical, so any
difference in reported time reflects only the algorithm under test. All
timings below were obtained on Amazon Corretto 21 (JDK 21.0.11) on a
macOS Apple-silicon machine.

### Required Output

| Dataset | Bubble (ms) | Quick (ms) | Merge (ms) | Top 10 Selected Locations |
|---|---|---|---|---|
| Dataset A | **0.103** | 0.159 | 0.176 | L0001, L0002, L0003, L0004, L0005, L0006, L0007, L0008, L0009, L0010 |
| Dataset B | 3.169 | **0.072** | 0.216 | L0101, L0102, L0103, L0104, L0105, L0106, L0107, L0108, L0109, L0110 |
| Dataset C | 0.748 | 0.144 | **0.123** | L0201, L0202, L0203, L0204, L0205, L0206, L0207, L0208, L0209, L0210 |

The fastest algorithm on each dataset is bolded. The total of 30
inspection targets (10 from each dataset) is the input to Task B.

## 1.2 Analysis of Dataset Properties

### How does the initial order affect performance?

Initial order matters most for Bubble Sort and least for Merge Sort, with
Quick Sort sitting in between.

**Dataset A is already sorted in priority-descending order** (priority
scores 10000, 9999, 9998, …). Bubble Sort therefore makes one full pass
without any swap, the early-exit flag fires, and the algorithm returns
in O(n). The measured 0.103 ms is the smallest Bubble-Sort time across
all three datasets, confirming the best-case behaviour. This is also
why Bubble Sort *outperforms* the asymptotically better Quick Sort and
Merge Sort on this dataset: at n = 1000, the constant factor of a single
linear pass beats a Θ(n log n) algorithm.

**Dataset B is randomly shuffled**. Bubble Sort here is closest to its
average case (and approaches its worst case): 3.169 ms, about 30× the
Dataset A figure. Quick Sort dominates this dataset (0.072 ms) because
random input is exactly the regime in which the median-of-three pivot
yields balanced partitions and the divide-and-conquer recursion runs at
its theoretical Θ(n log n) cost. Merge Sort is also Θ(n log n) but pays
the price of the auxiliary buffer copy on every merge, so it lags Quick
Sort by ≈ 3×.

**Dataset C contains many tied priority scores** (large blocks of
duplicates such as five `5000`s, eight `4951`s, etc.). The
`Candidate.RANKING` comparator falls through to the locationId
tiebreaker on every tie, increasing comparison cost. Merge Sort is
fastest here (0.123 ms) because (a) merging skips a comparison when one
half is exhausted and (b) Lomuto partition behaves slightly worse on
ties than ideal due to many "equal-to-pivot" elements clustering on one
side. Bubble Sort is moderately slow (0.748 ms): its early-exit flag
still helps because adjacent equal elements never trigger a swap, but
less dramatically than on the already-sorted Dataset A.

### Which algorithm performs best on each dataset?

Each of the three algorithms wins on exactly one dataset, which is a
useful illustration of how algorithm choice depends on input properties:

- **Dataset A: Bubble Sort** wins, courtesy of the early-exit flag
  catching the already-sorted input.
- **Dataset B: Quick Sort** wins, courtesy of median-of-three pivots on
  random data.
- **Dataset C: Merge Sort** wins, courtesy of stable, predictable
  Θ(n log n) behaviour on tie-heavy data.

### Which algorithm behaves most consistently?

Merge Sort. Its three measurements (0.176 / 0.216 / 0.123 ms) are the
narrowest spread (≈ 1.8×) of the three algorithms. The reason is
structural: Merge Sort's recursion tree depth is exactly ⌈log₂ n⌉ and
its number of comparisons per level is at most n, regardless of input.
It has no best-case shortcut (unlike Bubble Sort's early-exit) and no
pivot-selection variance (unlike Quick Sort).

By contrast, Bubble Sort's spread is 0.103 to 3.169 ms (≈ 31×); it is
the *least* consistent algorithm. Quick Sort's spread is 0.072 to
0.159 ms (≈ 2.2×), close to Merge Sort but slightly more variable
because pivot quality fluctuates with input.

## 1.3 Justification of Algorithm Choice

### If only one algorithm could be chosen for the final system

We would choose **Quick Sort with median-of-three pivots**. The three
selection criteria (best average runtime, most stable behaviour,
simplest implementation) point in different directions, but in the
context of the inspection system the priority order is:

1. **Worst-case robustness over best-case speed.** The infrastructure
   data feeding the system in production is not guaranteed to be
   pre-sorted. A naive Bubble Sort would be acceptable on
   Dataset-A-like inputs but catastrophic on Dataset-B-like ones (≈ 30×
   slower). Quick Sort's median-of-three eliminates Bubble Sort's
   advantage on already-sorted input while remaining fast on random
   data.
2. **Consistency vs raw speed at our scale.** Merge Sort is more
   consistent in asymptotic terms but ≈ 2× slower than Quick Sort on
   random data at n = 1000, and the gap widens for larger n in practice
   because Merge Sort's auxiliary-buffer writes have worse cache
   locality than Quick Sort's in-place swaps.
3. **Implementation simplicity is a weak criterion.** Bubble Sort is
   marginally simpler but has no strong guarantee on adversarial input.
   Implementation cost is paid once; runtime cost is paid every query.

### Scaling: if n becomes much larger

Quick Sort remains the strongest choice for scaling, with one caveat.
Both Quick Sort and Merge Sort are Θ(n log n) asymptotically, but
constants and memory access patterns matter at scale:

- **Quick Sort** sorts in place, so its memory footprint stays at
  O(log n) (the recursion stack). On modern CPUs its sequential
  partition writes have excellent cache locality.
- **Merge Sort** allocates an O(n) auxiliary buffer; for n in the tens
  of millions this becomes the dominant cost (allocation + memory
  bandwidth).
- **Bubble Sort** is unsuitable at any larger scale due to its O(n²)
  average and worst case.

The one caveat for Quick Sort is the worst case: even with
median-of-three, an adversarial input pattern can still drive recursion
to O(n²). At very large scale, an introspective sort (start with Quick
Sort, fall back to Heap Sort once recursion depth exceeds 2 log₂ n)
gives the best of both worlds; this is the strategy used by
`java.util.Arrays.sort` for primitives.

### Joint consideration of runtime and memory

When both runtime efficiency and memory usage are scored, Quick Sort
again wins, and the gap over Merge Sort widens.

| Algorithm | Time (avg) | Auxiliary space | Stable |
|---|---|---|---|
| Bubble Sort | O(n²) | O(1) | yes |
| Quick Sort | O(n log n) | O(log n) | no |
| Merge Sort | O(n log n) | O(n) | yes |

Merge Sort's main weakness for the inspection system is the O(n)
auxiliary buffer; in a memory-constrained deployment (say, an embedded
inspection terminal) this caps the input size that can be sorted. Quick
Sort, sorting in place with O(log n) recursion depth, has no such cap.
The only Merge Sort advantage (stability) is irrelevant to this project
because the `Candidate.RANKING` comparator is *total*: no two valid
`Candidate` objects can compare equal under it, since the locationId
tiebreaker is unique. Stability would matter only if our ranking allowed
ties, which it does not.

**Quick Sort with median-of-three is therefore the runtime-and-memory
optimum for the inspection system at any realistic scale.**
