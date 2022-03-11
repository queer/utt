package gg.amy.utt.transform.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * @author amy
 * @since 2/27/22.
 */
public class CsvTransformer implements Transformer {
    private static final CsvMapper MAPPER = new CsvMapper();
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        try {
            return MAPPER.readerFor(Object.class).with(CsvSchema.emptySchema().withHeader()).readValues(input).readAll();
        } catch(final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        // Writing to a CSV is tricky. Jackson can't automatically determine the schema, so we have to
        // attempt to figure it out for Jackson to use.

        final var schemaBuilder = CsvSchema.builder();
        if(input instanceof Iterable || input instanceof Map) {
            // We can have two main types of data, lists and
            // objects. Any other data structure, when serialised,
            // will convert down into one of these two primitives.
            // We can use this to extract a schema from the first level.

            // If `input` is a map, extract keys directly.
            if(input instanceof Map map) {
                // HACK: Assume that this is an ordered map
                final var keys = map.keySet().stream().toList();
                for(int i = 0; i < keys.size(); i++) {
                    final var k = keys.get(i);
                    if(!(k instanceof final String key)) {
                        throw new IllegalArgumentException("Key at " + i + " is not a string (got: " + k + ')');
                    }
                    schemaBuilder.addColumn(key);
                }
            } else {
                // If `input` is a list, extract keys from all maps
                final var keys = new LinkedHashSet<String>();
                for(final var o : (Iterable<?>) input) {
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
            // HACK: Serialise complex objects into a usable form
            final Object out;
            if(input instanceof Map map) {
                out = flatten(map);
            } else if(input instanceof List list) {
                out = flatten(list);
            } else {
                out = input;
            }
            var writer = MAPPER.writerFor(input.getClass());
            if(ctx.pretty()) {
                writer = writer.withDefaultPrettyPrinter();
            }
            return writer.with(schema).writeValueAsString(out);
        } catch(final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<Object> flatten(@Nonnull final Collection<?> input) {
        final var out = new ArrayList<>(input.size());
        input.forEach(o -> {
            if(o instanceof Map) {
                out.add(flatten((Map<?, ?>) o));
            } else {
                out.add(o);
            }
        });
        return out;
    }

    private Map<Object, Object> flatten(@Nonnull final Map<?, ?> input) {
        final var out = new LinkedHashMap<>();
        input.forEach((k, v) -> {
            // If v is a non-primitive, flatten it into JSON
            if(v != null && !(v instanceof String) && !(v instanceof Number) && !(v instanceof Boolean)) {
                try {
                    out.put(k, JSON_MAPPER.writeValueAsString(v));
                } catch(final JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                // If v is primitive, add directly
                out.put(k, v);
            }
        });
        return out;
    }
}
