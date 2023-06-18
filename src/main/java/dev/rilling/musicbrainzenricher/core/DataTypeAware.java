package dev.rilling.musicbrainzenricher.core;

import org.jetbrains.annotations.NotNull;

/**
 * Base for a class supporting a {@link DataType}.
 */
public interface DataTypeAware {
	/**
	 * Checks which data type is supported by this class.
	 *
	 * @return The data type that is supported.
	 */
	@NotNull DataType getDataType();
}
