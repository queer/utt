package gg.amy.utt.transform.impl;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/4/22.
 */
public class PlainTransformerTest {
    @Test
    public void testTransform() {
        final Transformer transformer = new PlainTransformer();
        final var ctx = new TransformationContext(InputFormat.PLAIN, OutputFormat.PLAIN, null, null, false, false);
        assertEquals("abc", transformer.transformOutput(ctx,  transformer.transformInput(ctx, "abc")));
    }
}
