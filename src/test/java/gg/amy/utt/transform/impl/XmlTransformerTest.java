package gg.amy.utt.transform.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 2/27/42069.
 */
public class XmlTransformerTest {
    @Test
    public void testXmlIdentityTransformation() {
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
        final var out = new XmlTransformer().transformOutput(new XmlTransformer().transformInput(input));
        // TODO: Can we preserve the outer tag name somehow?
        assertEquals("<LinkedHashMap><name>amy</name><age>42069</age><address><street>123 fake street</street><city>nowhere</city><state>this was generated by github copilot</state></address></LinkedHashMap>", out);
    }

    @Test
    public void testJsonToXmlTransformation() {
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
        final var out = new XmlTransformer().transformOutput(new JsonTransformer().transformInput(input));
        assertEquals("<LinkedHashMap><name>amy</name><age>42069</age><address><street>123 fake street</street><city>nowhere</city><state>this was generated by github copilot</state></address></LinkedHashMap>", out);
    }
}
