package model;

import java.util.Comparator;
import java.util.Objects;

/**
 * Immutable record of a candidate inspection location.
 *
 * <p>Fields map directly to the columns of {@code candidates_*.csv}:
 * {@code location_id} and {@code priority_score}.
 */
public final class Candidate {

    private final String locationId;
    private final int priorityScore;

    public Candidate(String locationId, int priorityScore) {
        this.locationId = Objects.requireNonNull(locationId, "locationId");
        this.priorityScore = priorityScore;
    }

    public String getLocationId() {
        return locationId;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    /**
     * Ranking rule required by Task A:
     * priorityScore descending, then locationId ascending.
     */
    public static final Comparator<Candidate> RANKING =
            Comparator.comparingInt(Candidate::getPriorityScore).reversed()
                    .thenComparing(Candidate::getLocationId);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Candidate other)) return false;
        return priorityScore == other.priorityScore
                && locationId.equals(other.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, priorityScore);
    }

    @Override
    public String toString() {
        return locationId + "(" + priorityScore + ")";
    }
}
