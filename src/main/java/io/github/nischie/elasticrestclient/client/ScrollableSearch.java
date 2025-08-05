package io.github.nischie.elasticrestclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocumentSearchResult;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Utility for performing scrollable search operations in Elasticsearch.
 * <p>
 * Handles scroll context and pagination for large result sets.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class ScrollableSearch {
    private final RestClient restClient;
    private StringSearchQuery query;
    private Index index;
    private ElasticDocumentSearchResult elasticDocumentSearchResult;
    private int pageSize;

    /**
     * Constructs a ScrollableSearch for the given index, query, and page size.
     *
     * @param restClient the RestClient to use for HTTP operations
     * @param index      the index to search in
     * @param query      the search query
     * @param pageSize   the number of results per page
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public ScrollableSearch(RestClient restClient, Index index, StringSearchQuery query, int pageSize) throws JsonProcessingException {
        this.restClient = restClient;
        this.index = index;
        this.pageSize = pageSize;
        this.query = query;
    }

    /**
     * Directly returns the search hits for this scroll page.
     *
     * @return a list of {@link ElasticDocument} containing the search hits for this scroll page
     */
    public List<ElasticDocument> getSearchHits() {
        if (elasticDocumentSearchResult == null) {
            return List.of();
        }
        return elasticDocumentSearchResult.searchHits();
    }

    /**
     * Retrieves the next page of search results using the scroll API.
     * Updates the internal state with the new search hits.
     *
     * @return whether there are more search hits available
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public boolean scroll() throws JsonProcessingException {
        if (elasticDocumentSearchResult == null) {
            // Initial search
            elasticDocumentSearchResult = restClient
                    .post()
                    .uri(index._index() + "/_search?scroll=1m&size=%d".formatted(pageSize))
                    .body(JsonUtil.serialize(query))
                    .retrieve()
                    .body(ElasticDocumentSearchResult.class);
        } else {
            // Scroll using scroll_id
            var scrollSearchQuery = """
                    {
                       "scroll": "1m",
                       "scroll_id": "%s"
                    }
                """.formatted(elasticDocumentSearchResult.scrollId());
            elasticDocumentSearchResult = restClient
                    .post()
                    .uri("/_search/scroll")
                    .body(scrollSearchQuery)
                    .retrieve()
                    .body(ElasticDocumentSearchResult.class);
        }
        return elasticDocumentSearchResult.searchHits() != null && !elasticDocumentSearchResult.searchHits().isEmpty();
    }
}
