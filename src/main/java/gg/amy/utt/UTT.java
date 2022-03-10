package gg.amy.utt;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.fake.FakeList;
import gg.amy.utt.fake.Faker;
import gg.amy.utt.mapper.Mapper;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;
import gg.amy.utt.transform.impl.*;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author amy
 * @since 2/27/22.
 */
public final class UTT {
    private static final Map<InputFormat, Transformer> INPUT_TRANSFORMERS = Map.of(
            InputFormat.CSV, new CsvTransformer(),
            InputFormat.JSON, new JsonTransformer(),
            InputFormat.XML, new XmlTransformer(),
            InputFormat.YAML, new YamlTransformer(),
            InputFormat.PLAIN, new PlainTransformer(),
            InputFormat.PROPERTIES, new PropertiesTransformer(),
            InputFormat.TOML, new TomlTransformer(),
            InputFormat.BASE64, new Base64Transformer()
    );
    private static final Map<OutputFormat, Transformer> OUTPUT_TRANSFORMERS = Map.of(
            OutputFormat.CSV, new CsvTransformer(),
            OutputFormat.JSON, new JsonTransformer(),
            OutputFormat.XML, new XmlTransformer(),
            OutputFormat.YAML, new YamlTransformer(),
            OutputFormat.PLAIN, new PlainTransformer(),
            OutputFormat.PROPERTIES, new PropertiesTransformer(),
            OutputFormat.TOML, new TomlTransformer(),
            OutputFormat.BASE64, new Base64Transformer()
    );

    private UTT() {
    }

    public static void main(@Nonnull final String[] args) {
        // TODO: Figure out enabling GraalVM JIT: https://docs.oracle.com/en/graalvm/enterprise/22/docs/reference-manual/js/RunOnJDK/
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        final var options = new Options();
        final var inputTypes = Arrays.stream(InputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));
        final var outputTypes = Arrays.stream(OutputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));

        /*
         * Conventions:
         * ------------
         * - As few required args as possible
         * - Flags transforming the input or output data are lowercase, whereas
         *   flags that operate on the intermediate representation are
         *   uppercase.
         * - Flags must have descriptive names and descriptions, so that the
         *   generated help message is useful.
         */

        options.addRequiredOption("i", "input", true, "Format of input. All types: " + inputTypes);
        options.addRequiredOption("o", "output", true, "Format of output. All types: " + outputTypes);

        options.addOption("e", "extract", true, "A http://jsonpatch.com/ path to extract from the input (ex. /foo/bar)");

        options.addOption("M", "mapper", true, "A Javascript operation to run on each mapped object (ex. to map [1,2,3] to [2,4,6], use '$ * 2'. `$` or `_` is the current object). WARNING: THIS IS VERY SLOW");
        options.addOption("F", "flatten", false, "Forcibly flatten data before serialisation if possible");

        final var parser = new DefaultParser();
        final InputFormat input;
        final OutputFormat output;
        final String extractionPath;
        final String mapper;
        final boolean flatten;
        try {
            final var cmd = parser.parse(options, args);
            input = InputFormat.valueOf(cmd.getOptionValue("input").toUpperCase(Locale.ROOT));
            output = OutputFormat.valueOf(cmd.getOptionValue("output").toUpperCase(Locale.ROOT));
            extractionPath = cmd.getOptionValue("extract");
            mapper = cmd.getOptionValue("mapper");
            flatten = cmd.hasOption("flatten");
        } catch(@Nonnull final Exception e) {
            final var helper = new HelpFormatter();
            helper.printHelp("utt", options, true);
            return;
        }

        final var ctx = new TransformationContext(input, output, extractionPath, mapper, flatten);

        try {
            final var data = collectInput();
            System.out.println(runExtraction(ctx, data));
        } catch(@Nonnull final IOException e) {
            e.printStackTrace();
        }
    }

    private static String collectInput() throws IOException {
        final var inputData = new StringBuilder();
        try(@Nonnull final var bis = new BufferedInputStream(System.in)) {
            try(@Nonnull final var scanner = new Scanner(bis)) {
                while(scanner.hasNextLine()) {
                    // Process multiline input with newlines so that ex. CSV,
                    // YAML, ... input work.
                    inputData.append(scanner.nextLine()).append('\n');
                }
            }
        }
        return inputData.toString().trim();
    }

    public static String runExtraction(@Nonnull final TransformationContext ctx, @Nonnull final String data) {
        // Sanity checks.
        if(!INPUT_TRANSFORMERS.containsKey(ctx.input())) {
            throw new IllegalArgumentException("Unknown input transformer: " + ctx.input());
        }
        if(!OUTPUT_TRANSFORMERS.containsKey(ctx.output())) {
            throw new IllegalArgumentException("Unknown output transformer: " + ctx.output());
        }

        // Load input.
        Object transformationTarget = INPUT_TRANSFORMERS.get(ctx.input()).transformInput(ctx, data);

        // Extract from transformed input as needed.
        if(ctx.extractionPath() != null && !ctx.extractionPath().isEmpty() && !ctx.extractionPath().equals("/")) {
            if(!ctx.extractionPath().startsWith("/")) {
                throw new IllegalArgumentException("--extract must start with /");
            }
            final var path = ctx.extractionPath().substring(1).split("/");
            for(final var segment : path) {
                try {
                    if(transformationTarget instanceof final Map map) {
                        transformationTarget = map.get(segment);
                    } else if(transformationTarget instanceof final List list) {
                        final var index = Integer.parseInt(segment);
                        transformationTarget = list.get(index);
                    }
                } catch(@Nonnull final Throwable t) {
                    throw new IllegalArgumentException("Error parsing --extract path at segment /" + segment + '/', t);
                }
            }
        }

        // Map as needed.
        if(ctx.mapper() != null) {
            transformationTarget = Mapper.map(ctx, transformationTarget);
        }

        // If we mapped into a byte[] accidentally, flatten it into a string.
        if(transformationTarget instanceof byte[] bytes) {
            transformationTarget = new String(bytes);
        }

        // Flatten where possible.
        if(ctx.flatten() && transformationTarget instanceof List || transformationTarget instanceof FakeList) {
            if(transformationTarget instanceof List list) {
                transformationTarget = Faker.makeFake(flatten(list).toList(), true);
            } else //noinspection ConstantConditions
                if(transformationTarget instanceof FakeList list) {
                transformationTarget = Faker.makeFake(flatten(list.delegate()).toList(), true);
            }
        }

        // Transform output.
        return OUTPUT_TRANSFORMERS.get(ctx.output()).transformOutput(ctx, transformationTarget);
    }

    private static Stream<?> flatten(@SuppressWarnings("TypeMayBeWeakened") @Nonnull final List<?> objects) {
        return objects
                .stream()
                .flatMap(o -> {
                    if(o instanceof List list) {
                        return flatten(list);
                    } else if(o instanceof FakeList fakeList) {
                        return flatten(fakeList.delegate());
                    }
                    return Stream.of(o);
                });
    }
}
