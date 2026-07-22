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
import java.util.Map;

/**
 * Handler for a plugin type behind a dynamic template with {@code match_mapping_type: "array"}.
 * Core detects only that an unmapped field's value is an array; each registered handler decides
 * whether the array is actually one of its types and, if so, completes the mapping configuration
 * before the mapper is built.
 *
 * <p>Rather than deserializing the field value for the handler, core hands it a
 * {@link FieldValueParserSupplier} that produces a fresh {@link XContentParser} over the buffered
 * bytes. A handler whose template config is already complete never calls {@code get()},
 * so no parsing happens for fully-specified templates. A handler that needs a
 * data-derived parameter (e.g. a vector's dimension) creates a parser and reads what it
 * needs.
 *
 * @opensearch.experimental
 */
@ExperimentalApi
public interface DynamicTemplateTypeHandler {

    /**
     * Given a matched {@code match_mapping_type: "array"} template, decide whether this handler
     * claims the field and, if so, complete the mapping configuration before the TypeParser builds
     * the mapper. Called at the single convergence point in document parsing where the field name,
     * value, and parser position are all known.
     *
     * <p>Returning {@code false} declines the field: core offers it to the next registered handler
     * and, if none claim it, falls back to the normal element-wise array parsing. A handler that
     * claims the field must inject its own {@code type} (and any data-derived parameter such as a
     * vector's dimension) into {@code mappingConfig}.
     *
     * @param mappingConfig the mutable mapping config from the template (modified in place)
     * @param fieldValueParser produces a fresh {@link XContentParser} over the buffered field bytes;
     *                      only call {@code get()} if the config is missing a parameter that must
     *                      be derived from the data. Close the returned parser (e.g. via
     *                      try-with-resources).
     * @return {@code true} if this handler claims the field, {@code false} to decline it
     * @throws IOException if reading from the parser fails
     */
    boolean adjustMappingConfig(Map<String, Object> mappingConfig, FieldValueParserSupplier fieldValueParser) throws IOException;

    /**
     * Returns {@code true} if the given template mapping config is fully specified — i.e. building a
     * mapper from it requires no parameter derived from a document ({@link #adjustMappingConfig} would
     * not need to read the field value). When this returns {@code true}, core validates the template
     * eagerly at index-creation time by handing the config to the {@link Mapper.TypeParser}, which
     * reports any invalid content. When it returns {@code false}, validation is deferred to
     * document-parse time, where the data-derived parameters become available.
     *
     * @param mappingConfig the mapping config from the matched template
     * @return whether the config can be validated without inspecting a document
     */
    default boolean isConfigComplete(Map<String, Object> mappingConfig) {
        return false;
    }
}
