package io.github.nischie.elasticrestclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocumentSearchResult;
import io.github.nischie.elasticrestclient.domain.model.Field;
import io.github.nischie.elasticrestclient.domain.model.Id;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.domain.model.Value;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import io.github.nischie.elasticrestclient.domain.queries.UpdateByStringQuery;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ServerErrorException;

import java.util.List;
import java.util.Map;

/**
 * Client for document operations in Elasticsearch.
 * <p>
 * Provides methods for indexing, retrieving, searching, counting, and deleting documents.
 * Uses Spring's RestClient for HTTP communication and JsonUtil for serialization.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class DocumentClient {
    /**
     * The underlying RestClient used for HTTP requests.
     */
    private final RestClient restClient;

    /**
     * Constructs a DocumentClient with the given RestClient.
     *
     * @param restClient the RestClient to use for HTTP operations
     */
    public DocumentClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Indexes (creates or updates) a document in the specified index with the given ID.
     *
     * @param index the index to store the document in
     * @param id the document ID
     * @param document the document object to serialize and store
     * @return the response entity of the rest request
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public ResponseEntity<Map> index(Index index, Id id, Object document) throws JsonProcessingException {
        String json = JsonUtil.serialize(document);
        return restClient.put()
                .uri(index._index() + "/_doc/" + id._id())
                .body(json)
                .retrieve()
                .toEntity(Map.class);
    }
    /**
     * Indexes (creates or updates) a document in the specified index without specifying an ID.
     *
     * @param index the index to store the document in
     * @param document the document object to serialize and store
     * @return the response entity of the rest request
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public ResponseEntity<Map> index(Index index, Object document) throws JsonProcessingException {
        String json = JsonUtil.serialize(document);
        return restClient.post()
                .uri(index._index() + "/_doc/")
                .body(json)
                .retrieve()
                .toEntity(Map.class);
    }

    /**
     * Retrieves a document from the specified index by its ID.
     *
     * @param index the index to search in
     * @param id the document ID
     * @return the ElasticDocument if found, or null if not found
     */
    public ElasticDocument getDocument(Index index, Id id) {
        try {
            return restClient.get()
                    .uri(index._index() + "/_doc/" + id._id())
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (response, status) -> {
                        throw new ServerErrorException("Server error for: " + index + "/" + id, new Throwable("Server error while retrieving document"));
                    })
                    .body(ElasticDocument.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes a document from the specified index by its ID.
     *
     * @param index the index to delete from
     * @param id the document ID
     * @return the response entity of the rest request
     */
    public ResponseEntity<Map> delete(Index index, Id id) {
        return restClient.delete()
                .uri(index._index() + "/_doc/" + id._id())
                .retrieve()
                .toEntity(Map.class);
    }

    /**
     * Searches for documents in the specified index using a StringSearchQuery.
     * The default result size maximum is 1000.
     *
     * @param index the index to search in
     * @param query the search query
     * @return a list of ElasticDocument search hits
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public List<ElasticDocument> searchDocuments(Index index, StringSearchQuery query) throws JsonProcessingException {
        var response = restClient.post()
                .uri(index._index() + "/_search?size=1000")
                .body(JsonUtil.serialize(query))
                .retrieve()
                .body(ElasticDocumentSearchResult.class);
        return response.searchHits();
    }

    /**
     * Searches for documents in the specified index using a StringSearchQuery.
     * The result size is set specifically.
     *
     * @param index the index to search in
     * @param query the search query
     * @param size the maximum search result size
     * @return a list of ElasticDocument search hits
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public List<ElasticDocument> searchDocuments(Index index, StringSearchQuery query, Integer size) throws JsonProcessingException {
        var response = restClient.post()
                .uri(index._index() + "/_search?size="+size)
                .body(JsonUtil.serialize(query))
                .retrieve()
                .body(ElasticDocumentSearchResult.class);
        return response.searchHits();
    }

    /**
     * Deletes documents from the specified index that match the given query.
     *
     * @param index the index to delete from
     * @param query the match query
     * @return the response entity of the rest request
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public ResponseEntity<Map> deleteByStringQuery(Index index, StringSearchQuery query) throws JsonProcessingException {
            String queryBody = JsonUtil.serialize(query);
            return restClient.post()
                    .uri(index._index() + "/_delete_by_query")
                    .body(queryBody)
                    .retrieve()
                    .toEntity(Map.class);
    }

    public ResponseEntity<Map> updateByStringQuery(Index index, StringSearchQuery query, Field field, Value value) throws JsonProcessingException {
        String queryBody = JsonUtil.serialize(UpdateByStringQuery.of(query, field, value));
        return restClient.post()
                .uri(index._index() + "/_update_by_query")
                .body(queryBody)
                .retrieve()
                .toEntity(Map.class);
    }

    /**
     * Counts the number of documents in the specified index that match the given query.
     *
     * @param index the index to count in
     * @param query the match query
     * @return the count of matching documents, or null if an error occurs
     */
    public Long countByQuery(Index index, StringSearchQuery query) {
        try {
            String queryBody = JsonUtil.serialize(query);
            var response = new ObjectNode(restClient
                    .post()
                    .uri(index._index() + "/_count")
                    .body(queryBody)
                    .retrieve()
                    .body(com.fasterxml.jackson.databind.node.JsonNodeFactory.class));
            return response.get("count").asLong();
        } catch (Exception e) {
            return null;
        }
    }

}
