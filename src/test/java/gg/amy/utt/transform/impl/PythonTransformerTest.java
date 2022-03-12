package gg.amy.utt.transform.impl;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/11/22.
 */
public class PythonTransformerTest {
    @Test
    public void testPythonToJsonTransformation() {
        final var input = """
                {'key': 'value', 'list': [1, 2, 3]}
                """;
        final var ctx = new TransformationContext(InputFormat.PYTHON, OutputFormat.JSON, null, null, false, false);
        final var out = new JsonTransformer().transformOutput(ctx, new PythonTransformer().transformInput(ctx, input));
        assertEquals("{\"list\":[1,2,3],\"key\":\"value\"}", out);
    }
}
