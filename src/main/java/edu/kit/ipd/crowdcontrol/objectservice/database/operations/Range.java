package edu.kit.ipd.crowdcontrol.objectservice.database.operations;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * represents a range containing the elements and whether there are next/prev results.
 * @author LeanderK
 * @version 1.0
 */
public class Range<T> {
    private final List<T> data;
    private final boolean hasMore;

    public Range(List<T> data, boolean hasMore) {
        this.data = data;
        this.hasMore = hasMore;
    }

    public List<T> getData() {
        return data;
    }

    public <X> Range<X> map(Function<T, X> mapping) {
        List<X> newList = data.stream()
                .map(mapping)
                .collect(Collectors.toList());
        return new Range<>(newList, hasMore);
    }

    public boolean hasMore() {
        return hasMore;
    }
}
