package io.github.nischie.elasticrestclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.client.ElasticRestClient;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.github.nischie.elasticrestclient.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class BulkClientIT extends BaseIT{
    private ElasticRestClient elasticClient = BaseIT.getElasticRestClient();
    private final TestData testData = new TestData();


    @Test
    void testBulk() throws JsonProcessingException, InterruptedException {
        var id = testData.newId();
        elasticClient.bulk().addIndexRequest(TEST_INDEX, id, TEST_DOCUMENT_SOURCE);
        elasticClient.bulk().executeBulk(true);
        ElasticDocument doc= null;
        var maxTries = 4;
        while (doc == null && maxTries-- > 0) {
            doc = elasticClient.document().getDocument(TEST_INDEX, id);
            if (doc == null) {
                Thread.sleep(1000);
            }
        }
        assertNotNull(doc);
        assertEquals(JsonUtil.serialize(TEST_DOCUMENT_SOURCE),
                JsonUtil.serialize(doc.source()));
        elasticClient.bulk().addUpdateRequest(TEST_INDEX, id, Map.of("newKey", "value"));
        elasticClient.bulk().executeBulk(true);
        doc = elasticClient.document().getDocument(TEST_INDEX, id);
        assertNotNull(doc);
        assertEquals("value", doc.source().get("newKey"));

        elasticClient.bulk().addDeleteRequest(TEST_INDEX, id);
        elasticClient.bulk().executeBulk(true);
        doc = elasticClient.document().getDocument(TEST_INDEX, id);
        assertNull(doc, "Document should be deleted");
    }
}

