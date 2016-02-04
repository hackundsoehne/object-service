package edu.kit.ipd.crowdcontrol.objectservice.database.transformers;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.MessageOrBuilder;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Contains various helper-methods to deal with the transformations
 *
 * @author LeanderK
 * @version 1.0
 */
public abstract class AbstractTransformer {
    /**
     * Converts an integer to it's protocol buffer encapsulation.
     *
     * @param value integer value to convert
     *
     * @return Encoded integer.
     */
    protected static edu.kit.ipd.crowdcontrol.objectservice.proto.Integer toInteger(int value) {
        return edu.kit.ipd.crowdcontrol.objectservice.proto.Integer.newBuilder().setValue(value).build();
    }

    /**
     * merges the MessageOrBuilder Y with X. Only merges the set fields
     *
     * @param x       the x to return
     * @param y       the MessageOrBuilder
     * @param combine the combine operation, takes the field-number and the x
     * @param <X>     the type to return
     * @param <Y>     the MessageOrBuilder
     *
     * @return x
     */
    protected static <X, Y extends MessageOrBuilder> X merge(X x, Y y, BiConsumer<Integer, X> combine) {
        y.getDescriptorForType().getFields().stream()
                .filter(field -> field.isRepeated() || y.hasField(field))
                .map(Descriptors.FieldDescriptor::getNumber)
                .forEach(number -> combine.accept(number, x));
        return x;
    }

    /**
     * constructs a builder-helper used when your data to merge into may be null.
     *
     * @param x   the builder
     * @param <X> the type of the builder
     *
     * @return the builder wrapped in a helper
     */
    protected static <X extends GeneratedMessage.Builder<X>> BuilderHelper<X> builder(X x) {
        return new BuilderHelper<>(x);
    }

    /**
     * helps constructing a message if your data may be null
     *
     * @param <B> the type of the builder
     */
    protected static class BuilderHelper<B extends GeneratedMessage.Builder<B>> {
        private B b;

        /**
         * creates a new instance of BuilderHelper
         *
         * @param b the builder to use
         */
        public BuilderHelper(B b) {
            this.b = b;
        }

        /**
         * executes the merge-function if the passed x is not null
         *
         * @param x     the data to set
         * @param merge the merge-function
         * @param <X>   the type of the data
         *
         * @return this, used for chaining
         */
        public <X> BuilderHelper<B> set(X x, BiFunction<B, X, B> merge) {
            if (x != null) {
                b = merge.apply(b, x);
            }
            return this;
        }

        /**
         * returns the resulting builder
         *
         * @return the resulting builder
         */
        public B getBuilder() {
            return b;
        }
    }
}
