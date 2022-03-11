package gg.amy.utt.transform.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/4/22.
 */
public class PropertiesTransformer implements Transformer {
    private static final JavaPropsMapper MAPPER = new JavaPropsMapper();

    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        try {
            return MAPPER.readValue(input, Object.class);
        } catch(@Nonnull final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        try {
            var writer = MAPPER.writerFor(input.getClass());
            if(ctx.pretty()) {
                writer = writer.withDefaultPrettyPrinter();
            }
            return writer.writeValueAsString(input);
        } catch(@Nonnull final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
