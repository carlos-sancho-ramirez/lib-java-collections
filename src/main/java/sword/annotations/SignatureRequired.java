package sword.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that in future major release, the annotated method will be removed
 * because it is already overloaded and the alternative also includes the logic
 * of this method.
 *
 * When we have a class B that inherits from class A, and a method like
 * <code>ReturnType methodName(B param)</code>. We might think that we could
 * change this method to <code>ReturnType methodName(A param)</code> and any
 * code using this API will not see any difference. However, even if this is
 * true in source code, a NoSuchMethodError may be thrown if the code calling
 * this method has not been recompiled.
 *
 * The reason of throwing a NoSuchMethodError is that the class file stores a
 * method reference like "(B)LReturnType;", which does not match the current
 * one with type A as parameter.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface SignatureRequired {
}
