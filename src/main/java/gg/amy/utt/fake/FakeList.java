package gg.amy.utt.fake;

import gg.amy.utt.UTT;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author amy
 * @since 3/4/22.
 */
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
        delegate.set((int) index, value.asHostObject());
    }

    @Override
    public long getSize() {
        return delegate.size();
    }
}
