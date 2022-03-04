package gg.amy.utt.transform.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 2/27/22.
 */
public class YamlTransformer implements Transformer {
    private static final YAMLMapper MAPPER = new YAMLMapper();

    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        try {
            return MAPPER.readValue(input, Object.class);
        } catch(final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        try {
            return MAPPER.writeValueAsString(input);
        } catch(final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
