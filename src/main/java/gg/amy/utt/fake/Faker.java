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

    public static Object makeFake(@Nonnull final Object object) {
        return makeFake(object, false);
    }

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
