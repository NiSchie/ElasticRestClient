package io.github.nischie.elasticrestclient.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Represents a search result from Elasticsearch containing scroll information and hits.
 *
 * @param scrollId the scroll identifier for pagination
 * @param hits     the hits object containing the list of documents and total count
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ElasticDocumentSearchResult (
        /**
         * The scroll ID used for paginated search results (scroll API).
         */
        @Nullable @JsonProperty("_scroll_id") String scrollId,
        /**
         * The hits object containing the list of documents and total count.
         */
        @JsonProperty("hits") ElasticSearchHits hits
) {
    /**
     * Returns the number of search hits in this result.
     * @return the number of hits
     */
    public Integer searchResultSize() {
        return hits.total().value();
    }
    /**
     * Returns the list of ElasticDocument search hits.
     * @return the list of search hits
     */
    public List<ElasticDocument> searchHits() {
        return hits.hits();
    }

    /**
     * Represents the hits object in the Elasticsearch search result.
     * <p>
     * Contains the total count and the list of document hits.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ElasticSearchHits(
        /**
         * The total number of hits.
         */
        @JsonProperty("total") ElasticSearchTotal total,
        /**
         * The list of document hits.
         */
        @JsonProperty("hits") List<ElasticDocument> hits
    ) {}
    /**
     * Represents the total object in the Elasticsearch search result.
     * <p>
     * Contains the total count and the relation (e.g., "eq" or "gte").
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ElasticSearchTotal(
        /**
         * The total number of hits.
         */
        @JsonProperty("value") Integer value,
        /**
         * The relation of the total (e.g., "eq" or "gte").
         */
        @JsonProperty("relation") String relation
    ) {}
}
