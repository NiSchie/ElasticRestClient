package io.github.nischie.elasticrestclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.client.ElasticRestClient;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.domain.model.Id;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import io.github.nischie.elasticrestclient.util.JsonUtil;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.nischie.elasticrestclient.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

class DocumentClientIT extends BaseIT{
    private ElasticRestClient elasticClient = BaseIT.getElasticRestClient();
    private final TestData testData = new TestData();


    @Test
    void testIndexMap() throws JsonProcessingException {
        var id = testData.newId();
        var resp = elasticClient.document().index(TEST_INDEX, id, TEST_DOCUMENT_SOURCE);
        assertNotNull(resp);
        JSONObject response = new JSONObject(resp.getBody());
        assertEquals("created", response.optString("result"));
        var doc = elasticClient.document().getDocument(TEST_INDEX, id);
        org.skyscreamer.jsonassert.JSONAssert.assertEquals(
                JsonUtil.serialize(TEST_DOCUMENT_SOURCE),
                JsonUtil.serialize(doc.source()),
                true
        );
    }

    @Test
    void testAutoIdIndexMap() throws JsonProcessingException {
        var resp = elasticClient.document().index(TEST_INDEX, TEST_DOCUMENT_SOURCE);
        assertNotNull(resp);
        JSONObject response = new JSONObject(resp.getBody());
        assertEquals("created", response.optString("result"));
        var doc = elasticClient.document().getDocument(TEST_INDEX, Id.of(response.getString("_id")));
        org.skyscreamer.jsonassert.JSONAssert.assertEquals(
                JsonUtil.serialize(TEST_DOCUMENT_SOURCE),
                JsonUtil.serialize(doc.source()),
                true
        );
    }

    @Test
    void testSearch() throws JsonProcessingException, InterruptedException {
        var id = testData.newId();
        var testDoc = Map.of(
                "string", "searchMe"
        );
        var resp = elasticClient.document().index(TEST_INDEX, id, testDoc);
        assertNotNull(resp);
        assertEquals("created", resp.getBody().get("result"));
        var maxTries = 3;
        List<ElasticDocument> searchDocuments = new ArrayList<>();
        do {
            searchDocuments = elasticClient.document().searchDocuments(TEST_INDEX, new StringSearchQuery("string: \"searchMe\""));
            Thread.sleep(1000 * (4 - maxTries));
        } while (searchDocuments.size() == 0 && --maxTries > 0);

        assertEquals(1, searchDocuments.size());

        resp = elasticClient.document().deleteByStringQuery(TEST_INDEX, new StringSearchQuery("string: \"searchMe\""));
        var d = resp.getBody();
        assertEquals(1, d.get("deleted"));
    }

    @Test
    void testCount() throws JsonProcessingException, InterruptedException {
        var resp = elasticClient.document().index(TEST_INDEX, testData.newId(), Map.of("string", "valuecountSearch"));
        assertNotNull(resp);
        var count = elasticClient.document().countByQuery(TEST_INDEX, new StringSearchQuery("string: \"valuecountSearch\""));
        var maxTries = 3;
        while (count == null || count == 0 && --maxTries > 0) {
            count = elasticClient.document().countByQuery(TEST_INDEX, new StringSearchQuery("string: \"valuecountSearch\""));
            Thread.sleep(1000 * (4 - maxTries));
        }
        assertEquals(1, count);
    }

    @Test
    void testDelete() throws JsonProcessingException, InterruptedException {
        var id = testData.newId();
        var resp = elasticClient.document().index(TEST_INDEX, id, TEST_DOCUMENT_SOURCE);
        assertNotNull(resp);
        assertEquals("created", resp.getBody().get("result"));
        Thread.sleep(3000);
        // Test delete
        var deleteResponse = elasticClient.document().delete(TEST_INDEX, id).getBody();
        assertNotNull(deleteResponse);
        assertEquals("deleted", deleteResponse.get("result"));
        Thread.sleep(1000);
        var maxTries = 4;
        var deletedDoc = elasticClient.document().getDocument(TEST_INDEX, id);
        while (deletedDoc != null && --maxTries > 0) {
            Thread.sleep(1000 * (4 - maxTries));
            deletedDoc = elasticClient.document().getDocument(TEST_INDEX, id);
        }
        assertNull(deletedDoc, "Document should be deleted and not found");
    }

    @Test
    void pojoParsingTest() throws JsonProcessingException {
        var id = testData.newId();
        var elasticClient = new ElasticRestClient(getHttpHostAddress(), "user", "password");
        elasticClient.index().deleteIndex(TEST_INDEX);
        var resp = elasticClient.document().index(TEST_INDEX, id, TestData.getTestPOJO());
        assertNotNull(resp);
        JSONObject response = new JSONObject(resp.getBody());
        assertEquals("created",response.optString("result"));

        var doc = elasticClient.document().getDocument(TEST_INDEX, id);
        assertNotNull(doc);

        // Test sourceAsPOJO
        TestData.TestPOJO sourcePOJO = doc.sourceAs(TestData.TestPOJO.class);
        assertEquals(getTestPOJO(), sourcePOJO);
    }
}

