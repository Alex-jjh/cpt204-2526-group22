package sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runs a {@link Sorter} against a fresh copy of the input list a configurable
 * number of times and reports the average wall-clock runtime.
 *
 * <p>Each run gets its own copy so in-place sorters do not get a pre-sorted
 * input on subsequent runs.
 */
public final class SortBenchmark {

    private final int runs;

    public SortBenchmark(int runs) {
        if (runs < 1) throw new IllegalArgumentException("runs must be >= 1");
        this.runs = runs;
    }

    public <T> Result run(Sorter sorter, List<T> data, Comparator<? super T> cmp) {
        long totalNanos = 0;
        List<T> lastSorted = null;
        for (int i = 0; i < runs; i++) {
            List<T> copy = new ArrayList<>(data);
            long t0 = System.nanoTime();
            sorter.sort(copy, cmp);
            long t1 = System.nanoTime();
            totalNanos += (t1 - t0);
            lastSorted = copy;
        }
        return new Result(sorter.name(), totalNanos / runs, lastSorted);
    }

    public static final class Result {
        private final String algorithm;
        private final long averageNanos;
        private final List<?> sorted;

        public Result(String algorithm, long averageNanos, List<?> sorted) {
            this.algorithm = algorithm;
            this.averageNanos = averageNanos;
            this.sorted = sorted;
        }

        public String algorithm() { return algorithm; }
        public long averageNanos() { return averageNanos; }
        public double averageMillis() { return averageNanos / 1_000_000.0; }
        @SuppressWarnings("unchecked")
        public <T> List<T> sorted() { return (List<T>) sorted; }
    }
}
