package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import com.google.protobuf.Message;
import edu.kit.ipd.crowdcontrol.objectservice.rest.Paginated;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a range containing the elements and whether there are more elements left / right of
 * the range.
 *
 * @param <T>
 *         Type of the data.
 * @param <X>
 *         Type of the key.
 *
 * @author Leander K.
 * @author Niklas Keller
 */
public class Range<T, X> {
    private final List<T> data;
    private final boolean hasPredecessors;
    private final boolean hasSuccessors;
    private final X left;
    private final X right;
    private static Range rangeFF = new Range<>(Collections.emptyList(), null, null, false, false);
    private static Range rangeTT = new Range<>(Collections.emptyList(), null, null, true, true);
    private static Range rangeTF = new Range<>(Collections.emptyList(), null, null, true, false);
    private static Range rangeFT = new Range<>(Collections.emptyList(), null, null, false, true);

    /**
     * @param data the data of the range
     * @param left the key of the most left element inside the range
     * @param right the key of the most right element inside the range
     * @param hasPredecessors whether there are elements left of the range
     * @param hasSuccessors whether there are elements right of the range
     */
    private Range(List<T> data, X left, X right, boolean hasPredecessors, boolean hasSuccessors) {
        this.data = data;
        this.left = left;
        this.right = right;
        this.hasPredecessors = hasPredecessors;
        this.hasSuccessors = hasSuccessors;
    }

    /**
     * gets a Range
     * @param data the data of the range
     * @param left the key of the most left element inside the range
     * @param right the key of the most right element inside the range
     * @param hasPredecessors whether there are elements left of the range
     * @param hasSuccessors whether there are elements right of the range
     * @param <A> the type of the data
     * @param <B> the type of the key
     * @return an instance of Range
     */
    public static <A, B> Range<A, B> of(List<A> data, B left, B right, boolean hasPredecessors, boolean hasSuccessors) {
        if (data.isEmpty()) {
            return getEmptyRange(hasPredecessors, hasSuccessors);
        }
        return new Range<>(data, left, right, hasPredecessors, hasSuccessors);
    }

    /**
     * constructs an empty Range
     * @param <A> the type of the data
     * @param <B> the type of the key
     * @return an empty Range
     */
    @SuppressWarnings("unchecked")
    public static <A, B> Range<A, B> getEmptyRange(boolean hasPredecessors, boolean hasSuccessors) {
        Range range;
        if (hasPredecessors && hasSuccessors) {
            range = rangeTT;
        } else if (hasPredecessors) {
            range = rangeTF;
        } else if (hasSuccessors) {
            range = rangeFT;
        } else {
            range = rangeFF;
        }
        return (Range<A, B>) range;
    }

    /**
     * the data in the Range
     *
     * @return a List filled with the data inside the range
     */
    public List<T> getData() {
        return data;
    }

    /**
     * Maps the data inside the range.
     *
     * @param mapping
     *         Mapping to apply.
     * @param <Y>
     *         Type to map to.
     *
     * @return Range with applied mapping.
     */
    @SuppressWarnings("unchecked")
    public <Y> Range<Y, X> map(Function<T, Y> mapping) {
        if (data.isEmpty())
            return (Range<Y, X>) this;
        List<Y> newList = data.stream()
                .map(mapping)
                .collect(Collectors.toList());

        return new Range<>(newList, left, right, hasPredecessors, hasSuccessors);
    }

    /**
     * Maps the data inside the range.
     * This method does not get called when the range is empty.
     *
     * @param mapping
     *         Mapping to apply.
     * @param <Y>
     *         Type to map to.
     *
     * @return Range with applied mapping.
     */
    @SuppressWarnings("unchecked")
    public <Y> Range<Y, X> mapList(Function<List<T>, List<Y>> mapping) {
        if (data.isEmpty())
            return (Range<Y, X>) this;
        List<Y> newList = mapping.apply(data);

        return new Range<>(newList, left, right, hasPredecessors, hasSuccessors);
    }

    /**
     * Key of the most left data element inside the range.
     *
     * @return Key, or empty if the list is empty.
     */
    public Optional<X> getLeft() {
        return Optional.ofNullable(left);
    }

    /**
     * Key of the most right data element inside the range.
     *
     * @return Key, or empty if the list is empty.
     */
    public Optional<X> getRight() {
        return Optional.ofNullable(right);
    }

    /**
     * Whether there are elements left of the range.
     *
     * @return {@code true}, if there are elements left of the range.
     */
    public boolean hasPredecessors() {
        return hasPredecessors;
    }

    /**
     * Whether there are elements right of the range.
     *
     * @return {@code true}, if there are elements right of the range.
     */
    public boolean hasSuccessors() {
        return hasSuccessors;
    }

    /**
     * constructs an instance of Paginated out of the range if the range is existing (not empty)
     * @param builder the builder to use
     * @param merge the function to merge the data into the builder
     * @param <R> the type of the builder
     * @return an instance Paginated
     */
    public <R extends Message.Builder> Paginated<X> constructPaginated(R builder, BiFunction<R, List<T>, R> merge) {
        Message message = merge.apply(builder, data).build();
        return new Paginated<>(message, left, right, hasPredecessors, hasSuccessors);
    }
}
