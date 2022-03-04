package gg.amy.utt.fake;

import gg.amy.utt.UTT;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author amy
 * @since 3/4/22.
 */
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
}
