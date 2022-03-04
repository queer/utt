package gg.amy.utt.transform;

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
                    }
                ]
                """;
        final var parsed = new JsonTransformer().transformInput(input);
        final var csv = new CsvTransformer().transformOutput(parsed);
        assertEquals("""
                name,age,cars
                John,30,"[{""name"":""Ford"",""models"":[""Fiesta"",""Focus"",""Mustang""]},{""name"":""BMW"",""models"":[""320"",""X3"",""X5""]}]"
                """, csv);
    }
}
