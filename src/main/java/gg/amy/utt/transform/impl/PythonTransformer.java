package gg.amy.utt.transform.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;
import org.python.util.PythonInterpreter;

import javax.annotation.Nonnull;

/**
 * @author amy
 * @since 3/11/22.
 */
public class PythonTransformer implements Transformer {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Nonnull
    @Override
    public Object transformInput(@Nonnull final TransformationContext ctx, @Nonnull final String input) {
        final var interpreter = new PythonInterpreter();
        interpreter.set("input", input);
        interpreter.exec("import ast, json; output = json.dumps(ast.literal_eval(input))");
        final var output = interpreter.get("output");
        try {
            return MAPPER.readValue(output.asString(), Object.class);
        } catch(@Nonnull final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    @Override
    public String transformOutput(@Nonnull final TransformationContext ctx, @Nonnull final Object input) {
        throw new UnsupportedOperationException("Python objects are not a valid output format!");
    }
}
