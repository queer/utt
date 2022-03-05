package gg.amy.utt.transform.impl;

import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author amy
 * @since 3/4/22.
 */
public class Base64Transformer implements Transformer {
    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        return Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8));
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        if(input instanceof byte[] array) {
            return Base64.getEncoder().encodeToString(array);
        } else if(input instanceof String string) {
            return Base64.getEncoder().encodeToString(string.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new IllegalArgumentException("Unsupported input type for base64: " + input.getClass());
        }
    }
}
