package io.github.nischie.elasticrestclient.domain.queries;

import io.github.nischie.elasticrestclient.domain.model.Field;
import io.github.nischie.elasticrestclient.domain.model.Value;

/**
 *
 */
public record UpdateByStringQuery(
        StringSearchQuery.Query query,
        Script script
) {

    public static UpdateByStringQuery of(StringSearchQuery query, Field field, Value value) {
        return new UpdateByStringQuery(query.getQuery(), new Script(field, value) );
    }

    private record Script(String source, String lang) {
        public Script(Field field, Value value) {
            this("ctx._source." + field.fieldName() + " = \"" + value.value() + "\"", "painless");
        }
        public static Script of(String source, String lang) {
            return new Script(source, lang);
        }
    }
}
