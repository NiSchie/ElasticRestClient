package io.github.nischie.elasticrestclient.client;

import io.github.nischie.elasticrestclient.domain.model.Index;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Client for index operations in Elasticsearch.
 * <p>
 * Provides methods for creating, deleting, and managing indices.
 * Uses Spring's RestClient for HTTP communication.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class IndexClient {
    private final RestClient restClient;
    /**
     * Constructs an IndexClient with the given RestClient.
     *
     * @param restClient the RestClient to use for HTTP operations
     */
    public IndexClient(RestClient restClient) {
        this.restClient = restClient;
    }
    /**
     * Creates an index with the specified name and optional mapping.
     *
     * @param index the index to create
     * @return the response specification from the RestClient
     */
    public RestClient.ResponseSpec createIndex(Index index) {
        return restClient.put()
                .uri(index._index())
                .retrieve();
    }
    /**
     * Deletes the specified index.
     *
     * @param index the index to delete
     * @return the response specification from the RestClient
     */
    public RestClient.ResponseSpec deleteIndex(Index index) {
        return restClient.delete()
                .uri(index._index())
                .retrieve();
    }
    /**
     * Checks if the specified index exists.
     *
     * @param index the index to check
     * @return true if the index exists, false otherwise
     */
    public boolean indexExists(Index index) {
        try {
            restClient.head()
                    .uri(index._index())
                    .retrieve()
                    .body(Void.class);
            return true; // Index exists
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return false; // Index does not exist
            }
            throw new RuntimeException(e); // Other error
        }
    }

    /**
     * Adds an alias to the specified index.
     *
     * @param index the index to add the alias to
     * @param aliasName the name of the alias to add
     * @return the response specification from the RestClient
     */
    public RestClient.ResponseSpec alias(Index index, String aliasName) {
        return restClient.post()
                .uri(index._index() + "/_alias/" + aliasName)
                .retrieve();
    }

    /**
     * Returns the aliases for the specified index.
     *
     * @param index the index to remove the alias from
     * @return a map of alias names to their properties
     */
    public Map<String, Object> getAliases(Index index) {
        Map<String, Object> map = restClient.get()
                .uri(index._index() + "/_alias")
                .retrieve()
                .body(Map.class)
                ;
        Map<String, Object> aliasMap = (Map<String, Object>) map.get(index._index());
        Map<String, Object> aliases = (Map<String, Object>) aliasMap.get("aliases");
        return aliases;
    }
}
