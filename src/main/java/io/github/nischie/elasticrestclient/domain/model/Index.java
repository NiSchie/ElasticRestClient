package io.github.nischie.elasticrestclient.domain.model;

/**
 * Represents an Elasticsearch index.
 *
 * @param _index the name of the index
 */
public record Index(String _index) {
    /**
     * Creates an Index instance from the given index name.
     *
     * @param index the name of the index
     * @return a new Index instance
     */
    public static Index of(String index) {
        return new Index(index);
    }
}
