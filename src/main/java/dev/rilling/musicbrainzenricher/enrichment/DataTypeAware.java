package dev.rilling.musicbrainzenricher.enrichment;

import dev.rilling.musicbrainzenricher.core.DataType;
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
