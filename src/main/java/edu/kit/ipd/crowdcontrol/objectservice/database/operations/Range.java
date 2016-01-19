package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * represents a range containing the elements and whether there are more elements left/right of the range.
 * @author LeanderK
 * @version 1.0
 * @param <T> the type of the data
 * @param <X> the type of the key
 */
public class Range<T, X> {
    private final List<T> data;
    private final boolean existsLeft;
    private final boolean existsRight;
    private final X limitLeft;
    private final X limitRight;

    /**
     * creates a new instance of range
     * @param data the data of the range
     * @param limitLeft the key of the most left element inside the range
     * @param limitRight the key of the most right element inside the range
     * @param existsLeft whether there are elements left of the range
     * @param existsRight whether there are elements right of the range
     */
    public Range(List<T> data, X limitLeft, X limitRight, boolean existsLeft, boolean existsRight) {
        this.data = data;
        this.limitLeft = limitLeft;
        this.limitRight = limitRight;
        this.existsLeft = existsLeft;
        this.existsRight = existsRight;
    }

    /**
     * the data in the Range
     * @return a List filled with the data inside the range
     */
    public List<T> getData() {
        return data;
    }

    /**
     * maps the data inside the range
     * @param mapping the mapping to apply
     * @param <Y> the type to map to
     * @return a range where the mapping is applied
     */
    public <Y> Range<Y, X> map(Function<T, Y> mapping) {
        List<Y> newList = data.stream()
                .map(mapping)
                .collect(Collectors.toList());
        return new Range<>(newList, limitLeft, limitRight, existsLeft, existsRight);
    }

    /**
     * the key of the most left data-element inside the range
     * @return the key, or empty if the list is empty
     */
    public Optional<X> getLimitLeft() {
        return Optional.ofNullable(limitLeft);
    }

    /**
     * the key of the most right data-element inside the range
     * @return the key, or empty if the list is empty
     */
    public Optional<X> getLimitRight() {
        return Optional.ofNullable(limitRight);
    }

    /**
     * whether there are elements right of the range
     * @return true if there are elements left of the range
     */
    public boolean existsElementsLeft() {
        return existsLeft;
    }

    /**
     * whether there are elements left of the range
     * @return true if there are elements right of the range
     */
    public boolean existsElementsRight() {
        return existsRight;
    }
}
