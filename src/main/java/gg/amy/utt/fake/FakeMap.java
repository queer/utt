package gg.amy.utt.fake;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import gg.amy.utt.UTT;
import gg.amy.utt.fake.FakeMap.Serializer;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author amy
 * @since 3/4/22.
 */
@JsonSerialize(using = Serializer.class)
public record FakeMap(Map<String, Object> delegate) implements ProxyObject {
    public FakeMap(@Nonnull final Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object getMember(final String key) {
        return Faker.makeFake(delegate.get(key), true);
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

    public static final class Serializer extends StdSerializer<FakeMap> {
        public Serializer() {
            this(FakeMap.class);
        }

        public Serializer(@Nonnull final Class<FakeMap> t) {
            super(t);
        }

        @Override
        public void serialize(@Nonnull final FakeMap fakeMap, @Nonnull final JsonGenerator jsonGenerator,
                              @Nonnull final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            for (final Entry<String, Object> o : fakeMap.delegate.entrySet()) {
                jsonGenerator.writePOJOField(o.getKey(), o.getValue());
            }
            jsonGenerator.writeEndObject();
        }
    }
}
