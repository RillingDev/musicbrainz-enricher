package dev.rilling.musicbrainzenricher.core;

/**
 * Base for a class supporting a {@link DataType}.
 */
public interface DataTypeAware {
	/**
	 * Checks which data type is supported by this class.
	 *
	 * @return The data type that is supported.
	 */
	 DataType getDataType();
}
