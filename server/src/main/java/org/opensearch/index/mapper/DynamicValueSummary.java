/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.index.mapper;

import org.opensearch.common.annotation.ExperimentalApi;

/**
 * A small, generic classification of an unmapped field's buffered value, computed once by core and
 * handed to {@link DynamicFieldTypeInferencer}s so they do not each re-stream the bytes to answer
 * "what shape is this."
 *
 * <p>This is the array analog of core's scalar token classification: core states a JSON fact
 * ("flat numeric array of length N", "array of objects", "scalar", ...) and lets plugins dispatch on
 * it. Core never interprets the shape as a plugin type — it does not know what a "vector" is.
 *
 * @opensearch.experimental
 */
@ExperimentalApi
public class DynamicValueSummary {

    /**
     * The coarse JSON shape of the field value.
     */
    @ExperimentalApi
    public enum ValueShape {
        /** The value is an array whose every element is a JSON number. */
        FLAT_NUMERIC_ARRAY,
        /** The value is an array containing at least one non-number element (string, object, nested array, ...). */
        NON_NUMERIC_ARRAY,
        /** The value is a JSON object. */
        OBJECT,
        /** The value is a scalar (string, number, boolean, null, embedded object). */
        SCALAR
    }

    private final ValueShape shape;
    private final int arrayLength;

    private DynamicValueSummary(ValueShape shape, int arrayLength) {
        this.shape = shape;
        this.arrayLength = arrayLength;
    }

    /** A flat numeric array of the given length. */
    public static DynamicValueSummary flatNumericArray(int length) {
        return new DynamicValueSummary(ValueShape.FLAT_NUMERIC_ARRAY, length);
    }

    /** An array containing at least one non-number element; {@code length} is the element count. */
    public static DynamicValueSummary nonNumericArray(int length) {
        return new DynamicValueSummary(ValueShape.NON_NUMERIC_ARRAY, length);
    }

    /** The value is a JSON object. */
    public static DynamicValueSummary object() {
        return new DynamicValueSummary(ValueShape.OBJECT, -1);
    }

    /** The value is a scalar. */
    public static DynamicValueSummary scalar() {
        return new DynamicValueSummary(ValueShape.SCALAR, -1);
    }

    public ValueShape shape() {
        return shape;
    }

    /** True iff the value is an array whose every element is a JSON number. */
    public boolean isFlatNumericArray() {
        return shape == ValueShape.FLAT_NUMERIC_ARRAY;
    }

    /** The array element count, or {@code -1} when the value is not an array. */
    public int arrayLength() {
        return arrayLength;
    }
}
