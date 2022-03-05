package gg.amy.utt.fake;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import gg.amy.utt.fake.FakeObject.Serializer;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author amy
 * @since 3/4/22.
 */
@JsonSerialize(using = Serializer.class)
public record FakeObject(Object delegate) implements ProxyObject {
    public FakeObject(@Nonnull final Object delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getMember(@Nonnull final String key) {
        try {
            final var field = delegate.getClass().getDeclaredField(key);
            field.setAccessible(true);
            return Faker.makeFake(field.get(delegate), true);
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

    public static final class Serializer extends StdSerializer<FakeObject> {
        public Serializer() {
            this(FakeObject.class);
        }

        public Serializer(final Class<FakeObject> t) {
            super(t);
        }

        @Override
        public void serialize(@Nonnull final FakeObject fakeObject, @Nonnull final JsonGenerator jsonGenerator,
                              @Nonnull final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            for(final Field f : fakeObject.delegate.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    jsonGenerator.writePOJOField(f.getName(), f.get(fakeObject.delegate));
                } catch(@Nonnull final IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            jsonGenerator.writeEndObject();
        }
    }
}
