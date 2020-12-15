package org.felixrilling.musicbrainzenricher.enrichment;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;

/**
 * Base for a class supporting a {@link DataType}.
 */
interface DataTypeAware {
    /**
     * Checks which data type is supported by this class.
     *
     * @return The data type that is supported.
     */
    @NotNull DataType getDataType();
}
