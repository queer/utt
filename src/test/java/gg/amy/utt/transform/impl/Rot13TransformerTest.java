package gg.amy.utt.transform.impl;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/10/22.
 */
public class Rot13TransformerTest {
    @Test
    public void testRot13Transform() {
        final var input = "This is a test.";
        final var ctx = new TransformationContext(InputFormat.PLAIN, OutputFormat.ROT13, null, null, false, false);
        final var out = new Rot13Transformer().transformOutput(ctx, input);
        assertEquals("Guvf vf n grfg.", out);
    }
}
