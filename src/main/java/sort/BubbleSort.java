package sort;

import java.util.Comparator;
import java.util.List;

/**
 * Classic optimized bubble sort. Includes the early-exit flag so a sorted
 * input is detected in one O(n) pass — relevant for Task A's analysis of
 * how input order affects runtime.
 */
public final class BubbleSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        int n = list.size();
        for (int k = 1; k < n; k++) {
            boolean swapped = false;
            for (int i = 0; i < n - k; i++) {
                T current = list.get(i);
                T next = list.get(i + 1);
                if (comparator.compare(current, next) > 0) {
                    list.set(i, next);
                    list.set(i + 1, current);
                    swapped = true;
                }
            }
            if (!swapped) {
                return;
            }
        }
    }

    @Override
    public String name() {
        return "Bubble Sort";
    }
}
