package org.felixrilling.musicbrainzenricher.enrichment;

import org.felixrilling.musicbrainzenricher.DataType;
import org.jetbrains.annotations.NotNull;

/**
 * Base for a class supporting a {@link DataType}.
 */
interface DataTypeAware {
    /**
     * Checks if the given data type is supported by this class.
     *
     * @param dataType Data type to check.
     * @return if the data type is supported.
     */
    boolean dataTypeSupported(@NotNull DataType dataType);
}
