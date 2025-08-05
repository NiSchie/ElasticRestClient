package io.github.nischie.elasticrestclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.client.ElasticRestClient;
import io.github.nischie.elasticrestclient.domain.documents.ElasticDocument;
import io.github.nischie.elasticrestclient.domain.queries.StringSearchQuery;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.nischie.elasticrestclient.TestData.TEST_INDEX;
import static org.junit.jupiter.api.Assertions.*;

class ScrollSearchIT extends BaseIT{
    private ElasticRestClient elasticClient = BaseIT.getElasticRestClient();
    private final TestData testData = new TestData();


    @Test
    void testScrollSearch() throws JsonProcessingException, InterruptedException {
        var addedForSearch = 0;
        for (int i = 0; i < 100; i++) {
            elasticClient.bulk().addIndexRequest(TEST_INDEX, testData.newId(), Map.of("rand", testData.newId(), "string", "value"));
            addedForSearch++;
            elasticClient.bulk().addIndexRequest(TEST_INDEX, testData.newId(), Map.of("rand", testData.newId(), "shouldNot", "come up in search"));
        }
        elasticClient.bulk().executeBulk(true);
        Thread.sleep(1000);

        var scrollSearchResponse = elasticClient.scrollSearch(TEST_INDEX, StringSearchQuery.of("string: \"value\""), 13);
        assertNotNull(scrollSearchResponse);
        List<ElasticDocument> documents = new ArrayList<>();
        while (scrollSearchResponse.scroll()) {
            documents.addAll(scrollSearchResponse.getSearchHits());
        }
        assertEquals(addedForSearch, documents.size());
    }
}

