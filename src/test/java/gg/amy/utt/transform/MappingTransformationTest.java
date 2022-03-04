package gg.amy.utt.transform;

import gg.amy.utt.UTT;
import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/4/22.
 */
public class MappingTransformationTest {
    @Test
    public void testMapperWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "$ * 2");
        final var out = UTT.runExtraction(ctx, "[1, 2, 3]");
        assertEquals("[2.0,4.0,6.0]", out);
    }
}
