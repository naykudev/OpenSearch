/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

package org.opensearch.index.mapper;

import org.opensearch.common.annotation.ExperimentalApi;
import org.opensearch.index.IndexSettings;

import java.util.Map;

/**
 * Extension point allowing plugins to infer a specific field type from a numeric array
 * encountered during dynamic mapping of a previously unmapped field.
 *
 * @opensearch.experimental
 */
@ExperimentalApi
public interface DynamicArrayFieldTypeInferencer {

    /**
     * Inspect array metadata and decide if this inferencer claims the field.
     *
     * @param fieldName       the field name
     * @param arrayLength     number of elements in the array
     * @param isNumericArray  true if the first element is a numeric value
     * @param indexSettings   the index settings
     * @return the mapper field type name (e.g., "knn_vector") if claimed, or null to pass
     */
    String inferType(String fieldName, int arrayLength, boolean isNumericArray, IndexSettings indexSettings);

    /**
     * After a type is claimed, produce the configuration map for the Mapper.TypeParser.
     *
     * @param fieldName   the field name
     * @param arrayLength number of elements
     * @return configuration map (e.g., {"type": "knn_vector", "dimension": 128})
     */
    Map<String, Object> getFieldConfiguration(String fieldName, int arrayLength);
}
