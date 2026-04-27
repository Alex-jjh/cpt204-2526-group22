package sort;

import java.util.Comparator;
import java.util.List;

/**
 * In-place quick sort using Lomuto partition with a median-of-three pivot.
 * The median-of-three choice reduces the chance of hitting O(n^2) on
 * already-sorted or reverse-sorted input — a point worth discussing in
 * Task A's analysis.
 */
public final class QuickSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        if (list.size() < 2) return;
        quicksort(list, 0, list.size() - 1, comparator);
    }

    private <T> void quicksort(List<T> list, int lo, int hi, Comparator<? super T> cmp) {
        if (lo >= hi) return;
        int p = partition(list, lo, hi, cmp);
        quicksort(list, lo, p - 1, cmp);
        quicksort(list, p + 1, hi, cmp);
    }

    private <T> int partition(List<T> list, int lo, int hi, Comparator<? super T> cmp) {
        int mid = lo + (hi - lo) / 2;
        medianOfThree(list, lo, mid, hi, cmp);
        T pivot = list.get(hi);
        int i = lo - 1;
        for (int j = lo; j < hi; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, hi);
        return i + 1;
    }

    /** Arrange list[lo], list[mid], list[hi] so the median ends up at hi (used as pivot). */
    private <T> void medianOfThree(List<T> list, int lo, int mid, int hi, Comparator<? super T> cmp) {
        if (cmp.compare(list.get(lo), list.get(mid)) > 0) swap(list, lo, mid);
        if (cmp.compare(list.get(lo), list.get(hi)) > 0) swap(list, lo, hi);
        if (cmp.compare(list.get(mid), list.get(hi)) > 0) swap(list, mid, hi);
        // now list[mid] is the median; move it to hi to use as pivot
        swap(list, mid, hi);
    }

    private <T> void swap(List<T> list, int i, int j) {
        if (i == j) return;
        T tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }

    @Override
    public String name() {
        return "Quick Sort";
    }
}
