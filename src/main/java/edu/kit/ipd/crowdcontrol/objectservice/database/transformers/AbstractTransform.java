package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.MessageOrBuilder;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
                .filter(field -> field.isRepeated() || y.hasField(field))
                .map(Descriptors.FieldDescriptor::getNumber)
                .forEach(number -> combine.accept(number, x));
        return x;
    }

    protected static <X extends GeneratedMessage.Builder<X>> BuilderHelper<X> builder(X x) {
        return new BuilderHelper<>(x);
    }

    protected static class BuilderHelper <B extends GeneratedMessage.Builder<B>> {
        private B b;

        public BuilderHelper(B b) {
            this.b = b;
        }

        public <X> BuilderHelper<B> set(X x, BiFunction<B, X, B> merge) {
            if (x != null) {
                b = merge.apply(b, x);
            }
            return this;
        }

        public B getBuilder() {
            return b;
        }
    }
}
