package gg.amy.utt.transform.impl;

import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/10/22.
 */
public class Rot13Transformer implements Transformer {
    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        throw new UnsupportedOperationException("ROT13 is not a valid input");
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        final var out = "" + input;
        return rot13(out);
    }

    @Nonnull
    private String rot13(@Nonnull final String input) {
        final var output = new StringBuilder();
        for(int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if(c >= 'a' && c <= 'm') {
                c += 13;
            } else if(c >= 'A' && c <= 'M') {
                c += 13;
            } else if(c >= 'n' && c <= 'z') {
                c -= 13;
            } else if(c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            output.append(c);
        }
        return output.toString();
    }
}
