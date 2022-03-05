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
public class MapReducingTransformationTest {
    @Test
    public void testMapperWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "$ * 2", null, false);
        final var out = UTT.runExtraction(ctx, "[1, 2, 3]");
        assertEquals("[2.0,4.0,6.0]", out);
    }

    @Test
    public void testObjectAccessWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "({\"key\": $.key * 2})", null, false);
        final var out = UTT.runExtraction(ctx, "{\"key\": 1}");
        assertEquals("{\"key\":2}", out);
    }

    @Test
    public void testArrayAccessWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "($[0] * 2)", null, false);
        final var out = UTT.runExtraction(ctx, "[[1], [2], [3]]");
        assertEquals("[2.0,4.0,6.0]", out);
    }

    @Test
    public void testNestedAccessWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "$.key.key2", null, false);
        final var out = UTT.runExtraction(ctx, "{\"key\": {\"key2\": \"value\"}}");
        assertEquals("\"value\"", out);
    }

    @Test
    public void testArrayObjectNestingWorks() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "$.data", null, false);
        final var out = UTT.runExtraction(ctx, """
                [
                    {
                        "data": [
                            {
                                "key": "value"
                            },
                            {
                                "key": "value2"
                            },
                            {
                                "key": "value3"
                            }
                        ]
                    },
                    {
                        "data": [
                            {
                                "key": "value4"
                            },
                            {
                                "key": "value5"
                            },
                            {
                                "key": "value6"
                            }
                        ]
                    }
                ]
                """);
        assertEquals("[[{\"key\":\"value\"},{\"key\":\"value2\"},{\"key\":\"value3\"}],[{\"key\":\"value4\"},{\"key\":\"value5\"},{\"key\":\"value6\"}]]", out);
    }

    @Test
    public void testArrayObjectNestingWorksWithReduction() {
        final var ctx = new TransformationContext(InputFormat.JSON, OutputFormat.JSON, null, "$.data", "$", true);
        final var out = UTT.runExtraction(ctx, """
                [
                    {
                        "data": [
                            {
                                "key": "value"
                            },
                            {
                                "key": "value2"
                            },
                            {
                                "key": "value3"
                            }
                        ]
                    },
                    {
                        "data": [
                            {
                                "key": "value4"
                            },
                            {
                                "key": "value5"
                            },
                            {
                                "key": "value6"
                            }
                        ]
                    }
                ]
                """);
        assertEquals("[{\"key\":\"value\"},{\"key\":\"value2\"},{\"key\":\"value3\"},{\"key\":\"value4\"},{\"key\":\"value5\"},{\"key\":\"value6\"}]", out);
    }
}
