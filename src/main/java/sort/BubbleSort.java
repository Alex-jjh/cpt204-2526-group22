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
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                T a = list.get(j);
                T b = list.get(j + 1);
                if (comparator.compare(a, b) > 0) {
                    list.set(j, b);
                    list.set(j + 1, a);
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
