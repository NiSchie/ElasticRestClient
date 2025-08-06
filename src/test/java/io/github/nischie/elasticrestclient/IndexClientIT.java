package io.github.nischie.elasticrestclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.nischie.elasticrestclient.client.ElasticRestClient;
import io.github.nischie.elasticrestclient.domain.model.Index;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndexClientIT extends BaseIT{
    private ElasticRestClient elasticClient = BaseIT.getElasticRestClient();
    private final TestData testData = new TestData();


    @Test
    void testIndexClient() throws JsonProcessingException, InterruptedException {
        var indexClient = elasticClient.index();
        var index = Index.of("indexclienttest");
        var created = indexClient.createIndex(index).getBody();
        assertNotNull(created);
        assertEquals(true, created.get("acknowledged"));
        var isExists = indexClient.indexExists(index);
        assertTrue(isExists);
        var aliased = indexClient.alias(index, "aliasIndexName").getBody();
        assertNotNull(aliased);
        var aliases = indexClient.getAliases(index);
        assertEquals(1, aliases.keySet().size());
    }
}

