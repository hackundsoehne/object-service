package edu.kit.ipd.crowdcontrol.objectservice.database.transforms;

import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;

import java.util.function.BiConsumer;

/**
 * Contains various helper-methods to deal with the transformations
 * @author LeanderK
 * @version 1.0
 */
public abstract class AbstractTransform {
    /**
     * merges the MessageOrBuilder Y with X. Only merges the set fields
     * @param x the x to return
     * @param y the MessageOrBuilder
     * @param combine the combine operation, takes the field-number and the x
     * @param <X> the type to return
     * @param <Y> the MessageOrBuilder
     * @return x
     */
    protected static  <X, Y extends MessageOrBuilder> X merge(X x, Y y, BiConsumer<Integer, X> combine) {
        y.getDescriptorForType().getFields().stream()
                .filter(y::hasField)
                .map(Descriptors.FieldDescriptor::getNumber)
                .forEach(number -> combine.accept(number, x));
        return x;
    }
}
