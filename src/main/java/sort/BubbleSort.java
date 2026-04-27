package sort;

import java.util.Comparator;
import java.util.List;

public final class BubbleSort implements Sorter {

    @Override
    public <T> void sort(List<T> list, Comparator<? super T> comparator) {
        // robust programming, available to any Object list that is comparable
        int n = list.size();

        for (int k = 1; k < n; k++) {
            // initialize the swapped tag (boolean varaible)
            boolean swapped = false;

            for (int i = 0; i < n - k; i++) {
                // get the current and next
                T current = list.get(i);
                T next = list.get(i + 1);
                // compare current and next
                if (comparator.compare(current, next) > 0) {
                    list.set(i, next);
                    list.set(i + 1, current);
                    swapped = true;
                }
            }
            // an imporvement by Liang, if there doesn't exist a single swap in a loop,
            // then it is already sorted
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
