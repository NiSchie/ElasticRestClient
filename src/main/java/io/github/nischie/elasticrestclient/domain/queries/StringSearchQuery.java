package io.github.nischie.elasticrestclient.domain.queries;

//"""
// {
//   "query":
//   {
//     "query_string": {"query": "%s"}
//   }
// }
// """
/**
 * Represents a query for searching documents in Elasticsearch using a query string.
 * <p>
 * Wraps the query string in the appropriate Elasticsearch query structure.
 *
 * @author nschieschke
 * @version $Id: $Id
 */
public class StringSearchQuery {
    private final Query query;

    /**
     * Constructs a StringSearchQuery with the given query string.
     *
     * @param queryString the query string to use in the search
     */
    public StringSearchQuery(String queryString) {
        this.query = new Query(new QueryString(queryString));
    }

    /**
     * Creates a new StringSearchQuery from the given query string.
     *
     * @param queryString the query string to use in the search
     * @return a new StringSearchQuery instance
     */
    public static StringSearchQuery of(String queryString) {
        return new StringSearchQuery(queryString);
    }

    /**
     * Returns the Query object representing the Elasticsearch query structure.
     *
     * @return the Query object
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Represents the top-level query object in the Elasticsearch query structure.
     */
    public static class Query {
        private final QueryString query_string;

        /**
         * Constructs a Query with the given QueryString.
         * @param query_string the QueryString object
         */
        public Query(QueryString query_string) {
            this.query_string = query_string;
        }

        /**
         * Returns the QueryString object.
         * @return the QueryString object
         */
        public QueryString getQuery_string() {
            return query_string;
        }
    }

    /**
     * Represents the query_string object in the Elasticsearch query structure.
     */
    public static class QueryString {
        private final String query;

        /**
         * Constructs a QueryString with the given query string.
         * @param query the query string
         */
        public QueryString(String query) {
            this.query = query;
        }

        /**
         * Returns the query string.
         * @return the query string
         */
        public String getQuery() {
            return query;
        }
    }
}
