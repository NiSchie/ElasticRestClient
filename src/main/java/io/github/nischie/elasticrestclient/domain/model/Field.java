package io.github.nischie.elasticrestclient.domain.model;

/**
 * Represents a field in an Elasticsearch document.
 *
 * @param fieldName the name of the field
 */
public record Field(String fieldName) {
    /**
     * Creates a new Field instance with the specified field name.
     *
     * @param fieldName the name of the field
     * @return a new Field instance
     */
    public static Field of(String fieldName) {
        return new Field(fieldName);
    }
}
