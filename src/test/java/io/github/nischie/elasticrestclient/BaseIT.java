package io.github.nischie.elasticrestclient;

import io.github.nischie.elasticrestclient.client.ElasticRestClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.logging.Logger;

public class BaseIT {
    private static final Logger log = Logger.getLogger("BaseIT");
    protected static ElasticsearchContainer elasticsearchContainer;

    @BeforeAll
    static void setUp() throws InterruptedException {
        log.info("Starting Elasticsearch container for integration tests");
        elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.14");
        elasticsearchContainer.start();
        log.info("Elasticsearch container started at " + elasticsearchContainer.getHttpHostAddress());
        var elasticRestClient = getElasticRestClient();
        elasticRestClient.index().createIndex(TestData.TEST_INDEX);
        Thread.sleep(5000);
    }

    @AfterAll
    static void tearDown() {
        log.info("Stopping Elasticsearch container after integration tests");
        if (elasticsearchContainer != null) {
            var hostaddress = elasticsearchContainer.getHttpHostAddress();
            elasticsearchContainer.stop();
            log.info("Elasticsearch container stopped at " + hostaddress);
        }
    }

    protected static String getHttpHostAddress() {
        return "http://"+elasticsearchContainer.getHttpHostAddress();
    }

    protected static final ElasticRestClient getElasticRestClient() {
            return new ElasticRestClient(getHttpHostAddress(), "user", "password");
    }

}
