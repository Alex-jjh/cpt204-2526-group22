package sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Top-down merge sort. Stable and O(n log n) in all cases, at the cost of
 * O(n) auxiliary memory — useful talking point for Task A's runtime-vs-memory
 * discussion.
 */
public final class MergeSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        int n = list.size();
        if (n < 2) return;
        // work on a mutable array-backed buffer to keep O(n) extra, not O(n log n)
        List<T> aux = new ArrayList<>(list);
        mergesort(list, aux, 0, n - 1, comparator);
    }

    private <T> void mergesort(List<T> src, List<T> aux, int lo, int hi, Comparator<? super T> cmp) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergesort(src, aux, lo, mid, cmp);
        mergesort(src, aux, mid + 1, hi, cmp);
        merge(src, aux, lo, mid, hi, cmp);
    }

    private <T> void merge(List<T> src, List<T> aux, int lo, int mid, int hi, Comparator<? super T> cmp) {
        for (int k = lo; k <= hi; k++) {
            aux.set(k, src.get(k));
        }
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                src.set(k, aux.get(j++));
            } else if (j > hi) {
                src.set(k, aux.get(i++));
            } else if (cmp.compare(aux.get(i), aux.get(j)) <= 0) {
                src.set(k, aux.get(i++));
            } else {
                src.set(k, aux.get(j++));
            }
        }
    }

    @Override
    public String name() {
        return "Merge Sort";
    }
}
