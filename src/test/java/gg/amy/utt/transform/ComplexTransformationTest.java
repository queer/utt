package gg.amy.utt.transform;

import gg.amy.utt.UTT;
import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.impl.CsvTransformer;
import gg.amy.utt.transform.impl.JsonTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author amy
 * @since 3/4/22.
 */
public class ComplexTransformationTest {
    @Test
    public void testNestedJsonObjectsArrayToCsv() {
        final var input = """
                [
                    {
                        "name": "John",
                        "age": 30,
                        "cars": [
                            {
                                "name": "Ford",
                                "models": [
                                    "Fiesta",
                                    "Focus",
                                    "Mustang"
                                ]
                            },
                            {
                                "name": "BMW",
                                "models": [
                                    "320",
                                    "X3",
                                    "X5"
                                ]
                            }
                        ]
                    },
                    {
                        "name": "Fred",
                        "age": 69,
                        "cars": [
                            {
                                "name": "Ford",
                                "models": [
                                    "Fiesta",
                                    "Focus",
                                    "Mustang"
                                ]
                            },
                            {
                                "name": "BMW",
                                "models": [
                                    "320",
                                    "X3",
                                    "X5"
                                ]
                            }
                        ]
                    }
                ]
                """;
        final var ctx = new TransformationContext(InputFormat.XML, OutputFormat.XML, null, null, false, false);
        final var parsed = new JsonTransformer().transformInput(ctx, input);
        final var csv = new CsvTransformer().transformOutput(ctx, parsed);
        assertEquals("""
                name,age,cars
                John,30,"[{""name"":""Ford"",""models"":[""Fiesta"",""Focus"",""Mustang""]},{""name"":""BMW"",""models"":[""320"",""X3"",""X5""]}]"
                Fred,69,"[{""name"":""Ford"",""models"":[""Fiesta"",""Focus"",""Mustang""]},{""name"":""BMW"",""models"":[""320"",""X3"",""X5""]}]"
                """, csv);
    }

    @Test
    public void testPathExtraction() {
        assertEquals("\"value\"", UTT.runExtraction(new TransformationContext(InputFormat.JSON, OutputFormat.JSON, "/key", null, false, false), "{\"key\":\"value\"}"));
        assertEquals("[\"value\"]", UTT.runExtraction(new TransformationContext(InputFormat.JSON, OutputFormat.JSON, "/key", null, false, false), "{\"key\":[\"value\"]}"));
        assertEquals("\"value\"", UTT.runExtraction(new TransformationContext(InputFormat.JSON, OutputFormat.JSON, "/key/0", null, false, false), "{\"key\":[\"value\"]}"));
    }

    @Test
    public void testFlattening() {
        assertEquals("[1.0,2.0,3.0]", UTT.runExtraction(new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, null, true, false), "[1, [2], [[3]]]"));
        assertEquals("[1.0,2.0,3.0]", UTT.runExtraction(new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "_.key", true, false), "{\"key\": [1, [2], [[3]]]}"));
    }
}
