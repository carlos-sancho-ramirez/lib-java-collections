package sword.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that in future major release, the annotated method may be abstract,
 * forcing any subtype to implement its own implementation.
 *
 * This annotation makes especially sense on default methods within interfaces.
 * If the default implementation is removed, all implementations of that
 * interface must implement the method, which will break the Liskov substitution
 * principle.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ToBeAbstract {

    /**
     * States the reason why it should be abstracted.
     */
    String value();
}
