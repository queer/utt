package gg.amy.utt;

import gg.amy.utt.data.InputFormat;
import gg.amy.utt.data.OutputFormat;
import gg.amy.utt.transform.TransformationContext;
import gg.amy.utt.transform.Transformer;
import gg.amy.utt.transform.impl.*;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
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
            try(@Nonnull final Context graal = Context.newBuilder("js")
                    .allowHostAccess(HostAccess.newBuilder()
                            .allowListAccess(true)
                            .allowArrayAccess(true)
                            .build())
                    .build()) {
                final List<Value> results;
                final boolean isList = transformationTarget instanceof List;
                if(transformationTarget instanceof Map) {
                    graal.getBindings("js").putMember("$", makeFake(transformationTarget));
                    results = List.of(graal.eval("js", ctx.mapper()));
                } else if(transformationTarget instanceof List<?> list) {
                    results = list.stream().map(o -> {
                        if(o instanceof Map || o instanceof List) {
                            graal.getBindings("js").putMember("$", makeFake(o));
                        } else if(o instanceof String || o instanceof Number || o instanceof Boolean) {
                            graal.getBindings("js").putMember("$", o);
                        } else {
                            graal.getBindings("js").putMember("$", makeFake(o));
                        }
                        return graal.eval("js", ctx.mapper());
                    }).toList();
                } else {
                    graal.getBindings("js").putMember("$", makeFake(transformationTarget));
                    results = List.of(graal.eval("js", ctx.mapper()));
                }
                final var cleanResults = results.stream().map(value -> {
                    if(value.isBoolean()) {
                        return value.asBoolean();
                    } else if(value.isNumber()) {
                        return value.asDouble();
                    } else if(value.isNull()) {
                        return null;
                    } else if(value.isString()) {
                        return value.asString();
                    } else if(value.isHostObject()) {
                        return value.asHostObject();
                    } else if(value.isProxyObject()) {
                        return value.asProxyObject();
                    } else if(value.hasMembers()) {
                        return value.as(Map.class);
                    } else if(value.hasArrayElements()) {
                        return value.as(ArrayList.class);
                    } else {
                        throw new IllegalArgumentException("Unsupported result type: " + value.getClass().getName());
                    }
                }).toList();
                if(isList) {
                    transformationTarget = fromPolyglot(cleanResults);
                } else {
                    transformationTarget = fromPolyglot(cleanResults.get(0));
                }
            }
        }

        return OUTPUT_TRANSFORMERS.get(ctx.output()).transformOutput(ctx, transformationTarget);
    }

    private static Object fromPolyglot(@Nonnull final Object polyglot) {
        if(polyglot instanceof Map map) {
            final Map<Object, Object> out = new LinkedHashMap<>();
            for(final Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
                out.put(fromPolyglot(entry.getKey()), fromPolyglot(entry.getValue()));
            }
            return out;
        } else if(polyglot instanceof List list) {
            final Collection<Object> out = new ArrayList<>();
            for(final Object o : (List<?>) list) {
                out.add(fromPolyglot(o));
            }
            return out;
        } else {
            return polyglot;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object makeFake(@Nonnull final Object object) {
        if(object instanceof Map map) {
            return new FakeMap(map);
        } else if(object instanceof List list) {
            return new FakeList(list);
        } else {
            return new FakeObject(object);
        }
    }

    private static Object makeFake(@Nonnull final Object object, final boolean noObjects) {
        if(object instanceof Map map) {
            return new FakeMap(map);
        } else if(object instanceof List list) {
            return new FakeList(list);
        } else {
            if(noObjects) {
                return object;
            } else {
                return new FakeObject(object);
            }
        }
    }

    private record FakeObject(Object delegate) implements ProxyObject {
        private FakeObject(@Nonnull final Object delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getMember(@Nonnull final String key) {
            try {
                final var field = delegate.getClass().getDeclaredField(key);
                field.setAccessible(true);
                return makeFake(field.get(delegate), true);
            } catch(final IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object getMemberKeys() {
            return Arrays.stream(delegate.getClass().getDeclaredFields()).map(Field::getName).toList();
        }

        @Override
        public boolean hasMember(final String key) {
            //noinspection unchecked
            return ((List<String>) getMemberKeys()).contains(key);
        }

        @Override
        public void putMember(final String key, final Value value) {
            try {
                final var field = delegate.getClass().getDeclaredField(key);
                field.setAccessible(true);
                field.set(delegate, value.asHostObject());
            } catch(final IllegalAccessException | NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private record FakeMap(Map<String, Object> delegate) implements ProxyObject {
        private FakeMap(@Nonnull final Map<String, Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getMember(final String key) {
            return makeFake(delegate.get(key), true);
        }

        @Override
        public Object getMemberKeys() {
            return delegate.keySet().stream().toList();
        }

        @Override
        public boolean hasMember(final String key) {
            return delegate.containsKey(key);
        }

        @Override
        public void putMember(final String key, final Value value) {
            delegate.put(key, value.asHostObject());
        }
    }

    private record FakeList(List<Object> delegate) implements ProxyArray {
        private FakeList(@Nonnull final List<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object get(final long index) {
            return makeFake(delegate.get((int) index), true);
        }

        @Override
        public void set(final long index, final Value value) {
            delegate.set((int) index, value.asHostObject());
        }

        @Override
        public long getSize() {
            return delegate.size();
        }
    }
}
