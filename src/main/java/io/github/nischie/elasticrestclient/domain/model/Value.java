package io.github.nischie.elasticrestclient.domain.model;

/**
 * Represents a value in the domain model.
 *
 * @param value the string representation of the value
 */
public record Value(String value) {
    /**
     * Creates a new Value instance using the given string representation.
     *
     * @param value the string representation of the value
     * @return a new Value instance
     */
    public static Value of(String value) {
        return new Value(value);
    }
}
