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
public class JsonTransformerTest {
    @Test
    public void testJsonIdentityTransformation() {
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
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, null, false, false);
        final var out = new JsonTransformer().transformOutput(ctx, new JsonTransformer().transformInput(ctx, input));
        assertEquals("{\"name\":\"amy\",\"age\":42069,\"address\":{\"street\":\"123 fake street\",\"city\":\"nowhere\",\"state\":\"this was generated by github copilot\"}}", out);
    }

    @Test
    public void testXmlToJsonTransformation() {
        final var input = """
                <person>
                    <name>amy</name>
                    <age>42069</age>
                    <address>
                        <street>123 fake street</street>
                        <city>nowhere</city>
                        <state>this was generated by github copilot</state>
                    </address>
                </person>
                """;
        final var ctx = new TransformationContext(InputFormat.XML, OutputFormat.JSON, null, null, false, false);
        final var out = new JsonTransformer().transformOutput(ctx, new XmlTransformer().transformInput(ctx, input));
        assertEquals("{\"name\":\"amy\",\"age\":\"42069\",\"address\":{\"street\":\"123 fake street\",\"city\":\"nowhere\",\"state\":\"this was generated by github copilot\"}}", out);
    }

    @Test
    public void testJsonPrettyIdentityTransformation() {
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
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, null, false, true);
        final var out = new JsonTransformer().transformOutput(ctx, new JsonTransformer().transformInput(ctx, input));
        assertEquals("""
                {
                  "name" : "amy",
                  "age" : 42069,
                  "address" : {
                    "street" : "123 fake street",
                    "city" : "nowhere",
                    "state" : "this was generated by github copilot"
                  }
                }
                """.trim(), out);
    }
}
