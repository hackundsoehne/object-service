package edu.kit.ipd.crowdcontrol.objectservice.rest;

import com.google.protobuf.Message;

import java.util.Optional;

/**
 * Represents a page containing the items and whether there's a next / previous page.
 *
 * @param <X> type of the cursor
 *
 * @author Niklas Keller
 */
public class Paginated<X> {
    private final Message message;
    private final boolean hasPrevious;
    private final boolean hasNext;
    private final X left;
    private final X right;

    /**
     * @param message     paginated message
     * @param left        key of the most left element inside the page
     * @param right       key of the most right element inside the page
     * @param hasPrevious whether there's a previous page
     * @param hasNext     whether there's a next page
     */
    public Paginated(Message message, X left, X right, boolean hasPrevious, boolean hasNext) {
        this.message = message;
        this.left = left;
        this.right = right;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
    }

    /**
     * @return Paginated message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Key of the most left data element inside the page.
     *
     * @return Key, or empty if the list is empty.
     */
    public Optional<X> getLeft() {
        return Optional.ofNullable(left);
    }

    /**
     * Key of the most right data element inside the page.
     *
     * @return Key, or empty if the list is empty.
     */
    public Optional<X> getRight() {
        return Optional.ofNullable(right);
    }

    /**
     * Whether there's a previous page.
     *
     * @return {@code true}, if there's a previous page.
     */
    public boolean hasPrevious() {
        return hasPrevious;
    }

    /**
     * Whether there's a next page.
     *
     * @return {@code true}, if there's a next page.
     */
    public boolean hasNext() {
        return hasNext;
    }
}
