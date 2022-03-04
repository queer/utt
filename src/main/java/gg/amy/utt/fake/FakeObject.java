package gg.amy.utt.fake;

import gg.amy.utt.UTT;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author amy
 * @since 3/4/22.
 */
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
}
