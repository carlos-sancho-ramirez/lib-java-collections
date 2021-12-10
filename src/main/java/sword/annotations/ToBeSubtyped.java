package sword.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that in future major release, the type of the returned value of
 * the annotated method may be a subtype of the current one.
 *
 * This annotation makes especially sense on methods within interfaces.
 * If the returned type is changed, all implementations of that interface must
 * be updated, which will break the Liskov substitution principle.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ToBeSubtyped {

    /**
     * Points to the type that should be returned instead of the current one.
     */
    Class<?> value();
}
