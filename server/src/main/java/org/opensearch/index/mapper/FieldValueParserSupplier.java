/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.index.mapper;

import org.opensearch.common.annotation.ExperimentalApi;
import org.opensearch.core.xcontent.XContentParser;

import java.io.IOException;

/**
 * Supplies a fresh {@link XContentParser} positioned at the start of a buffered field value.
 *
 * <p>Handed to dynamic-mapping plugin extension points ({@link DynamicFieldTypeInferencer} and
 * {@link DynamicTemplateTypeHandler}) so they can inspect an unmapped field's value without core
 * committing to any deserialized representation. Each call to {@link #get()} returns an independent
 * parser over the same buffered bytes, so a plugin may read the value more than once. Creating a
 * parser can fail with {@link IOException}, which callers propagate.
 *
 * <p>Plugins whose configuration is already complete need not call {@link #get()} at all.
 *
 * @opensearch.experimental
 */
@ExperimentalApi
@FunctionalInterface
public interface FieldValueParserSupplier {

    /**
     * Returns a fresh {@link XContentParser} positioned at the first token of the field value. The
     * caller is responsible for closing it (e.g. via try-with-resources).
     *
     * @throws IOException if the parser cannot be created
     */
    XContentParser get() throws IOException;
}
