package io.github.nischie.elasticrestclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.nischie.elasticrestclient.domain.model.Id;
import io.github.nischie.elasticrestclient.domain.model.Index;
import io.github.nischie.elasticrestclient.util.JsonUtil;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

public class TestData {

    public static final Index TEST_INDEX = new Index("testindex");
    public static final String TEST_ALIAS = "test-alias";
    public static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    public static final Map<String, Object> TEST_DOCUMENT_SOURCE = Map.of(
            "string", "value",
            "int", 123,
            "boolean", true,
            "nested", Map.of(
                    "nestedString", "nestedValue",
                    "nestedInt", 456
            ),
            "date", OffsetDateTime.parse("2023-10-01T12:00:00Z", fmt)
    );

    public Id newId() {
        return new Id(UUID.randomUUID());
    }

    public static TestPOJO getTestPOJO() {
        return JsonUtil.convertSourceToPojo(TEST_DOCUMENT_SOURCE, TestPOJO.class);
    }

    public record TestPOJO(
            @JsonProperty("string") String stringValue,
            @JsonProperty("int") Integer intValue,
            @JsonProperty("boolean") Boolean booleanValue,
            @JsonProperty("nested") Map<String, Object> nestedValue,
            @JsonProperty("date") OffsetDateTime dateValue
    ) {
    }
}
