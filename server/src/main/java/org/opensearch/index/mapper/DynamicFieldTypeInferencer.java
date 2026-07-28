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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * SPI for plugins to register dynamic field type inference logic.
 *
 * <p>When DocumentParser encounters an unmapped field and no dynamic template matches, it buffers
 * the field content and calls {@link #inferFieldType} on each registered inferencer in order. The
 * first non-null config map wins; the type is read from the {@code "type"} key, the mapper is built,
 * and the field content is replayed through it. If no inferencer claims the field, existing fallback
 * behavior applies.
 *
 * <p>Core classifies the buffered value once (see {@link DynamicValueSummary}) and passes that summary
 * so the inferencer does not re-stream the bytes just to answer "what shape is this." For anything the
 * summary doesn't cover, core also hands a {@link FieldValueParserSupplier} that produces a fresh
 * {@link XContentParser} over the buffered bytes. Each call to {@code fieldValueParser.get()} returns an
 * independent parser positioned before the field value, so the plugin can still inspect the raw content
 * however it needs. This keeps core free of any plugin-type contract: core states a JSON fact, each
 * plugin decides how to interpret it.
 *
 * @opensearch.experimental
 */
@ExperimentalApi
public interface DynamicFieldTypeInferencer {

    /**
     * Inspect the buffered field value and decide whether to claim it.
     *
     * @param summary core's classification of the value shape (e.g. flat numeric array of length N);
     *                lets the plugin dispatch without re-streaming the bytes
     * @param fieldValueParser produces a fresh {@link XContentParser} over the buffered field bytes for
     *                      any inspection the summary doesn't cover; call {@code get()} and advance the
     *                      parser to read the value. The returned parser should be closed by the caller
     *                      (e.g. via try-with-resources).
     * @return a mutable mapping config map with at minimum a {@code "type"} key (e.g.
     *         {@code {"type": "knn_vector", "dimension": 384}}), or {@code null} to pass to the
     *         next inferencer. The map MUST be mutable — TypeParser implementations call
     *         {@code node.remove()} on it during parsing.
     * @throws IOException if reading from the parser fails
     */
    Map<String, Object> inferFieldType(DynamicValueSummary summary, FieldValueParserSupplier fieldValueParser) throws IOException;

    /**
     * Value shapes this inferencer <em>exclusively reserves</em>. A reserved shape is owned by this
     * inferencer alone: core will offer a value of that shape only to this inferencer, never to any
     * other plugin's inferencer. If the reserving inferencer declines (returns {@code null}, e.g. the
     * value is below its own threshold), the value falls through to core's built-in handling — it is
     * never handed to a different plugin.
     *
     * <p>Two plugins reserving the same shape is a misconfiguration: core rejects it at node startup.
     *
     * <p>Reservation does <b>not</b> claim the shape away from core defaults or explicit user mappings;
     * it only excludes <em>other plugins</em>. Returning an empty set (the default) means this inferencer
     * competes for values on a first-non-null basis with no exclusivity.
     *
     * @return the shapes exclusively owned by this inferencer; empty if none
     */
    default Set<DynamicValueSummary.ValueShape> reservedShapes() {
        return Collections.emptySet();
    }
}
