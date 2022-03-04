package gg.amy.utt.transform;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 2/27/22.
 */
public interface Transformer {
    @Nonnull
    Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull String input);

    @Nonnull
    String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull Object input);
}
