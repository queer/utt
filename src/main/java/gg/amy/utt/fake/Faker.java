package gg.amy.utt.fake;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @author amy
 * @since 3/4/22.
 */
public class Faker {
    @SuppressWarnings("unchecked")
    public static Object makeFake(@Nonnull final Object object) {
        if(object instanceof Map map) {
            return new FakeMap(map);
        } else if(object instanceof List list) {
            return new FakeList(list);
        } else {
            return new FakeObject(object);
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static Object makeFake(@Nonnull final Object object, final boolean noObjects) {
        if(object instanceof Map map) {
            //noinspection unchecked
            return new FakeMap(map);
        } else if(object instanceof List list) {
            //noinspection unchecked
            return new FakeList(list);
        } else {
            if(noObjects) {
                return object;
            } else {
                return new FakeObject(object);
            }
        }
    }
}