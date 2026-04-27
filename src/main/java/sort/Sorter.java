package sort;

import java.util.Comparator;
import java.util.List;

/**
 * Common abstraction for the three sorting algorithms required by Task A.
 *
 * <p>Accepting an arbitrary {@link Comparator} keeps the algorithms generic;
 * the project-level ranking rule is supplied by the caller (e.g.
 * {@code model.Candidate.RANKING}). This is the polymorphism example cited in
 * the Task C design discussion.
 *
 * <p>Implementations sort the given list in place.
 */
public interface Sorter {

    /** Sort {@code list} in place using {@code comparator}. */
    <T> void sort(List<T> list, Comparator<? super T> comparator);

    /** Human-readable name used in reports (e.g. {@code "Bubble Sort"}). */
    String name();
}
