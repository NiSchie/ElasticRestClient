package io.github.nischie.elasticrestclient.domain.model;

import java.util.UUID;

/**
 * Represents a document ID in Elasticsearch.
 * <p>
 * Wraps a string value and provides a constructor for UUIDs.
 *
 * @param _id the string representation of the document ID
 */
public record Id(String _id) {
    /**
     * Constructs an Id from a UUID.
     *
     * @param uuid the UUID to convert to a string ID
     */
    public Id(UUID uuid) {
        this(uuid.toString());
    }

    /**
     * Creates a new Id instance from the given string value.
     *
     * @param id the string value for the document ID
     * @return a new Id instance
     */
    public static Id of(String id) {
        return new Id(id);
    }
}
