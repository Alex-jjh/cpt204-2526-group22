package sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs a {@link Sorter} against a fresh copy of the input list a configurable
 * number of times and reports the average wall-clock runtime in milliseconds.
 *
 * <p>Each run gets its own copy so in-place sorters do not get a pre-sorted
 * input on subsequent runs.
 */
public final class SortBenchmark {

    /** How many times to repeat each sort (for averaging). */
    private final int runs;

    /**
     * Constructor.
     *
     * @param runs number of repetitions (must be >= 1)
     */
    public SortBenchmark(int runs) {
        if (runs < 1) throw new IllegalArgumentException("runs must be >= 1");
        this.runs = runs;
    }

    /**
     * Benchmark a single sorter on the given data.
     *
     * @param sorter the sorting algorithm to benchmark
     * @param data   the original unsorted list (never modified)
     * @param cmp    the comparator defining the sort order
     * @return a {@link Result} containing the algorithm name, average time,
     *         and the sorted list from the last run
     */
    public <T> Result run(Sorter sorter, List<T> data, Comparator<? super T> cmp) {
        long totalMillis = 0;
        List<T> lastSorted = null;

        for (int i = 0; i < runs; i++) {
            // 1. Copy the original data so each run starts from the same unsorted state
            List<T> copy = new ArrayList<>(data);

            // 2. Record start time
            long startTime = System.currentTimeMillis();

            // 3. Sort
            sorter.sort(copy, cmp);

            // 4. Record end time
            long endTime = System.currentTimeMillis();

            // 5. Accumulate elapsed time
            totalMillis += (endTime - startTime);

            // Keep the last sorted result for top-K selection
            lastSorted = copy;
        }

        long averageMillis = totalMillis / runs;
        return new Result(sorter.name(), averageMillis, lastSorted);
    }

    /**
     * Holds the benchmark result for one algorithm on one dataset.
     */
    public static final class Result {

        private final String algorithm;
        private final long averageMillis;
        private final List<?> sorted;

        public Result(String algorithm, long averageMillis, List<?> sorted) {
            this.algorithm = algorithm;
            this.averageMillis = averageMillis;
            this.sorted = sorted;
        }

        /** Name of the sorting algorithm (e.g. "Bubble Sort"). */
        public String algorithm() { return algorithm; }

        /** Average runtime in milliseconds across all runs. */
        public long averageMillis() { return averageMillis; }

        /** The sorted list from the last run (used for top-K selection). */
        @SuppressWarnings("unchecked")
        public <T> List<T> sorted() { return (List<T>) sorted; }
    }
}
