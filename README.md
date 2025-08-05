# ElasticRestClient

A lightweight Java client for interacting with Elasticsearch via REST, supporting document CRUD, bulk operations, advanced search queries, and scrollable search.

## Features
- Index, update, delete, and retrieve documents
- Bulk operations (index, update, delete)
- Flexible search with query string and match queries
- Scrollable search for large result sets
- Index management (delete, alias)
- Handles both POJOs and org.json (JSONObject/JSONArray) for document bodies

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven (for building)
- Elasticsearch instance (tested with 7.x)

### Installation
#### Maven Dependency
Add the following dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>io.github.nischie</groupId>
    <artifactId>elasticrestclient</artifactId>
</dependency>
```

#### Building from source
1. Clone the repository:
   ```sh
   git clone git@github.com:NiSchie/ElasticRestClient.git
   cd ElasticRestClient
   ```
2. Build the project:
   ```sh
   mvn clean package
   ```

## Usage

### 1. Create a Client
```java
ElasticRestClient client = new ElasticRestClient(
    "http://localhost:9200", // Elasticsearch host
    "user",                  // Username
    "password"               // Password
);
```

### 2. Index a Document
```java
Index index = Index.of("my-index");
Id id = Id.of("doc-id");
Map<String, Object> doc = Map.of("field", "value");
client.document().index(index, id, doc); // POJO or Map, allows update if exists

//index only with auto-generated ID
client.document().index(index, doc); // No ID specified, Elasticsearch generates one

// Or using JSONObject
JSONObject jsonDoc = new JSONObject(doc);
client.document().index(index, id, jsonDoc);
```

### 3. Get a Document
```java
ElasticDocument doc = client.document().getDocument(index, id);
if (doc == null) {
    // Not found
}
```

### 4. Working with ElasticDocument
After retrieving a document, you can access its source and metadata:

```java
if (doc != null) {
    // Get the source as a Map
    Map<String, Object> source = doc.source();
    // Get the source as a JSONObject
    JSONObject sourceJson = doc.sourceAsJSON();
    // Get the entire document (including metadata) as a JSONObject
    JSONObject fullJson = doc.toJSON();
    // Parse the source directly into a POJO
    MyPojo pojo = doc.sourceAs(MyPojo.class);
}
```

### 5. Delete a Document
```java
client.document().delete(index, id);
```

### 6. Search Documents
```java
StringSearchQuery query = StringSearchQuery.of("field:value AND other:foo");
List<ElasticDocument> results = client.document().searchDocuments(index, query);
```

### 7. Count Documents by Query
```java
Long count = client.document().countByQuery(index, query);
```

### 8. Delete by Query
```java
client.document().deleteByMatchQuery(index, query);
```

### 9. Bulk Operations
```java
BulkClient bulk = client.bulk();
bulk.addIndexRequest(index, id, doc); // POJO, Map, or JSONObject
bulk.addUpdateRequest(index, id, Map.of("field", "newValue")); // Update specific fields
bulk.addDeleteRequest(index, id);
bulk.executeBulk(true); // Force execution
```

### 10. Scrollable Search
```java
ScrollableSearch scroll = client.scrollSearch(index, query, 100);
List<ElasticDocument> hits;
while (scroll.scroll()) {
    hits = scroll.getSearchHits();
    // process hits
}
```
- The `scroll()` method performs the initial search on the first call and fetches the next page on subsequent calls using the scroll ID.
- It returns the current page of hits as a `List<ElasticDocument>`, or an empty list if there are no more results.

### 11. Index Management
```java
client.index().deleteIndex(index);
client.index().alias(index, "alias-name");
```

## Notes
- All document methods accept POJOs, Maps, or org.json.JSONObject/JSONArray.
- JSON serialization is handled automatically.
- Error handling: `getDocument` returns `null` if not found; other methods throw on error.
