package io.github.nischie.elasticrestclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Main client for interacting with Elasticsearch.
 * <p>
 * Provides access to document, index, and bulk operations via sub-clients.
 * Handles authentication and configuration of the underlying RestClient.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class ElasticRestClient {
    private final RestClient restClient;
    private final IndexClient indexClient;
    private final BulkClient bulkClient;
    private final DocumentClient documentClient;

    /**
     * Constructs an ElasticRestClient with the given host, username, and password.
     *
     * @param host the Elasticsearch host URL
     * @param username the username for authentication
     * @param password the password for authentication
     */
    public ElasticRestClient(String host, String username, String password) {
        String baseUrl = host +"/";

        restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(
                        headers -> {
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.setBasicAuth(username, password);
                        })
                .build();
        this.indexClient = new IndexClient(restClient);
        this.bulkClient = new BulkClient(restClient);
        this.documentClient = new DocumentClient(restClient);
    }
    /**
     * Returns the DocumentClient for document operations.
     *
     * @return the DocumentClient instance
     */
    public DocumentClient document() {
        return documentClient;
    }
    /**
     * Returns the IndexClient for index operations.
     *
     * @return the IndexClient instance
     */
    public IndexClient index() {
        return indexClient;
    }
    /**
     * Returns the BulkClient for bulk operations.
     *
     * @return the BulkClient instance
     */
    public BulkClient bulk() {
        return bulkClient;
    }
    /**
     * Creates a ScrollableSearch for paginated search results using Elasticsearch's scroll API.
     *
     * @param index the index to search in
     * @param query the search query
     * @param pageSize the number of results per page
     * @return a new ScrollableSearch instance
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public ScrollableSearch scrollSearch(Index index, StringSearchQuery query, int pageSize) throws JsonProcessingException {
        return new ScrollableSearch(restClient, index, query, pageSize);
    }
}
