package gg.amy.utt;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
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
            InputFormat.TOML, new TomlTransformer()
    );
    private static final Map<OutputFormat, Transformer> OUTPUT_TRANSFORMERS = Map.of(
            OutputFormat.CSV, new CsvTransformer(),
            OutputFormat.JSON, new JsonTransformer(),
            OutputFormat.XML, new XmlTransformer(),
            OutputFormat.YAML, new YamlTransformer(),
            OutputFormat.PLAIN, new PlainTransformer(),
            OutputFormat.PROPERTIES, new PropertiesTransformer(),
            OutputFormat.TOML, new TomlTransformer()
    );

    private UTT() {
    }

    public static void main(@Nonnull final String[] args) {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        final var options = new Options();
        final var inputTypes = Arrays.stream(InputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));
        final var outputTypes = Arrays.stream(OutputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));
        options.addOption("i", "input", true, "Format of input. All types: " + inputTypes);
        options.addOption("o", "output", true, "Format of output. All types: " + outputTypes);
        options.addOption("e", "extract", true, "A http://jsonpatch.com/ path to extract from the input (ex. /foo/bar)");
        options.addOption("M", "mapper", true, "A Javascript operation to run on each mapped object (ex. to map [1,2,3] to [2,4,6], use '$ * 2'. $ is the current object). WARNING: THIS IS VERY SLOW");

        final var parser = new DefaultParser();
        final InputFormat input;
        final OutputFormat output;
        final String extractionPath;
        final String mapper;
        try {
            final var cmd = parser.parse(options, args);
            input = InputFormat.valueOf(cmd.getOptionValue("input").toUpperCase(Locale.ROOT));
            output = OutputFormat.valueOf(cmd.getOptionValue("output").toUpperCase(Locale.ROOT));
            extractionPath = cmd.getOptionValue("extract");
            mapper = cmd.getOptionValue("mapper");
        } catch(@Nonnull final Exception e) {
            final var helper = new HelpFormatter();
            helper.printHelp("utt", options);
            return;
        }

        final var ctx = new TransformationContext(input, output, extractionPath, mapper);

        try {
            final var data = collectInput();
            System.out.println(runExtraction(ctx, data));
        } catch(@Nonnull final IOException e) {
            e.printStackTrace();
        }
    }

    private static String collectInput() throws IOException {
        // TODO: Streaming someday
        final var inputData = new StringBuilder();
        try(@Nonnull final var bis = new BufferedInputStream(System.in)) {
            try(@Nonnull final var scanner = new Scanner(bis)) {
                while(scanner.hasNextLine()) {
                    inputData.append(scanner.nextLine()).append('\n');
                }
            }
        }
        return inputData.toString();
    }

    public static String runExtraction(@Nonnull final TransformationContext ctx, @Nonnull final String data) {
        if(!INPUT_TRANSFORMERS.containsKey(ctx.input())) {
            throw new IllegalArgumentException("Unknown input transformer: " + ctx.input());
        }
        Object transformationTarget = INPUT_TRANSFORMERS.get(ctx.input()).transformInput(ctx, data);

        // Extract from transformed input as needed
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

        // Transform output
        if(!OUTPUT_TRANSFORMERS.containsKey(ctx.output())) {
            throw new IllegalArgumentException("Unknown output transformer: " + ctx.output());
        }

        if(ctx.mapper() != null) {
            transformationTarget = Mapper.map(ctx, transformationTarget);
        }

        return OUTPUT_TRANSFORMERS.get(ctx.output()).transformOutput(ctx, transformationTarget);
    }
}
