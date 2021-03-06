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
public class TomlTransformerTest {
    @Test
    public void testTomlIdentityTransformation() {
        final var input = """
                [person]
                name = "amy"
                age = 42069
                [person.address]
                street = "123 fake street"
                city = "nowhere"
                state = "this was generated by github copilot"
                """;
        final var ctx = new TransformationContext(InputFormat.TOML, OutputFormat.TOML, null, null, false, false);
        final var out = new TomlTransformer().transformOutput(ctx, new TomlTransformer().transformInput(ctx, input));
        assertEquals("""
                person.name = 'amy'
                person.age = 42069
                person.address.street = '123 fake street'
                person.address.city = 'nowhere'
                person.address.state = 'this was generated by github copilot'
                """, out);
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
        final var ctx = new TransformationContext(InputFormat.XML, OutputFormat.TOML, null, null, false, false);
        final var out = new TomlTransformer().transformOutput(ctx, new XmlTransformer().transformInput(ctx, input));
        assertEquals("""
                name = 'amy'
                age = '42069'
                address.street = '123 fake street'
                address.city = 'nowhere'
                address.state = 'this was generated by github copilot'
                """, out);
    }
}
