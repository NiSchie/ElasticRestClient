package io.github.nischie.elasticrestclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.domain.model.Id;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for bulk operations in Elasticsearch.
 * <p>
 * Supports bulk indexing, updating, and deleting documents using the Elasticsearch Bulk API.
 * Uses Spring's RestClient for HTTP communication and JsonUtil for serialization.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class BulkClient {
    private final RestClient restClient;
    private final String indexRequest = """
            { "index": { "_index": "%s", "_id": "%s" } }
            %s
            """;
    private final String updateRequest = """
            { "update": { "_index": "%s", "_id": "%s" } }
            { "doc": %s }
            """;
    private final String deleteRequest = """
            { "delete": { "_index": "%s", "_id": "%s" } }
            """;

    private List<String> bulkRequests = new ArrayList<>();
    private boolean bulkAutoCommit = false;

    /**
     * Constructs a BulkClient with the given RestClient.
     *
     * @param restclient the RestClient to use for HTTP operations
     */
    public BulkClient(RestClient restclient) {
        this.restClient = restclient;
    }
    /**
     * Adds an index request to the bulk queue.
     *
     * @param index the index to store the document in
     * @param id the document ID
     * @param doc the document object to serialize and store
     * @param <T> the type of the document
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public <T> void addIndexRequest(Index index, Id id, T doc) throws JsonProcessingException {
        String json = JsonUtil.serialize(doc);
        bulkRequests.add(indexRequest.formatted(index._index(), id._id(), json));
        if (bulkAutoCommit)  executeBulk(false);
    }
    /**
     * Adds an update request to the bulk queue.
     *
     * @param index the index to update the document in
     * @param id the document ID
     * @param doc the document object containing fields to update
     * @param <T> the type of the document
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public <T> void addUpdateRequest(Index index, Id id, T doc) throws JsonProcessingException {
        String json = JsonUtil.serialize(doc);
        bulkRequests.add(updateRequest.formatted(index._index(), id._id(), json));
        if (bulkAutoCommit) executeBulk(false);
    }
    /**
     * Adds a delete request for the specified index and document ID to the bulk queue.
     * Executes the bulk operation if auto-commit is enabled.
     *
     * @param index the index to delete the document from
     * @param id the document ID
     */
    public void addDeleteRequest(Index index, Id id) {
        bulkRequests.add(deleteRequest.formatted(index._index(), id._id()));
        if (bulkAutoCommit)  executeBulk(false);
    }
    /**
     * Executes the bulk operation if forced or if the bulk queue exceeds 10,000 requests.
     * Clears the bulk queue after execution.
     *
     * @param force if true, forces execution regardless of queue size
     */
    public void executeBulk(Boolean force) {
        if ((force || bulkRequests.size() > 10000) && !bulkRequests.isEmpty()) {
            StringBuilder bodyBuffer =  new StringBuilder();
            bulkRequests.forEach(bodyBuffer::append);
            var response = restClient.post()
                    .uri("_bulk")
                    .body(bodyBuffer.toString())
                    .retrieve()
                    .body(Map.class);
            bulkRequests.clear();
            if (response.get("errors") != null && (Boolean) response.get("errors")) {
                throw new RuntimeException("Bulk operation failed: " + response);
            }
            if (response == null || response.isEmpty()) {
                throw new RuntimeException("Bulk operation failed: No response received.");
            }
        }
    }
    /**
     * Sets whether bulk operations should be automatically committed after each request.
     *
     * @param autoCommit true to enable auto-commit, false otherwise
     */
    public void setBulkAutoCommit(boolean autoCommit) {
        bulkAutoCommit = autoCommit;
    }
}
