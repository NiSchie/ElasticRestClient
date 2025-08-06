package io.github.nischie.elasticrestclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocumentSearchResult;
import io.github.nischie.elasticrestclient.domain.model.Field;
import io.github.nischie.elasticrestclient.domain.model.Id;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.domain.model.Value;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import io.github.nischie.elasticrestclient.domain.queries.UpdateByStringQuery;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.json.JSONObject;
import org.springframework.http.HttpStatusCode;
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
     * @return the response specification from the RestClient
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public RestClient.ResponseSpec index(Index index, Id id, Object document) throws JsonProcessingException {
        String json = JsonUtil.serialize(document);
        return restClient.put()
                .uri(index._index() + "/_doc/" + id._id())
                .body(json)
                .retrieve();
    }
    /**
     * Indexes (creates or updates) a document in the specified index without specifying an ID.
     *
     * @param index the index to store the document in
     * @param document the document object to serialize and store
     * @return the response specification from the RestClient
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public RestClient.ResponseSpec index(Index index, Object document) throws JsonProcessingException {
        String json = JsonUtil.serialize(document);
        return restClient.post()
                .uri(index._index() + "/_doc/")
                .body(json)
                .retrieve();
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
     * @return the response specification from the RestClient
     */
    public RestClient.ResponseSpec delete(Index index, Id id) {
        return restClient.delete()
                .uri(index._index() + "/_doc/" + id._id())
                .retrieve();
    }

    /**
     * Searches for documents in the specified index using a StringSearchQuery.
     *
     * @param index the index to search in
     * @param query the search query
     * @return a list of ElasticDocument search hits
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public List<ElasticDocument> searchDocuments(Index index, StringSearchQuery query) throws JsonProcessingException {
        var response = restClient.post()
                .uri(index._index() + "/_search")
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
     * @return the response specification from the RestClient
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public RestClient.ResponseSpec deleteByStringQuery(Index index, StringSearchQuery query) throws JsonProcessingException {
            String queryBody = JsonUtil.serialize(query);
            return restClient.post()
                    .uri(index._index() + "/_delete_by_query")
                    .body(queryBody)
                    .retrieve();
    }

    public RestClient.ResponseSpec updateByStringQuery(Index index, StringSearchQuery query, Field field, Value value) throws JsonProcessingException {
        String queryBody = JsonUtil.serialize(UpdateByStringQuery.of(query, field, value));
        return restClient.post()
                .uri(index._index() + "/_update_by_query")
                .body(queryBody)
                .retrieve();
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
            var response = new JSONObject(restClient
                    .post()
                    .uri(index._index() + "/_count")
                    .body(queryBody)
                    .retrieve()
                    .body(Map.class));
            return response.getLong("count");
        } catch (Exception e) {
            return null;
        }
    }

}
