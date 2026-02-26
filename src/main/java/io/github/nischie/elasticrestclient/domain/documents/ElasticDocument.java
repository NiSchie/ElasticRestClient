package io.github.nischie.elasticrestclient.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.nischie.elasticrestclient.util.JsonUtil;

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

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Converts the entire document (including metadata) to a Jackson ObjectNode.
     *
     * @return a Jackson ObjectNode representation of the document
     */
    public ObjectNode toJSON() {
        ObjectNode json = MAPPER.createObjectNode();
        if (index != null) json.put("_index", index);
        if (id != null) json.put("_id", id);
        if (version != null) json.put("_version", version);
        if (type != null) json.put("_type", type);
        if (source != null) json.set("_source", MAPPER.valueToTree(source));
        return json;
    }

    /**
     * Converts the document source to a Jackson ObjectNode.
     *
     * @return a Jackson ObjectNode representation of the source map
     */
    public ObjectNode sourceAsJSON() {
        return MAPPER.valueToTree(source);
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
