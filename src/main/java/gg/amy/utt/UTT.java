package gg.amy.utt;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.Transformer;
import gg.amy.utt.transform.impl.CsvTransformer;
import gg.amy.utt.transform.impl.JsonTransformer;
import gg.amy.utt.transform.impl.XmlTransformer;
import gg.amy.utt.transform.impl.YamlTransformer;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
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
            InputFormat.YAML, new YamlTransformer()
    );
    private static final Map<OutputFormat, Transformer> OUTPUT_TRANSFORMERS = Map.of(
            OutputFormat.CSV, new CsvTransformer(),
            OutputFormat.JSON, new JsonTransformer(),
            OutputFormat.XML, new XmlTransformer(),
            OutputFormat.YAML, new YamlTransformer()
    );

    private UTT() {
    }

    public static void main(final String[] args) {
        final var options = new Options();
        final var inputTypes = Arrays.stream(InputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));
        final var outputTypes = Arrays.stream(OutputFormat.values())
                .map(s -> s.name().toLowerCase(Locale.ENGLISH))
                .collect(Collectors.joining(", "));
        options.addOption("i", "input", true, "Format of input. All types: " + inputTypes);
        options.addOption("o", "output", true, "Format of output. All types: " + outputTypes);

        final var parser = new DefaultParser();
        final InputFormat input;
        final OutputFormat output;
        try {
            final var cmd = parser.parse(options, args);
            input = InputFormat.valueOf(cmd.getOptionValue("input").toUpperCase(Locale.ROOT));
            output = OutputFormat.valueOf(cmd.getOptionValue("output").toUpperCase(Locale.ROOT));
        } catch(final Exception e) {
            final var helper = new HelpFormatter();
            helper.printHelp("utt", options);
            return;
        }

        try {
            // TODO: Streaming someday
            final var inputData = new StringBuilder();
            try(final var bis = new BufferedInputStream(System.in)) {
                try(final var scanner = new Scanner(bis)) {
                    while(scanner.hasNextLine()) {
                        inputData.append(scanner.nextLine()).append('\n');
                    }
                }
            }

            final var data = inputData.toString();

            if(!INPUT_TRANSFORMERS.containsKey(input)) {
                throw new IllegalArgumentException("Unknown input transformer: " + input);
            }
            final Object transformationTarget = INPUT_TRANSFORMERS.get(input).transformInput(data);

            if(!OUTPUT_TRANSFORMERS.containsKey(output)) {
                throw new IllegalArgumentException("Unknown output transformer: " + output);
            }
            System.out.println(OUTPUT_TRANSFORMERS.get(output).transformOutput(transformationTarget));
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }
}
