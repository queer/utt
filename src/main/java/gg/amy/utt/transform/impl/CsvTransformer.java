package gg.amy.utt.transform.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import gg.amy.utt.transform.Transformer;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * @author amy
 * @since 2/27/22.
 */
public class CsvTransformer implements Transformer {
    private static final CsvMapper MAPPER = new CsvMapper();

    @Override
    public Object transformInput(final String input) {
        try {
            final var out = MAPPER.readerFor(Object.class).with(CsvSchema.emptySchema().withHeader()).readValues(input).readAll();
            return out;
        } catch(final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String transformOutput(final Object input) {
        // Writing to a CSV is tricky. Jackson can't automatically determine the schema, so we have to
        // attempt to figure it out for Jackson to use.

        final var schemaBuilder = CsvSchema.builder();
        if(input instanceof Iterable) {
            // We can have two main types of data, lists and
            // objects. Any other data structure, when serialised,
            // will convert down into one of these two primitives.
            // We can use this to extract a schema from the first level.

            // If `input` is a map, extract keys directly.
            if(input instanceof Map) {
                // HACK: Assume that this is an ordered map
                final var keys = ((Map<?, ?>) input).keySet().stream().toList();
                for(int i = 0; i < keys.size(); i++) {
                    final var k = keys.get(i);
                    if(!(k instanceof final String key)) {
                        throw new IllegalArgumentException("Key at " + i + " is not a string (got: " + k + ')');
                    }
                    schemaBuilder.addColumn(key);
                }
            } else if(input instanceof List) {
                // If `input` is a list, extract keys from all maps
                final var keys = new LinkedHashSet<String>();
                for(final var o : (List<?>) input) {
                    if(o instanceof Map) {
                        ((Map<?, ?>) o).keySet()
                                .stream()
                                .filter(k -> k instanceof String)
                                .map(k -> (String) k)
                                .forEach(keys::add);
                    }
                }
                for(final var key : keys) {
                    schemaBuilder.addColumn(key);
                }
            }
        }

        final var schema = schemaBuilder.build().withHeader();
        try {
            return MAPPER.writerFor(input.getClass()).with(schema).writeValueAsString(input);
        } catch(final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
