package com.shorgov;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>A utility class designed to adapt one object (the delegate) to match a target interface,
 * even if the delegate does not formally implement that interface, provided it contains
 * public methods with matching signatures.</p>
 *
 * <p>This implementation uses Java's {@link Proxy} mechanism to dynamically create an
 * adapter that reflects method calls to the underlying delegate object.</p>
 *
 * @author shorgov
 * @version 1.0
 */
public class DynamicProxy {

    /**
     * The underlying object whose methods will be invoked by the proxy.
     */
    private final Object delegate;

    /**
     * Private constructor to enforce the use of the static factory method {@link #from(Object)}.
     *
     * @param delegate The object to be wrapped and delegated to.
     */
    private DynamicProxy(Object delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new {@code DynamicProxy} factory instance wrapping the given delegate object.
     *
     * @param delegate The object that holds the actual implementation methods. Must not be null.
     * @return A new {@code DynamicProxy} instance.
     */
    public static DynamicProxy from(Object delegate) {
        if (Objects.isNull(delegate)) {
            throw new IllegalArgumentException("Delegate object cannot be null.");
        }
        return new DynamicProxy(delegate);
    }

    /**
     * <p>Adapts the wrapped delegate object to the target interface class {@code toClass}.</p>
     *
     * <p>The adaptation logic is as follows:</p>
     * <ul>
     * <li>If the delegate object already implements or is assignable to the {@code toClass},
     * the original delegate object is returned directly (identity optimization).</li>
     * <li>Otherwise, a new dynamic proxy instance implementing {@code toClass} is created.
     * When a method is called on the proxy, the {@code InvocationHandler} searches the
     * delegate's public methods for a name match and invokes it reflectively.</li>
     * </ul>
     *
     * @param <T>     The target interface type.
     * @param toClass The {@code Class} object representing the target interface to adapt to.
     * @return An instance of the target interface, either the original delegate or a dynamic proxy.
     * @throws RuntimeException which wraps a {@link NoSuchMethodException} if the target interface
     *                          contains a method that cannot be found on the delegate object.
     */
    public <T> T to(Class<T> toClass) {
        if (toClass.isAssignableFrom(delegate.getClass())) {
            return toClass.cast(delegate);
        }
        Object returnValue = Proxy.newProxyInstance(toClass.getClassLoader(), new Class[]{toClass},
                (_, method, args) -> {

                    Optional<Method> candidate = Arrays.stream(delegate.getClass().getMethods())
                            .filter(m -> m.getName().equals(method.getName()))
                            .findFirst();

                    if (candidate.isPresent()) {
                        return candidate.get().invoke(delegate, args);
                    }

                    throw new NoSuchMethodException("Method not found %s".formatted(method.getName()));
                });
        return toClass.cast(returnValue);
    }
}
