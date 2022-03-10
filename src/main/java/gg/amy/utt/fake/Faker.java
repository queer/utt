package gg.amy.utt.fake;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author amy
 * @since 3/4/22.
 */
public final class Faker {
    private Faker() {
    }

    /**
     * {@link #makeFake(Object, boolean)}
     */
    public static Object makeFake(@Nonnull final Object object) {
        return makeFake(object, false);
    }

    /**
     * Converts the input object into a fake object. A fake object is a wrapper
     * around some POJO that can be safely accessed inside of a GraalVM
     * Polyglot {@link org.graalvm.polyglot.Context}. This method will convert
     * normal POJOs into fake objects, but will pass through fake objects
     * as-is.
     *
     * @param object    The object to convert to a fake object.
     * @param noObjects Whether non-list/map objects should be converted.
     * @return A new, potentially-fake object.
     */
    @SuppressWarnings({"SameParameterValue", "unchecked"})
    public static Object makeFake(@Nonnull final Object object, final boolean noObjects) {
        if(object instanceof Map map) {
            return new FakeMap(Map.copyOf(map));
        } else if(object instanceof List list) {
            return new FakeList(List.copyOf(list));
        } else if(object instanceof FakeList fakeList) {
            return fakeList;
        } else if(object instanceof FakeMap fakeMap) {
            return fakeMap;
        } else if(object instanceof FakeObject fakeObject) {
            return fakeObject;
        } else {
            if(noObjects) {
                return object;
            } else {
                return new FakeObject(object);
            }
        }
    }
}
