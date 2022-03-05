package gg.amy.utt.mapper;

import gg.amy.utt.fake.Faker;
import gg.amy.utt.transform.TransformationContext;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author amy
 * @since 3/4/22.
 */
public final class Mapper {
    private static final String LANGUAGE = "js";
    private static final String DEFAULT_SYM = "$";
    private static final String EXTRA_SYM = "_";

    private Mapper() {
    }

    @SuppressWarnings("ConstantConditions")
    public static Object map(@Nonnull final TransformationContext ctx, @Nonnull final Object transformationTarget) {
        try(@Nonnull final Context graal = Context.newBuilder(LANGUAGE)
                .allowHostAccess(HostAccess.newBuilder()
                        .allowListAccess(true)
                        .allowArrayAccess(true)
                        .build())
                .build()) {
            final List<Value> results;
            final boolean isList = transformationTarget instanceof List;
            if(transformationTarget instanceof Map) {
                graal.getBindings(LANGUAGE).putMember(DEFAULT_SYM, Faker.makeFake(transformationTarget));
                graal.getBindings(LANGUAGE).putMember(EXTRA_SYM, Faker.makeFake(transformationTarget));
                results = List.of(graal.eval(LANGUAGE, ctx.mapper()));
            } else if(transformationTarget instanceof List<?> list) {
                results = list.stream().map(o -> {
                    if(o instanceof Map || o instanceof List) {
                        graal.getBindings(LANGUAGE).putMember(DEFAULT_SYM, Faker.makeFake(o));
                        graal.getBindings(LANGUAGE).putMember(EXTRA_SYM, Faker.makeFake(o));
                    } else if(o instanceof String || o instanceof Number || o instanceof Boolean) {
                        graal.getBindings(LANGUAGE).putMember(DEFAULT_SYM, o);
                        graal.getBindings(LANGUAGE).putMember(EXTRA_SYM, o);
                    } else {
                        graal.getBindings(LANGUAGE).putMember(DEFAULT_SYM, Faker.makeFake(o));
                        graal.getBindings(LANGUAGE).putMember(EXTRA_SYM, Faker.makeFake(o));
                    }
                    return graal.eval(LANGUAGE, ctx.mapper());
                }).toList();
            } else {
                graal.getBindings(LANGUAGE).putMember(DEFAULT_SYM, Faker.makeFake(transformationTarget));
                graal.getBindings(LANGUAGE).putMember(EXTRA_SYM, Faker.makeFake(transformationTarget));
                results = List.of(graal.eval(LANGUAGE, ctx.mapper()));
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
                return fromPolyglot(cleanResults);
            } else {
                return fromPolyglot(cleanResults.get(0));
            }
        }
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
}
