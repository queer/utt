package gg.amy.utt.transform.impl;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 2/27/22.
 */
public class CsvTransformerTest {
    @Test
    public void testCsvIdentityTransformation() {
        final var input = """
                name,age,address
                amy,42069,"123 fake street, nowhere, this was generated by github copilot"
                """;
        final var ctx = new TransformationContext(InputFormat.CSV, OutputFormat.CSV, null, null, false, false);
        final var out = new CsvTransformer().transformOutput(ctx, new CsvTransformer().transformInput(ctx, input));
        // TODO: Can we preserve the outer tag name somehow?
        assertEquals("""
                name,age,address
                amy,42069,"123 fake street, nowhere, this was generated by github copilot"
                """, out);
    }

    @Test
    public void testJsonToCsvTransformation() {
        final var input = """
                {
                    "name": "amy",
                    "age": 42069,
                    "address": {
                        "street": "123 fake street",
                        "city": "nowhere",
                        "state": "this was generated by github copilot"
                    }
                }
                """;
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.CSV, null, null, false, false);
        final var out = new CsvTransformer().transformOutput(ctx, new JsonTransformer().transformInput(ctx, input));
        assertEquals("""
                name,age,address
                amy,42069,"{""street"":""123 fake street"",""city"":""nowhere"",""state"":""this was generated by github copilot""}"
                """, out);
    }
}
