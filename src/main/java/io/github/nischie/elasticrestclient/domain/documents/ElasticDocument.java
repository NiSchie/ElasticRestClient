package io.github.nischie.elasticrestclient.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.json.JSONObject;

import java.util.Map;

/**
 * Represents a document retrieved from Elasticsearch.
 * <p>
 * Contains metadata and the document source as a map.
 * Provides utility methods for converting the document to JSON and mapping the source to POJOs.
 *
 * @param index   the name of the index the document belongs to
 * @param id      the unique identifier of the document
 * @param version the version number of the document
 * @param type    the type of the document (usually "_doc")
 * @param source  the source content of the document as a map
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ElasticDocument(
        @JsonProperty("_index") String index,
        @JsonProperty("_id") String id,
        @JsonProperty("_version") Integer version,
        @JsonProperty("_type") String type,
        @JsonProperty("_source") Map<String, Object> source
) {

    /**
     * Converts the entire document (including metadata) to a JSONObject.
     *
     * @return a JSONObject representation of the document
     */
    public JSONObject toJSON() {
        var json = new JSONObject();
        json.put("_index", index);
        json.put("_id", id);
        json.put("_version", version);
        json.put("_type", type);
        json.put("_source", source);
        return json;
    }

    /**
     * Converts the document source to a JSONObject.
     *
     * @return a JSONObject representation of the source map
     */
    public JSONObject sourceAsJSON() {
        return new JSONObject(source);
    }

    /**
     * Maps the document source to a POJO of the specified class.
     *
     * @param clazz the target class to map the source to
     * @param <T>   the type of the target class
     * @return an instance of the target class populated with source data
     */
    public <T> T sourceAs(Class<T> clazz) {
        try {
            return JsonUtil.convertSourceToPojo(source, clazz);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to map source to POJO", e);
        }
    }
}
