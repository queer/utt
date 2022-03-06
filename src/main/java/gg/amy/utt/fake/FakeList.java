package gg.amy.utt.fake;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import gg.amy.utt.fake.FakeList.Serializer;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * @author amy
 * @since 3/4/22.
 */
@JsonSerialize(using = Serializer.class)
public record FakeList(List<Object> delegate) implements ProxyArray {
    public FakeList(@Nonnull final List<Object> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object get(final long index) {
        return Faker.makeFake(delegate.get((int) index), true);
    }

    @Override
    public void set(final long index, final Value value) {
        delegate.set((int) index, value.as(Object.class));
    }

    @Override
    public long getSize() {
        return delegate.size();
    }

    public static final class Serializer extends StdSerializer<FakeList> {
        public Serializer() {
            this(FakeList.class);
        }

        public Serializer(@Nonnull final Class<FakeList> t) {
            super(t);
        }

        @Override
        public void serialize(@Nonnull final FakeList fakeList, @Nonnull final JsonGenerator jsonGenerator,
                              @Nonnull final SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartArray();
            for(final Object o : fakeList.delegate) {
                jsonGenerator.writePOJO(Faker.makeFake(o));
            }
            jsonGenerator.writeEndArray();
        }
    }
}
