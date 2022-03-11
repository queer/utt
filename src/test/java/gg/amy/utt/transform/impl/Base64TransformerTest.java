package gg.amy.utt.transform.impl;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/4/22.
 */
public class Base64TransformerTest {
    @Test
    public void testIdentityWorks() {
        final var input = "asdf";
        final var ctx = new TransformationContext(InputFormat.BASE64, OutputFormat.BASE64, null, null, false, false);
        final var out = new Base64Transformer().transformOutput(ctx, new Base64Transformer().transformInput(ctx, input));
        assertEquals(input, out);
    }

    @Test
    public void testEncodeWorks() {
        final var input = "asdf";
        final var ctx = new TransformationContext(InputFormat.BASE64, OutputFormat.BASE64, null, null, false, false);
        final var out = new Base64Transformer().transformOutput(ctx, input);
        assertEquals("YXNkZg==", out);
    }

    @Test
    public void testDecodeWorks() {
        final var input = "YXNkZg==";
        final var ctx = new TransformationContext(InputFormat.BASE64, OutputFormat.BASE64, null, null, false, false);
        final var out = new Base64Transformer().transformInput(ctx, input);
        assertEquals("asdf", new String((byte[]) out));
    }
}
