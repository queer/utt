package gg.amy.utt.transform;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author amy
 * @since 3/4/22.
 */
public record TransformationContext(
        @Nonnull InputFormat input,
        @Nonnull OutputFormat output,
        @Nullable String extractionPath,
        @Nullable String mapper
) {
}
