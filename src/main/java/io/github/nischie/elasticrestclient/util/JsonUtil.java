package io.github.nischie.elasticrestclient.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;

/**
 * Utility class for JSON operations, including serialization and conversion to POJOs.
 * Uses Jackson for JSON processing and supports ObjectNode.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Serializes an object to a JSON string.
     * Otherwise, it uses Jackson's ObjectMapper to serialize the object.
     *
     * @param obj the object to serialize
     * @return a JSON string representation of the object
     * @throws com.fasterxml.jackson.core.JsonProcessingException if serialization fails
     */
    public static String serialize(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Converts a source map to a POJO of the specified class using Jackson's ObjectMapper.
     *
     * @param source the source map containing JSON data
     * @param clazz the target class to map the source to
     * @param <T> the type of the target class
     * @return an instance of the target class populated with source data
     * @throws java.lang.RuntimeException if mapping fails
     */
    public static <T> T convertSourceToPojo(Map<String, Object> source, Class<T> clazz) {
        try {
            return objectMapper.convertValue(source, clazz);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Failed to map source to "+clazz.getName(), e);
        }
    }
}
